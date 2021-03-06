package kizwid.sqlLoader;

import kizwid.shared.dao.discriminator.SimpleCriteria;
import kizwid.sqlLoader.dao.DatabaseReleaseDao;
import kizwid.sqlLoader.domain.DatabaseRelease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * Util for deploying release scripts onto our database
 * and also for reloading database objects (storedProc, view, trigger etc)
 *
 * User: kizwid
 * Date: 2012-01-31
 */
public class SqlLoader {

    private static final Logger logger = LoggerFactory.getLogger(SqlLoader.class);
    private final DatabaseReleaseDao databaseReleaseDao;
    private final JdbcTemplate jdbcTemplate;
    private final String rootPath;

    public SqlLoader(DatabaseReleaseDao databaseReleaseDao, DataSource dataSource, String rootPath) {
        this.databaseReleaseDao = databaseReleaseDao;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.rootPath = rootPath;
    }

    public void load(String... paths) throws IOException {
        for (final String path : paths) {
            String fullPath = appendTrailingSlash(rootPath) + path;
            URL url = Thread.currentThread().getContextClassLoader().getResource( fullPath);
            logger.info("loading database path {} from {}",fullPath, url);
            createLoader(url, fullPath).load();
    }
    }

    private ScriptLoader createLoader(URL sourceUrl, String path){

        if(path.endsWith("/releases")){
            return new IncrementalScriptLoader(jdbcTemplate, sourceUrl, databaseReleaseDao, path);
        }else {
            return new RepeatableScriptLoader(jdbcTemplate, sourceUrl, path);
        }

    }

    // we always address the resource via its parent folder, Jetty expects a folder address to have a trailing slash
    public static String appendTrailingSlash(String address) {
        if (!address.endsWith("/")) {
            address = address + "/";
        }
        return address;
    }

    public static InputStream getInputStream(String fileName){
        InputStream resourceInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        if (resourceInputStream == null) {
            throw new IllegalArgumentException("Resource " + fileName + " was not found on the classpath.");
        }
        return resourceInputStream;
    }

    public static void main( String[] args) throws IOException {

        String[] locations = new String[]{
                "classpath:sqlLoader/sqlLoader.spring.xml"
        };
        ApplicationContext ctx = new ClassPathXmlApplicationContext(locations);
        SqlLoader sqlLoader = ctx.getBean("sqlLoader",SqlLoader.class);
        sqlLoader.load("releases", "views");

    }


    /** -------------------------------------------------------------------------
     *  specialised script loader for dealing with release scripts
     * --------------------------------------------------------------------------
     */
    private static class IncrementalScriptLoader extends ScriptLoader
    {
        private final Set releaseFilesAlreadyRun;
        private final DatabaseReleaseDao dao;
        private final JdbcTemplate jdbcTemplate;

        private IncrementalScriptLoader(JdbcTemplate jdbcTemplate, URL sourceUrl, DatabaseReleaseDao dao, String path) {
            super(jdbcTemplate, sourceUrl, path);
            this.dao = dao;
            this.jdbcTemplate = jdbcTemplate;
            releaseFilesAlreadyRun = getReleaseFilesAlreadyRun(dao);
        }

        public void loadSql(NamedSql namedSql)
                throws IOException
        {
            String resource = namedSql.getName();
            String[] dirs = resource.split("/");
            String releaseName = dirs[dirs.length -1];

            if(!releaseFilesAlreadyRun.contains(releaseName))
            {
                super.loadSql(namedSql);
                DatabaseRelease release = new DatabaseRelease(resource);
                dao.save(release);
            } else
            {
                SqlLoader.logger.info("Release already in database {}", releaseName);
            }
        }

        protected List<String> getSqlFiles(URL sourceUrl) throws IOException {
            return SqlLoaderUtil.getAllSqlFiles(sourceUrl, path,"0.*.sql");
        }

