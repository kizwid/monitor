package kizwid.web;

import kizwid.caterr.dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;

/**
 * User: kizwid
 * Date: 2012-02-02
 */
public class DataController extends ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(DataController.class);
    private final static Map<String,Map<String,Integer>> dummyJobStats = new HashMap<String, Map<String,Integer>>();
    private final static Map<String,List<String>> dummyErrors = new HashMap<String,List<String>>();
    private final String env;
    private Map<String,String> miscContext;

    static {
        String[] jobs = new String[]{"group1","group2","group3","group4","group5","group6","group7"};
        for(String s:jobs){
            Map<String,Integer> job = new LinkedHashMap<String, Integer>();
            job.put("completed",0);
            job.put("running",0);
            job.put("queueing",200);
            dummyJobStats.put(s,job);
        }

        dummyErrors.put("some.dummy.error.string.a", Arrays.asList("Error1", "Error2"));
        dummyErrors.put("some.dummy.error.string.b", Arrays.asList("Error11", "Error12"));

    }

    public DataController(JdbcTemplate jdbcTemplate,
                          ErrorEventDao errorEventDao,
                          ErrorActionDao errorActionDao,
                          PricingErrorDao pricingErrorDao,
                          PricingRunDao pricingRunDao,
                          ErrorSummaryViewDao errorSummaryViewDao,
                          ErrorDetailViewDao errorDetailViewDao,
                          Map<String, String> miscContext,
                          String env) {
        super(jdbcTemplate, errorEventDao, errorActionDao,
                pricingErrorDao,pricingRunDao,errorSummaryViewDao, errorDetailViewDao, miscContext);
        this.miscContext = miscContext;
        this.env = miscContext.get("env");
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        PrintWriter out = response.getWriter();
        String action = request.getParameter("Action");
        if( !"LatestId".equals(action)){
            //LatestId can flood log file if lots of users connect
        logger.info("Action [{}] from {}", action, request.getRemoteHost());
        }
//        ModelAndView model = super.handleRequest(request, response);

        if ("GetJobStats".equals(action)) {
            response.setContentType("text/plain");
            String jobStatsJson = toJson(dummyJobStats);
            JobActivitySimulator.updateJobStatus(dummyJobStats);
            out.println(jobStatsJson);
            out.flush();
        }else if("LatestId".equals(action)){
            response.setContentType("text/plain");
            //TODO: use updater thread so that all clients use caterr cache
            out.println(super.getLatestHash());
            out.flush();
        }else if("GetEnvironmentName".equals(action)){
            response.setContentType("text/plain");
            //Context env = (Context)new InitialContext().lookup("java:comp/env");
            //String envName = (String)env.lookup("monitorApp.env");
            out.println(env);
            out.flush();
        }else if("ShowFile".equals(action)){
            response.setContentType("text/plain");
            String filePath = request.getParameter("File");
            File file = new File(filePath);
            if(!file.exists()){
                out.println("samba path: " + filePath.replace("/","\\")); //change to windows(samba) friendly path
                //attempt to substitute samba.root for file.root
                //thus will open file from path mounted on webserver
                //OR print the samba friendly name for our users
                for (Map.Entry<String, String> entry : miscContext.entrySet()) {
                    if(filePath.startsWith(entry.getKey() + "/")){
                        filePath = filePath.replace(entry.getKey(), entry.getValue());
                        file = new File(filePath);
                        break;
        }
                }
            }

            if(file.exists()) {
                BufferedReader br = null;
                try{
                    br = new BufferedReader(new FileReader(file));
                    for(;;){
                        String line = br.readLine();
                        if( line == null) break;
                        out.println(line);
                    }
                }catch (Exception ex){
                    logger.error("failed to read error log {}", file, ex);
                }finally {
                    try{if(br != null) br.close();}catch(Exception e){/*swallow*/}
                }
            }else{
                out.println("file not found: " + filePath);
            }

            out.flush();
        }

        return null;
    }


    private String toJson(Map<String, Map<String, Integer>> dummyJobStats) {
        String jobData = "";
        for(String job: dummyJobStats.keySet()){
            if(jobData.length() > 0) jobData += ",";
            jobData += "{ ";
            jobData += "\"jobSet\":" + "\"" + job + "\",";
            boolean first = true;
            for(String p: dummyJobStats.get(job).keySet()){
                if(!first){
                    jobData += ", ";
                }
                jobData += "\"" + p + "\": " + dummyJobStats.get(job).get(p);
                first = false;
            }
            jobData += "}";
        }

        //logger.info("jobData: {}", jobData);

        String testData = "{\"aChartData\":" +
                "[" +
                jobData +
                " ]" +
                ", \"sTimeStamp\": \"" + new Date() + "\"" +
                ", \"sEvent\": \"simulated activity...\"" +
                "}";

        logger.info("testData: {}", testData);
        return testData;
    }

    private static class JobActivitySimulator{

        public static void updateJobStatus(Map<String,Map<String,Integer>> jobStats) {
            Random random = new Random();
            List<String> jobNames = new LinkedList<String>(jobStats.keySet());
            int numOfSims = random.nextInt(10);
            for(int sim = 0; sim < numOfSims; sim++) {
                Map<String,Integer> job = jobStats.get(jobNames.get(random.nextInt(jobNames.size())));
                simulateActivity(job, random.nextInt(3), random.nextInt(20));
            }
        }

        private static void simulateActivity(Map<String, Integer> job, int operation, int amount) {
            int q = job.get("queueing");
            int r = job.get("running");
            int c = job.get("completed");

            int x;
            switch (operation){
                case 0: //move jobs from running to completed
                    x = Math.min(amount, r);
                    c += x;
                    r -= x;
                    break;
                case 1: //move jobs from queueing to running
                default:
                    x = Math.min(amount, q);
                    q -= x;
                    r += x;
                    break;
            }
            job.put("completed",c);
            job.put("running",r);
            job.put("queueing", q);
        }

    }
}
