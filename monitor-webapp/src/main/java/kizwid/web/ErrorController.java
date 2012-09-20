package kizwid.web;

import kizwid.shared.dao.*;
import kizwid.shared.dao.discriminator.SimpleCriteria;
import kizwid.shared.dao.discriminator.SimpleCriterion;
import kizwid.shared.database.release.SqlLoader;
import kizwid.shared.domain.*;
import kizwid.shared.domain.database.release.DatabaseRelease;
import kizwid.shared.util.FormatUtil;
import kizwid.web.util.CalcDigest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.*;

/**
 * User: kizwid
 * Date: 2012-02-02
 */
public class ErrorController implements Controller {

    private static final Logger logger = LoggerFactory.getLogger(ErrorController.class);
    public static final String ANONYMOUS = "anonymous";
    public static final String ACTION_DASHBOARD = "Dashboard";
    public static final String ACTION_REFRESH = "Refresh";
    public static final String ACTION_ADD_FILTERED_ERRORS_TO_ACTION = "Add filtered errors to action";
    public static final String ACTION_APPLY_FILTER = "Apply filter";
    public static final String ACTION_REMOVE_FILTER = "Remove filter";
    public static final String ACTION_DROP_ERROR_ACTION = "DropErrorAction";
    public static final String ACTION_SIMULATE = "Simulate";
    public static final String ACTION_SHOW_ERROR_ACTION_DETAILS = "ShowErrorActionDetails";
    public static final String ACTION_LIST_LOG_FILES = "List log files";
    public static final String ACTION_LOAD_FROM_LOG = "LoadFromLog";
    public static final String VIEW_LOGIN = "login";
    public static final String VIEW_DASHBOARD = "dashboard";
    public static final String VIEW_ERROR_ACTION = "errorAction";
    public static final String VIEW_LIST_LOG_FILES = "listLogFiles";
    public static final String PARAM_USER = "User";
    public static final String PARAM_BUSINESS_DATE = "BusinessDate";
    public static final String PARAM_FILTER = "Filter";
    public static final String PARAM_FILTER_COLUMN = "FilterColumn";
    public static final String PARAM_COMMENT = "Comment";
    public static final String PARAM_ERROR_DETAIL_VIEWS = "errorDetailViews";
    public static final String PARAM_VALIDATION_ERROR = "validationError";
    public static final String PARAM_ERROR_SUMMARY_VIEWS = "errorSummaryViews";
    public static final String PARAM_DIR = "Dir";
    public static final String PARAM_FILES = "Files";
    public static final String PARAM_ID = "Id";
    public static final String PARAM_ROW_COUNT = "RowCount";
    public static final String PARAM_LATEST_ID = "latestId";
    public static final String ACTION_BUILD_DATABASE = "BuildDatabase";
    public static final String PARAM_FILE = "File";
    public static final String PARAM_TOO_MANY_ERRORS = "TooManyErrors";
    public static final String VERSION = "1.0.23"; //TODO: read from properties

    private JdbcTemplate jdbcTemplate;
    private DatabaseReleaseDao databaseReleaseDao;
    private ErrorEventDao errorEventDao;
    private ErrorActionDao errorActionDao;
    private PricingErrorDao pricingErrorDao;
    private ErrorSummaryViewDao errorSummaryViewDao;
    private ErrorDetailViewDao errorDetailViewDao;
    private PricingRunDao pricingRunDao;
    private String sambaStaging;
    private String sambaPricingLogBase;
    private String sambaAppLogBase;
    private Map<String,String> sambaMapping;

    //for testing
    private static int latestErrorEventId;
    private Map<File, String> previouslyLoadedErrorLogs = new HashMap<File, String>();
    private final String env;
    private boolean isDev;
    //TODO: formalize link between columns (which control display order) and mapRowData
    public static final String[] COLUMNS = new String[]{"ErrorTime", "ErrorMessage", "RunId", "Rollup", "RiskGroup", "Batch", "Dictionary", "MarketData", "Split", "RunLabel", "LaunchEventId", "ConfigId", "BusinessDate"};