        private Set<String> getReleaseFilesAlreadyRun(DatabaseReleaseDao dao)
        {

            int n = jdbcTemplate.queryForInt("select count(*) from INFORMATION_SCHEMA.SYSTEM_TABLES where table_name='DATABASE_RELEASE'");
            if(n==0){
                jdbcTemplate.update("create table database_release(\n" +
                        "    file_name varchar(100) primary key\n" +
                        "    ,executed_at timestamp not null\n" +
                        "    ,checksum varchar(64)\n" +
                        "    ,file_last_modified_at bigint\n" +
                        "    ,succeeded int\n" +
                        "\n" +
                        ")");
                DatabaseRelease databaseRelease = new DatabaseRelease("database_release.sql",new Date());
                dao.save(databaseRelease);
            }

            List<DatabaseRelease> releases = dao.find(SimpleCriteria.EMPTY_CRITERIA);
            Set<String> releaseFiles = new HashSet<String>();
            for (DatabaseRelease release : releases) {
                releaseFiles.add(release.getId());
            }
            return releaseFiles;
        }

        @Override
        protected void dropObject(String sql) {
            //not on this occasion
    }
    }
    /** -------------------------------------------------------------------------
     *  specialised script loader for dealing with database objects
     * --------------------------------------------------------------------------
     */
    private static class RepeatableScriptLoader extends ScriptLoader
    {
        private final JdbcTemplate jdbcTemplate;

        private RepeatableScriptLoader(JdbcTemplate jdbcTemplate, URL sourceUrl, String path) {
            super(jdbcTemplate, sourceUrl, path);
            this.jdbcTemplate = jdbcTemplate;
        }

        @Override
        protected void dropObject(String sql){

            //drop view/function/procedure/trigger/etc before we re-create it
            if( sql.toLowerCase().startsWith("create")){

                String[] rows = sql.split("\n");
                String[] tokens = rows[0].split(" ");
                if( !"table".equals(tokens[1].toLowerCase())){

                    final String dropSql = "drop " + tokens[1] + " " + tokens[2];
                    logger.info("running: {}", dropSql);
                    try{
                        jdbcTemplate.update(dropSql);
                    }catch (DataAccessException ex){
                        //carry on regardless
                    }

                }
            }

        }


    }


    /** -------------------------------------------------------------------------
     *  base class for executing scripts on the database
     * --------------------------------------------------------------------------
     */
    private abstract static class ScriptLoader
    {
        private final JdbcTemplate jdbcTemplate;
        private final URL sourceUrl;
        final String path;

        public ScriptLoader(JdbcTemplate jdbcTemplate, URL sourceUrl, String path)
        {
            this.sourceUrl = sourceUrl;
            this.jdbcTemplate = jdbcTemplate;
            this.path = path;
        }

        public void load()
                throws IOException
        {
            List<NamedSql> namedSqls = getNamedSql(sourceUrl);
            int numSqlFiles = namedSqls.size();
            SqlLoader.logger.info("Found {} sql files", numSqlFiles);
            for(int i = 0; i < numSqlFiles; i++)
            {
                NamedSql namedSql = namedSqls.get(i);
                SqlLoader.logger.info("Running [{} of {}] {}",new Object[]{i + 1 , numSqlFiles, namedSql});
                loadSql(namedSql);
            }

            SqlLoader.logger.info("Load complete");
        }

        protected void loadSql(NamedSql namedSql)
                throws IOException
        {
            String sql = namedSql.getSql();
            String sqlWithNoComments = SqlLoaderUtil.stripSqlComments(sql);
            List<String> sqlBatches = SqlLoaderUtil.splitSqlIntoBatches(sqlWithNoComments);
            int n = 0;
            for (String sqlBatch : sqlBatches) {

                dropObject(sqlBatch);

                logger.info("running batch {} of {} {}", new Object[]{n++, sqlBatches.size(), sqlBatch.trim()});
                jdbcTemplate.update(sqlBatch);
                //todo: handle multiple resultsets
                //todo: more descriptive output (resultset or update details)
            }

        }

