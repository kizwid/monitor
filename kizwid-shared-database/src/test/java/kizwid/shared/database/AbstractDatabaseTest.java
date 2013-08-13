package kizwid.shared.database;

import kizwid.shared.util.EncryptedPropertyPlaceholderConfigurer;
import org.dbmaintain.DbMaintainer;
import org.dbmaintain.MainFactory;
import org.dbmaintain.structure.clean.DBCleaner;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.junit.AfterClass;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath:sqlLoader/sqlLoader.spring.xml"})
public class AbstractDatabaseTest{// extends AbstractTransactionalJUnit4SpringContextTests/**/ {

//    @Resource
    protected static DataSource dataSource;
    //@Resource
    //private static JdbcTemplate jdbcTemplate;
    @Resource protected JdbcTemplate jdbcTemplate;

//    @Resource
    protected static EncryptedPropertyPlaceholderConfigurer commonProperties;

    protected static DbMaintainer dbMaintainer;
    protected static DBCleaner dbCleaner;
    protected static MainFactory dbMaintainMainFactory;

    private static Connection conn = null;

    //private static  beanFactory;


    static {

        boolean success = false;
        try {
            initDataBase();
            success = true;
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        if(!success){
            System.out.println("*********\n********\n failed to initialise database\n***************\n*************");
        }

    }


    protected static void initDataBase() throws URISyntaxException, IOException, SQLException {
        createDBMainainer().updateDatabase(false);
        createDBCleaner().cleanDatabase();
    }

    private static MainFactory createDBMaintainMainFactory() throws URISyntaxException, IOException, SQLException {
        if (dbMaintainMainFactory == null) {

            String[] resources = new String[]{
                "classpath:sqlLoader/sqlLoader.spring.xml"
            };
            BeanFactory beanFactory = new ClassPathXmlApplicationContext(resources);
            commonProperties = beanFactory.getBean("commonProperties", EncryptedPropertyPlaceholderConfigurer.class);
            dataSource = beanFactory.getBean("dataSource", DataSource.class);

            //jdbcTemplate = beanFactory.getBean("jdbcTemplate", JdbcTemplate.class);
            //jdbcTemplate = new JdbcTemplate(dataSource);


            InputStream resource = ClassLoader.getSystemClassLoader().getResourceAsStream("dbmaintain.properties");
            Properties properties = new Properties();
            properties.load(resource);
            commonProperties.processProperties(properties);//resolve placeholders
            dbMaintainMainFactory = new MainFactory(properties);
            conn = dataSource.getConnection();
        }
        return dbMaintainMainFactory;
    }

    private static DBCleaner createDBCleaner() throws URISyntaxException, IOException, SQLException {
        createDBMaintainMainFactory();
        dbCleaner = dbMaintainMainFactory.createDBCleaner();
        return dbCleaner;
    }

    private static DbMaintainer createDBMainainer() throws URISyntaxException, IOException, SQLException {
        createDBMaintainMainFactory();
        dbMaintainer = dbMaintainMainFactory.createDbMaintainer();
        return dbMaintainer;
    }

    @Test
    public void testInit() throws SQLException, IOException, URISyntaxException {
        jdbcTemplate = new JdbcTemplate(dataSource);
        assertEquals(1, jdbcTemplate.queryForInt("select 1 from dual"));
        //assertEquals(0, getRowCount("error_event"));
    }

    @BeforeTransaction
    public void createTxnExpectation() throws IOException {

    }
    @AfterTransaction
    public void verifyTxnExpectation() {

    }

    @AfterClass
    public static void tearDown() throws Exception {
        //capture final state of database
        IDatabaseConnection connection = new DatabaseConnection(conn);
        DatabaseConfig config = connection.getConfig();
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new HsqldbDataTypeFactory());
        // full database export
        IDataSet fullDataSet = connection.createDataSet();
        FlatXmlDataSet.write(fullDataSet, new FileOutputStream("target/full-dataset-dbmaintain.xml"));
        //conn.close();
    }

}