    public ErrorController(JdbcTemplate jdbcTemplate,
                           DatabaseReleaseDao databaseReleaseDao,
                           ErrorEventDao errorEventDao,
                           ErrorActionDao errorActionDao,
                           PricingErrorDao pricingErrorDao,
                           PricingRunDao pricingRunDao,
                           ErrorSummaryViewDao errorSummaryViewDao,
                           ErrorDetailViewDao errorDetailViewDao,
                           String sambamonitorAppStaging,
                           String sambaPricingLogBase,
                           String sambamonitorAppLogBase,
                           Map<String, String> sambaMapping, String env) {
        this.jdbcTemplate = jdbcTemplate;
        this.databaseReleaseDao = databaseReleaseDao;
         this.errorEventDao = errorEventDao;
        this.errorActionDao = errorActionDao;
        this.pricingErrorDao = pricingErrorDao;
        this.pricingRunDao = pricingRunDao;
        this.errorSummaryViewDao = errorSummaryViewDao;
        this.errorDetailViewDao = errorDetailViewDao;
        this.sambaStaging = sambaStaging;
        this.sambaPricingLogBase = sambaPricingLogBase;
        this.sambaAppLogBase = sambamonitorAppLogBase;
        this.sambaMapping = sambaMapping;
        this.env = env;
        isDev = "dev".equals(env);
        logger.info("env [{}]", env);
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        //facilitate deployment to dev with empty in-memory db
        if (!isDatabaseBuilt() && isDev) {
            try {
                logger.info("building database on first request");
                buildDatabase();
                saveRandomErrors(1);
            } catch (Exception ex) {
                logger.error("failed to buildDatabase", ex);
            }
        }


        Map<String, Object> model = new HashMap<String, Object>();
        String action = request.getParameter("Action");
        logger.info("Action [{}] from [{}]", new Object[]{action, request.getRemoteHost()});

        //first check for User details
        String user = getUser(request, model);
        if (ANONYMOUS.equals(user)) {
            //TODO: store intended action and params before redirect to login
            return new ModelAndView(VIEW_LOGIN, model);
        }

        if (ACTION_DASHBOARD.equals(action) ||
                ACTION_REFRESH.equals(action)) {
            dashboard(request, model);
            return new ModelAndView(VIEW_DASHBOARD, model);

        } else if (ACTION_ADD_FILTERED_ERRORS_TO_ACTION.equals(action)) {
            //current parameters
            int yyyymmdd = parseDate(request.getParameter(PARAM_BUSINESS_DATE));

            String filter = readParam(model, request, PARAM_FILTER, "");
            String filterColumn = readParam(model, request, PARAM_FILTER_COLUMN, "error_message");
            String comment = readParam(model, request, PARAM_COMMENT, "");
            if (comment.length() == 0) {
                model.put(PARAM_VALIDATION_ERROR, "you must supply a comment!");
                dashboard(request, model);
                return new ModelAndView(VIEW_DASHBOARD, model);
            }
            if (comment.length() > 255) {
                model.put(PARAM_VALIDATION_ERROR, "comments are resticted to 255 characters!");
                dashboard(request, model);
                return new ModelAndView(VIEW_DASHBOARD, model);
            }
            if (filter.length() > 255) {
                model.put(PARAM_VALIDATION_ERROR, "filter criteria are resticted to 255 characters!");
                dashboard(request, model);
                return new ModelAndView(VIEW_DASHBOARD, model);
            }
            if (!("run_business_date".equalsIgnoreCase(filterColumn) ||
                    "error_message".equalsIgnoreCase(filterColumn) ||
                    "run_id".equalsIgnoreCase(filterColumn) ||
                    "rollup".equalsIgnoreCase(filterColumn) ||
                    "risk_group".equalsIgnoreCase(filterColumn) ||
                    "batch".equalsIgnoreCase(filterColumn) ||
                    "dictionary".equalsIgnoreCase(filterColumn) ||
                    "market_data".equalsIgnoreCase(filterColumn) ||
                    "split".equalsIgnoreCase(filterColumn) ||
                    "run_label".equalsIgnoreCase(filterColumn) ||
                    "launch_event_id".equalsIgnoreCase(filterColumn) ||
                    "config_id".equalsIgnoreCase(filterColumn))
                    ) {
                model.put(PARAM_VALIDATION_ERROR, "unrecognised filterColumn [" + filterColumn + "]");
                dashboard(request, model);
                return new ModelAndView(VIEW_DASHBOARD, model);
            }
            if (comment.length() > 255) {
                model.put(PARAM_VALIDATION_ERROR, "comments are resticted to 255 characters!");
                dashboard(request, model);
                return new ModelAndView(VIEW_DASHBOARD, model);
            }
            if (filter.length() > 255) {
                model.put(PARAM_VALIDATION_ERROR, "filter criteria are resticted to 255 characters!");
                dashboard(request, model);
                return new ModelAndView(VIEW_DASHBOARD, model);
            }
            if (!("run_business_date".equalsIgnoreCase(filterColumn) ||
                    "error_message".equalsIgnoreCase(filterColumn) ||
                    "run_id".equalsIgnoreCase(filterColumn) ||
                    "rollup".equalsIgnoreCase(filterColumn) ||
                    "risk_group".equalsIgnoreCase(filterColumn) ||
                    "batch".equalsIgnoreCase(filterColumn) ||
                    "dictionary".equalsIgnoreCase(filterColumn) ||
                    "market_data".equalsIgnoreCase(filterColumn) ||
                    "split".equalsIgnoreCase(filterColumn) ||
                    "run_label".equalsIgnoreCase(filterColumn) ||
                    "launch_event_id".equalsIgnoreCase(filterColumn) ||
                    "config_id".equalsIgnoreCase(filterColumn))
                    ) {
                model.put(PARAM_VALIDATION_ERROR, "unrecognised filterColumn [" + filterColumn + "]");
                dashboard(request, model);
                return new ModelAndView(VIEW_DASHBOARD, model);
            }

            //save new comment and associated errors
            SimpleCriteria criteria = createSummaryCriteria(yyyymmdd);
            addDetailCriteria(filter, filterColumn, criteria);

            List<ErrorDetailView> errorDetailViews =
                    errorDetailViewDao.read(ErrorDetailView.class, criteria);
            model.put(PARAM_ERROR_DETAIL_VIEWS, errorDetailViews);

            if (errorDetailViews.size() > 0) {

                logger.info("applied filter {} and found {} matching errors", criteria, errorDetailViews.size());

                //create a new errorAction
                ErrorAction errorAction = new ErrorAction(yyyymmdd, user, new Date(), comment);
                errorActionDao.save(errorAction);

                //attach all filtered errors to new errorAction
                errorActionDao.attachPricingErrorsFromCriteria(errorAction.getId(), criteria);

                //attach all filtered errors to new errorAction
                errorActionDao.attachPricingErrorsFromCriteria(errorAction.getId(), criteria);

            }

            //remove filter and re-list un-actioned errors
            model.put(PARAM_FILTER, "");
            model.put(PARAM_COMMENT, "");
            model.remove(PARAM_VALIDATION_ERROR);
            dashboard(request, model);

            return new ModelAndView(VIEW_DASHBOARD, model);

        } else if (ACTION_APPLY_FILTER.equals(action)) {
            dashboard(request, model);
            return new ModelAndView(VIEW_DASHBOARD, model);

        } else if (ACTION_REMOVE_FILTER.equals(action)) {
            model.put(PARAM_FILTER, "");
            model.put(PARAM_COMMENT, "");
            dashboard(request, model);
            return new ModelAndView(VIEW_DASHBOARD, model);

        } else if (ACTION_DROP_ERROR_ACTION.equals(action)) {
            //remove all pricingErrors associated with selected errorAction
            long id = readErrorActionId(request, model);
            if (id == -1L) {
                model.put(PARAM_VALIDATION_ERROR, "You can't drop 'New Errors'!");
            } else {
                errorSummaryViewDao.deleteById(ErrorSummaryView.class, id);
            }
            dashboard(request, model);
            return new ModelAndView(VIEW_DASHBOARD, model);

        } else if (ACTION_SIMULATE.equals(action) && isDev) {
            try {
                saveRandomErrors(10);
            } catch (Exception ex) {
                model.put(PARAM_VALIDATION_ERROR, "failed to simulate errors");
                logger.error("failed to simulate errors", ex);
            }
            dashboard(request, model);
            return new ModelAndView(VIEW_DASHBOARD, model);

        } else if (ACTION_BUILD_DATABASE.equals(action) && isDev) {
            try {
                buildDatabase();
            } catch (Exception ex) {
                model.put(PARAM_VALIDATION_ERROR, "failed to buildDatabase");
                logger.error("failed to buildDatabase", ex);
            }
            dashboard(request, model);
            return new ModelAndView(VIEW_DASHBOARD, model);

        } else if (ACTION_SHOW_ERROR_ACTION_DETAILS.equals(action)) {

            long id = readErrorActionId(request, model);
            if (id == -1L) {
                dashboard(request, model);
                return new ModelAndView(VIEW_DASHBOARD, model);
            } else {

                SimpleCriteria criteria = new SimpleCriteria();
                criteria.addCriterion(new SimpleCriterion("error_action_id", SimpleCriterion.Operator.EQUALS, id, true, SimpleCriterion.DataType.NUMBER));

                //load custom summary/details to view
                dashboard(
                        request, model,
                        errorSummaryViewDao.read(ErrorSummaryView.class, criteria),
                        errorDetailViewDao.read(ErrorDetailView.class, criteria)
                );

                return new ModelAndView(VIEW_ERROR_ACTION, model); //details for specific errorAction
            }

        } else if (ACTION_LIST_LOG_FILES.equals(action) && isDev) {
            String dir = readParam(model, request, PARAM_DIR, sambaAppLogBase);
            File fileDir = new File(dir);

            if (!fileDir.exists()) {
                for (Map.Entry<String, String> entry : sambaMapping.entrySet()) {
                    if (dir.startsWith(entry.getKey() + "/")) {
                        dir = dir.replace(entry.getKey(), entry.getValue());
                        fileDir = new File(dir);
                        break;
                    }
                }
            }

            logger.info("file {} exists {}", fileDir, fileDir.exists());
            File[] files = new File[0];
            if (fileDir.exists()) {
                files = fileDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        logger.info("checking {}", name);
                        return name.startsWith("Jobstatus");
                    }
                });
                logger.info("filecount {}", files.length);
            }
            model.put(PARAM_FILES, Arrays.asList(files));
            return new ModelAndView(VIEW_LIST_LOG_FILES, model);

        } else {
            dashboard(request, model);
            return new ModelAndView(VIEW_DASHBOARD, model);
        }


    }

    /*test to see if we can read data from database*/
    private boolean isDatabaseBuilt() {
        try{
            List<DatabaseRelease> databaseReleases =
                    databaseReleaseDao.read(DatabaseRelease.class, SimpleCriteria.EMPTY_CRITERIA);
            return databaseReleases.size() > 0;
        }catch (Exception e){
            logger.warn("database is not built");
            return false;
        }
    }

    private String getUser(HttpServletRequest request, Map<String, Object> model) {
        final Principal userPrincipal = request.getUserPrincipal();
        String user = readParam(model, request, PARAM_USER, ANONYMOUS);
        if(userPrincipal == null || userPrincipal.getName().trim().length() == 0){
            logger.warn("userPrincipal not supplied: using fallback user " + user);
        }else{
            user = userPrincipal.getName();
            model.put(PARAM_USER, user);
        }
        return user;
    }

    private long readErrorActionId(HttpServletRequest request, Map<String, Object> model) {
        String idString = readParam(model, request, PARAM_ID, "-1");
        return Long.parseLong(idString);
    }

    private String readParam(Map<String, Object> model, HttpServletRequest request, String param, String defaultValue) {

        String value = (String)model.get(param);
        if(value == null){
            value = request.getParameter(param);
        }
        if( value != null && value.startsWith("$")) {
            //from uninitialised vm template
            value = null;
        }
        if(value == null || value.trim().length() == 0){
            value = defaultValue;
        }
        model.put(param, value);
        return value;
    }

    //save summary + details to view with sorting etc, but allow customisation of criteria applied
    private Map<String, Object> dashboard(HttpServletRequest request, Map<String, Object> model, List<ErrorSummaryView> errorSummaryViews, List<ErrorDetailView> errorDetailViews) throws NoSuchAlgorithmException {

        //current parameters
        model.put(PARAM_BUSINESS_DATE, parseDate(request.getParameter(PARAM_BUSINESS_DATE)));

        readParam(model, request, PARAM_FILTER, "");
        readParam(model, request, PARAM_FILTER_COLUMN, "error_message");
        readParam(model, request, PARAM_USER, ANONYMOUS);
        readParam(model, request, PARAM_COMMENT, "");
        readParam(model, request, PARAM_ID, "-1");
        readParam(model, request, "SambamonitorAppStaging", sambaStaging);
        readParam(model, request, "SambaPricingLogBase", sambaPricingLogBase);

        //default sort details by error timestamp
        final Comparator<ErrorDetailView> detailViewComparator = new Comparator<ErrorDetailView>() {
            @Override
            public int compare(ErrorDetailView o1, ErrorDetailView o2) {
                int n;

                //1st sort by error time
                if (o1.getErrorCreatedAt() != null && o2.getErrorCreatedAt() != null) {
                    n = o1.getErrorCreatedAt().compareTo(o2.getErrorCreatedAt());
                    if (n != 0) return n;
                }

                //then by id (as id is unique we don't need any more
                n = Long.valueOf(o1.getPricingErrorId()).compareTo(o2.getPricingErrorId());

                return n;
            }
        };

        //check for too many errors
        checkForTooManyErrors(model, errorDetailViews);

        //apply default sorting
        Collections.sort(errorDetailViews, Collections.reverseOrder(detailViewComparator));

        //add details to view
        model.put(PARAM_ERROR_SUMMARY_VIEWS, errorSummaryViews);
        model.put(PARAM_ERROR_DETAIL_VIEWS, errorDetailViews);
        model.put(PARAM_ROW_COUNT, errorDetailViews.size());

        //latestId
        model.put(PARAM_LATEST_ID, getLatestHash(errorSummaryViews));

        //details (associated errors)
        model.put("errorEventColumnJson", toErrorEventColumnJson(COLUMNS));
        model.put("errorEventDataJson", toErrorDetailViewJson(errorDetailViews, COLUMNS));

        model.put("Version", VERSION);
        model.put("Env",env);

        return model;
    }

    //default dashboard view: build default criteria from current filter
    private Map<String, Object> dashboard(HttpServletRequest request, Map<String, Object> model) throws NoSuchAlgorithmException {

        //current parameters
        int yyyymmdd = parseDate(request.getParameter(PARAM_BUSINESS_DATE));
        model.put(PARAM_BUSINESS_DATE, yyyymmdd);

        //current filter
        String filter = readParam(model,request, PARAM_FILTER, "");
        String filterColumn = readParam(model, request, PARAM_FILTER_COLUMN, "error_message");

        //summary (actions for today)
        SimpleCriteria criteria = createSummaryCriteria(yyyymmdd);
        List<ErrorSummaryView> errorSummaryViews =
                errorSummaryViewDao.read(ErrorSummaryView.class, criteria);

        //add filter to detail view only
        addDetailCriteria(filter, filterColumn, criteria);
        List<ErrorDetailView> errorDetailViews =
                errorDetailViewDao.read(ErrorDetailView.class, criteria);

        return dashboard(request, model, errorSummaryViews, errorDetailViews);

    }

    private void checkForTooManyErrors(Map<String, Object> model, List<ErrorDetailView> errorDetailViews) {
        if( errorDetailViews.size() > 0){
            ErrorDetailView last = errorDetailViews.get(errorDetailViews.size() -1);
            model.remove(PARAM_TOO_MANY_ERRORS);
            if(last.getPricingErrorId() == 0L){
                //this is the dummy record that marks the point the data is truncated (see @ErrorDetailViewDaoImpl)
                //remove it from the detail
                errorDetailViews.remove(last);
                //and add warning to user
                model.put(PARAM_TOO_MANY_ERRORS, "Too many errors to display them all. Only showing the first " + errorDetailViews.size());
            }
        }
    }


    //summary view is grouped by error_action_id where business_date = $selectedDate
    private SimpleCriteria createSummaryCriteria(int yyyymmdd) {
        SimpleCriteria criteria = new SimpleCriteria();
        criteria.addCriterion(new SimpleCriterion("business_date", SimpleCriterion.Operator.EQUALS, yyyymmdd, true, SimpleCriterion.DataType.NUMBER));
        return criteria;
    }

    //detail view adds additional filters to summary view
    //to show only 'New Errors' and any user defined filter
    private void addDetailCriteria(String filter, String filterColumn, SimpleCriteria criteria) {
        criteria.addCriterion(new SimpleCriterion("error_action_id", SimpleCriterion.Operator.EQUALS, -1L, true, SimpleCriterion.DataType.NUMBER));
        if(filter.length() >0){
            criteria.addCriterion(new SimpleCriterion(filterColumn, SimpleCriterion.Operator.CONTAINS, filter, false, lookupFieldDataType(filterColumn)));
        }
    }

    private String toErrorEventColumnJson(String[] columns) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (String column : columns) {
            if(sb.length() > 1)sb.append(',');
            sb.append('{');
            sb.append("key")
                    .append(':')
                    .append("\"")
                    .append(column)
                    .append("\"");
            sb.append(",sortable:true");
            sb.append('}');
        }
        return sb.append(']').toString();
    }

    private String toErrorDetailViewJson(List<ErrorDetailView> errorDetails, String[] columns) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (ErrorDetailView errorDetail : errorDetails) {
            Map<String, String> rowMap = new HashMap<String, String>();
            rowMap.put("ErrorTime", safeString(FormatUtil.formatSqlDateTime(errorDetail.getErrorCreatedAt())));
            rowMap.put("RunId", String.valueOf(errorDetail.getRunId()));
            rowMap.put("Rollup", safeString(errorDetail.getRollup()));
            rowMap.put("RiskGroup", safeString(errorDetail.getRiskGroup()));
            rowMap.put("Batch", safeString(errorDetail.getBatch()));
            rowMap.put("Dictionary", safeString(errorDetail.getDictionary()));
            rowMap.put("MarketData", safeString(errorDetail.getMarketData()));
            rowMap.put("Split", safeString(errorDetail.getSplit()));
            rowMap.put("ErrorMessage", safeString(errorDetail.getErrorMessage()));
            rowMap.put("RunLabel", safeString(errorDetail.getRunLabel()));
            rowMap.put("LaunchEventId", safeString(errorDetail.getLaunchEventId()));
            rowMap.put("ConfigId", safeString(errorDetail.getConfigId()));
            rowMap.put("BusinessDate", String.valueOf(errorDetail.getRunBusinessDate()));
                if(sb.length() > 1)sb.append(',');
            sb.append(rowDataMapToJson(rowMap, columns));
        }
        return sb.append(']').toString();
    }

    private String rowDataMapToJson(Map<String,String> rowData, String[] columns) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (String column : columns) {
            if(sb.length() > 1)sb.append(',');
            sb.append(column);
            sb.append(':');
            sb.append("\"");
            sb.append(rowData.get(column));
            sb.append("\"");
        }
        sb.append('}');
        return sb.toString();
    }
    private String safeString(String value){
        return value == null ? "" : value;
    }

    private int parseDate(String yyyymmdd){
        if( yyyymmdd == null){
            yyyymmdd = FormatUtil.yyyymmdd(new Date());
        }
        return  Integer.parseInt(yyyymmdd);
    }
    
        
    //latest pending error
    String getLatestHash() throws NoSuchAlgorithmException {
        List<ErrorSummaryView> errorSummaryViews =
                errorSummaryViewDao.read(
                        ErrorSummaryView.class,
                        createSummaryCriteria(parseDate(null)));

        return getLatestHash(errorSummaryViews);
    }

    private String getLatestHash(List<ErrorSummaryView> errorSummaryViews) throws NoSuchAlgorithmException {
        return CalcDigest.calcDigest(errorSummaryViews.toString());
    }

    /**
     * map field name to its datatype
     * @param filterColumn
     */
    private SimpleCriterion.DataType lookupFieldDataType(String filterColumn) {
        SimpleCriterion.DataType dataType;
        if (Arrays.asList(("ERROR_ACTION_ID,BUSINESS_DATE,RUN_ID,RUN_BUSINESS_DATE," +
                "ERROR_EVENT_ID,PRICING_ERROR_ID").split(",")).contains( filterColumn.toUpperCase())){
            dataType = SimpleCriterion.DataType.NUMBER;
        }else if (Arrays.asList(("UPDATED_AT,RUN_CREATED_AT,ERROR_CREATED_AT").split(",")).contains( filterColumn.toUpperCase())){
            dataType = SimpleCriterion.DataType.NUMBER;
        }else if (Arrays.asList(("UPDATED_BY,LAUNCH_EVENT_ID,SPLIT,CONFIG_ID,ACTION_COMMENT,RUN_LABEL," +
                "ROLLUP,RISK_GROUP,BATCH,DICTIONARY,MARKET_DATA,ERROR_MESSAGE").split(",")).contains( filterColumn.toUpperCase())){
            dataType = SimpleCriterion.DataType.STRING;
        } else {
            dataType = SimpleCriterion.DataType.UNKNOWN;
        }
        return dataType;
    }


    /* ----------------------------------------------------------------------------------------------
    * simulation functions for POC testing
    * ----------------------------------------------------------------------------------------------
     */
    private void buildDatabase() throws IOException {
        String[] locations = {"classpath:sqlLoader/sqlLoader.spring.xml"};
        ApplicationContext ctx = new ClassPathXmlApplicationContext(locations);
        SqlLoader sqlLoader = ctx.getBean("sqlLoader", SqlLoader.class);
        sqlLoader.load("releases", "views");
    }

    private void saveRandomErrors(int numberToCreate) {
        final PricingRun pricingRun = new PricingRun(System.currentTimeMillis(), "dummy-run", parseDate(FormatUtil.yyyymmdd(new Date())), "some-config", new Date());
        pricingRunDao.save(pricingRun);
        errorEventDao.saveAll(createRandomErrorEvents(numberToCreate, pricingRun.getRunId()));
    }

    List<ErrorEvent> createRandomErrorEvents(int numberToCreate, long runId){

        Random random = new Random();
        String[] batches = new String[]{"monitorApp","monitorApp_LONG","TESTBOOK","CALIB"};
        String[] riskGroups = new String[]{"RISKGROUP_A","RISKGROUP_B","RISKGROUP_C","RISKGROUP_D","RISKGROUP_E","RISKGROUP_F"};
        String[] rollups = new String[]{"3472","3473","3476","3481","3482","3485"};

        List<ErrorEvent> errors = new ArrayList<ErrorEvent>(numberToCreate);
        for (int n = 0; n < numberToCreate; n++) {
            errors.add(createErrorEvent(latestErrorEventId++,
                    batches[random.nextInt(batches.length)],
                    riskGroups[random.nextInt(riskGroups.length)],
                    rollups[random.nextInt(rollups.length)],
                    runId,
                    random.nextInt(20)
            ));
        }

        return errors;
    }

    ErrorEvent createErrorEvent(int id, String batchName, String riskGroupName, String rollupName, long runId, int numberToCreate) {
        ErrorEvent errorEvent = new ErrorEvent(-1,String.valueOf(id),new Date(),runId,rollupName,riskGroupName,batchName,new LinkedList<PricingError>());
        errorEvent.setPricingErrors( createRandomPricingErrors(errorEvent, numberToCreate));
        return errorEvent;
    }

    List<PricingError> createRandomPricingErrors(ErrorEvent errorEvent,int numberToCreate){

        Random random = new Random();
        String[] dicts = new String[]{"PRICE","DELTA","VEGA","THETA","COMPLEX_VEGA"};
        String[] mkts = new String[]{"COB","COB_PRICE","AMEND","INTRA","YEST","AMENDYEST"};
        String[] splits = new String[]{"EUR","USD","EQ1","FTSE","EQ2","OTHER"};
        String[] messages = new String[]{"missing mktdata","broken config","weird dbax thing","weird propCalc thing"};

        List<PricingError> errors = new ArrayList<PricingError>(numberToCreate);
        for (int n = 0; n < numberToCreate; n++) {
            errors.add(createPricingError(
                    dicts[random.nextInt(dicts.length)],
                    mkts[random.nextInt(mkts.length)],
                    splits[random.nextInt(splits.length)],
                    messages[random.nextInt(messages.length)],
                    errorEvent
            ));
        }
        errors.add(createPricingError(
                dicts[random.nextInt(dicts.length)],
                mkts[random.nextInt(mkts.length)],
                splits[random.nextInt(splits.length)],
                "it's broken",
                errorEvent ));

        return errors;
    }

    PricingError createPricingError(String dictionary, String marketData, String split, String errorMessage, ErrorEvent errorEvent) {
        return new PricingError(errorEvent.getErrorEventId(),-1,
                dictionary,marketData,split,errorMessage);
    }

}