        protected abstract void dropObject(String sql);

        protected List<NamedSql> getNamedSql(URL sourceUrl)
                throws IOException
        {
            List<String> allSqlFiles = getSqlFiles(sourceUrl);
            Collections.sort(allSqlFiles);

            List<NamedSql> namedSqls = new ArrayList<NamedSql>();
            for (String resource : allSqlFiles) {
                Reader reader = new InputStreamReader( getInputStream(resource));
                //String[] dirs = resource.split("/");
                //String name = dirs[dirs.length -1];
                String sql = SqlLoaderUtil.getSqlFromReader(reader, true);
                namedSqls.add( new NamedSql(resource, sql, sourceUrl.toString()));
            }


            return namedSqls;
        }

        protected List<String> getSqlFiles(URL sourceUrl) throws IOException {
            return SqlLoaderUtil.getAllSqlFiles(sourceUrl, path, ".*.sql");
        }

    }

    /** -------------------------------------------------------------------------
     *  util for discovering and collecting database scripts
     * --------------------------------------------------------------------------
     */
    public static class SqlLoaderUtil
    {

        private static final Logger logger = LoggerFactory.getLogger(SqlLoaderUtil.class);
        public static final String DEFAULT_COMMAND_TERMINATOR = ";";
        public static final String LINE_SEPARATOR = "\n";
        private static final Pattern beginBlockComment = Pattern.compile("^(.*)/\\*(.*)$");
        private static final Pattern endBlockComment = Pattern.compile("^(.*)\\*/(.*)$");
        private static final Pattern lineComment = Pattern.compile("^([^-]*)--(.*)$");

        public SqlLoaderUtil()
        {
        }

        public static List<String> splitSqlIntoBatches(String sql)
        {
            return splitSqlIntoBatches(sql, DEFAULT_COMMAND_TERMINATOR, LINE_SEPARATOR);
        }

        public static List<String> splitSqlIntoBatches(String sql, final String commandTerminator, final String lineSeparator)
        {
            List<String> batches = new ArrayList<String>();
            BufferedReader reader = new BufferedReader(new StringReader(sql));
            try
            {
                String line = reader.readLine();
                StringBuilder batch = new StringBuilder();
                String batchString;
                for(; line != null; line = reader.readLine())
                    //if(line.trim().equalsIgnoreCase(commandTerminator)) //sybase 'go' is used on a new line
                    if(line.trim().endsWith(commandTerminator)) //oracle/hsql ';' can be used at the end of a line
                    {
                        //cut the command terminator from the end of the line and add the front to the collected batch
                        if(line.endsWith("end;")){
                            batch.append(line);
                        }else{
                            batch.append(line.substring(0,line.lastIndexOf(commandTerminator)));
                        }
                        batchString = batch.toString();
                        if(batchString.trim().length() > 0)
                            batches.add(batchString);
                        batch = new StringBuilder();
                    } else
                    {
                        batch.append(line).append(lineSeparator);
                    }

                batchString = batch.toString();
                if(batchString.trim().length() > 0)
                    batches.add(batchString);
            }
            catch(IOException e)
            {
                logger.error("Should never get an IOException here", e);
            }
            return batches;
        }

        private static String getSqlFromReader(Reader sqlSource, boolean stripUseDatabaseStatements)
                throws IOException
        {
            StringBuilder sql = new StringBuilder();
            BufferedReader reader = new BufferedReader(sqlSource);
            for(String line = reader.readLine(); line != null; line = reader.readLine())
                if(!stripUseDatabaseStatements || !line.trim().equalsIgnoreCase("use xxx")) //todo: database/schema name to remove
                    sql.append(line).append("\n");

            return sql.toString();
        }

        private static List<String> getAllSqlFiles(URL url, String path, String filenamePattern) throws IOException {

            List<String> names = new LinkedList<String>();
            if("jar".equals(url.getProtocol())){

                path = appendTrailingSlash(path);
                logger.info("inspecting :" + url);
                int bang = url.getFile().indexOf('!');
                URL jar = (bang == -1 ? url: new URL(url.getFile().substring(0,bang)));
                ZipInputStream zip = new ZipInputStream( jar.openStream());
                ZipEntry ze;

                while( ( ze = zip.getNextEntry() ) != null ) {
                    String entryName = ze.getName();
                    //eg path = "database/releases/"  //TODO: pattern match nested files
                    if( entryName.startsWith(path) &&  entryName.substring(path.length()).matches(filenamePattern) ) {
                        names.add( entryName);
                    }
                }

                logger.info("" + names);


            }else if("file".equals(url.getProtocol())){

                final File directory = new File(URLDecoder.decode(url.getFile()));
                names = findInFile(directory, filenamePattern, path);

            }else{
                throw new IllegalArgumentException("unsupported protocol: " + url.getProtocol());
            }

            return names;
        }

        private static List<String> findInFile(File directory, String filenamePattern, String path) {
            final Pattern pattern = Pattern.compile(filenamePattern);
            List<String> sqlFiles = new ArrayList<String>();
            File files[] = directory.listFiles( new FileFilter(){
                @Override
                public boolean accept(File file) {
                    return pattern.matcher(file.getName()).matches();
                }
            });
            if(files == null){
                files = new File[0];
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    //allow resolve resourceName for nested files
                    sqlFiles.addAll(findInFile(file, filenamePattern, appendTrailingSlash(path) + file.getName()));
                    continue;
                }

                sqlFiles.add(appendTrailingSlash(path) + file.getName());
            }

            return sqlFiles;

        }


        private static String stripSqlComments(String sql)
        {
            BufferedReader in = new BufferedReader(new StringReader(sql));
            StringBuilder sqlOut = new StringBuilder();
            try
            {
                String line = in.readLine();
                boolean inBlockComment = false;
                for(; line != null; line = in.readLine())
                {
                    if(!inBlockComment)
                    {
                        Matcher lineCommentMatcher = lineComment.matcher(line);
                        Matcher beginBlockCommentMatcher = beginBlockComment.matcher(line);
                        if(lineCommentMatcher.matches())
                            sqlOut.append(lineCommentMatcher.group(1)).append("\n");
                        else
                        if(beginBlockCommentMatcher.matches())
                        {
                            sqlOut.append(beginBlockCommentMatcher.group(1)).append("\n");
                            inBlockComment = true;
                        } else
                        {
                            sqlOut.append(line).append("\n");
                        }
                    }
                    if(inBlockComment)
                    {
                        Matcher endBlockCommentMatcher = endBlockComment.matcher(line);
                        if(endBlockCommentMatcher.matches())
                        {
                            sqlOut.append(endBlockCommentMatcher.group(2)).append("\n");
                            inBlockComment = false;
                        }
                    }
                }

            }
            catch(IOException e) {
                logger.warn("swallowed exception", e);
            }
            return sqlOut.toString();
        }

    }

    /** -------------------------------------------------------------------------
     *  POJO for keeping details of script
     * --------------------------------------------------------------------------
     */
    private static class NamedSql {

        private final String name;
        private final String sql;
        private final String source;

        public NamedSql(String name, String sql, String source) {
            this.name = name;
            this.sql = sql;
            this.source = source;
        }

        public String getName() {
            return name;
        }

        public String getSql() {
            return sql;
        }

        public String getSource() {
            return source;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof NamedSql)) {
                return false;
            } else {
                NamedSql other = (NamedSql) obj;
                return name.equals(other.name);
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            if(source != null){
                sb.append(" from ").append(source);
            }
            return sb.toString();
        }

    }

}
