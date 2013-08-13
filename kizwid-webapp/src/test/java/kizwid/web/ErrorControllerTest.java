package kizwid.web;

import kizwid.caterr.dao.ErrorEventDao;
import kizwid.caterr.dao.PricingRunDao;
import kizwid.caterr.domain.ErrorEvent;
import kizwid.caterr.domain.PricingError;
import kizwid.caterr.domain.PricingRun;
import kizwid.shared.database.AbstractDatabaseTest;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;

/**
 * User: kizwid
 * Date: 2012-02-06
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:caterr/dao.spring.xml"
})
@TransactionConfiguration(defaultRollback=true)
@Transactional
public class ErrorControllerTest extends AbstractDatabaseTest{

    private static final Logger logger = LoggerFactory.getLogger(ErrorControllerTest.class);
    public static final int PORT = 8090;
    public static final String HOST = "localhost";
    public static final String CONTEXTPATH = "/monitorApp";
    public static final String URL_BASE = "http://" + HOST + ":" + PORT + CONTEXTPATH +"/service/errors?";
    private Executor executor;
    private static ErrorControllerTest.WebServer webServer;
    private StringWriter sw;

    @Resource private PricingRunDao pricingRunDao;
    @Resource private ErrorEventDao errorEventDao;


    @Before
    public void setUp() throws IOException, SQLException, URISyntaxException {

        //just start for 1st test (or reuse running instance if using jetty:run)
        if(checkPort(PORT)){
            executor = Executors.newSingleThreadExecutor();
            CountDownLatch latch = new CountDownLatch(1);
            webServer = new WebServer(latch);
            executor.execute(webServer);
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            System.out.println("****** started webserver **********");
        }

    }

    @AfterClass
    public static void tearDown() throws Exception {
        if(webServer != null)
        webServer.stop();//only stop after all tests have completed

        //capture final state of database
        Class driverClass = Class.forName("org.hsqldb.jdbcDriver");
        Connection jdbcConnection = DriverManager.getConnection(
                "jdbc:hsqldb:mem:monitorApp", "sa", "");
        IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);

        // full database export
        IDataSet fullDataSet = connection.createDataSet();
        FlatXmlDataSet.write(fullDataSet, new FileOutputStream("target/full-dataset.xml"));

    }

    @Test
    public void canLoadDashboard() throws Exception {

        sw = new StringWriter();
        get(URL_BASE + "Action=Dashboard&User=unitTest", new PrintWriter(sw));
        String page = sw.toString().trim();
        logger.info(page.substring(0, Math.min(500, page.length())));
        assertTrue(page.contains("<title>Monitor Dashboard</title>"));
        assertTrue(page.contains("Filtered item count: 0"));

/*
        final WebClient webClient = new WebClient();
        final HtmlPage page = webClient.getPage(URL_BASE + "Action=Dashboard&User=unitTest");
        Assert.assertEquals("Monitor Dashboard", page.getTitleText());

        final String pageAsXml = page.asXml();
        Assert.assertTrue(pageAsXml.contains("Filtered item count: 0"));

        webClient.closeAllWindows();
*/

    }

    @Test
    @Ignore("not compatible with transactional tear down of test data")
    public void canFilterByErrorMessage() throws Exception {


        //TODO: load test data
        PricingRun pricingRun = new PricingRun(0,"dummy",20130518,"foo",new Date());
        PricingError pricingError = new PricingError("price","COB","ByEquity","something bad");
        List<PricingError> pricingErrors = new ArrayList<PricingError>();
        pricingErrors.add(new PricingError("PRICE","COB","ByEquity","something bad #1"));
        pricingErrors.add(new PricingError("DELTA","COB","ByCredit","something bad #2"));
        pricingErrors.add(new PricingError("VEGA","COB","ByCurrency","something bad ~3"));
        ErrorEvent errorEvent = new ErrorEvent(-1L,"1",new Date(),0L,"rr","rg","b", pricingErrors);

        pricingRunDao.save(pricingRun);
        errorEventDao.save(errorEvent);

        sw = new StringWriter();
        get(URL_BASE + "Action=Apply+filter&FilterColumn=error_message&Filter=2&User=unitTest", new PrintWriter(sw));
        String page = sw.toString().trim();
        logger.info(page.substring(0, Math.min(500, page.length())));
        assertTrue(page.contains("Filtered item count: 1"));

/*
        final WebClient webClient = new WebClient();
        HtmlPage page = webClient.getPage(URL_BASE + "Action=Apply+filter&FilterColumn=error_message&Filter=2&User=unitTest");
        Assert.assertTrue(page.asXml().contains("Filtered item count: 1"));

        page = webClient.getPage(URL_BASE + "Action=Apply+filter&FilterColumn=error_message&Filter=bad&User=unitTest");
        Assert.assertTrue(page.asXml().contains("Filtered item count: 3"));
*/

    }


    @Test
    public void runForever() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        sw = new StringWriter();
        get(URL_BASE + "Action=Simulate&User=unitTest", new PrintWriter(sw));
        logger.info(URL_BASE + "Action=Simulate&User=unitTest");
        //latch.await();
    }


    public static void get(String sUrl, PrintWriter pw) throws Exception {
        URL url = new URL(sUrl);
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

        for (; ; ) {
            String sLine = br.readLine();
            if (sLine == null) break;
            pw.println(sLine);
        }
        br.close();
        pw.flush();
    }

    public class WebServer implements Runnable {
        private final CountDownLatch latch;
        private final Server server;

        public WebServer(CountDownLatch latch) {
            this.latch = latch;
            server = new Server();
        }

        public void run() {

            try {
                SelectChannelConnector connector = new SelectChannelConnector();
                connector.setPort(PORT);
                server.addConnector(connector);

                final String WEBAPPDIR = "src/main/webapp";

                //TODO: use reliable path to webAppPath
                //final URL warUrl = Thread.currentThread().getContextClassLoader().getResource(WEBAPPDIR);
                File testClasses = new File(URLDecoder.decode(ClassLoader.getSystemClassLoader().getResource(".").getFile()));
                File base = testClasses.getParentFile().getParentFile();
                File webAppDir = new File(base + File.separator + WEBAPPDIR);
                final String webAppPath = webAppDir.getAbsolutePath();

                //final URL warUrl = new URL("file://" + webAppDir.getAbsolutePath());
                //final URL warUrl = this.getClass().getResource(WEBAPPDIR);
                //final String webAppPath = warUrl.toExternalForm();
                //final WebAppContext webAppContext = new WebAppContext(webAppPath, CONTEXTPATH);

                ResourceHandler resourceHandler = new ResourceHandler();
                resourceHandler.setWelcomeFiles(new String[]{"index.html"});
                resourceHandler.setResourceBase(webAppPath);

                WebAppContext webAppContext = new WebAppContext();
                logger.info("setting webapp descriptor path = " + webAppPath + "/WEB-INF/web.xml");
                webAppContext.setDescriptor(webAppPath + "/WEB-INF/web.xml");
                webAppContext.setResourceBase(webAppPath);
                webAppContext.setContextPath(CONTEXTPATH);
                webAppContext.setParentLoaderPriority(true);


                HandlerList handlers = new HandlerList();
                handlers.setHandlers(new Handler[]{resourceHandler, webAppContext, new DefaultHandler()});
                server.setHandler(handlers);

                server.start();
                latch.countDown();//notify caller
                server.join();
                server.stop();
                logger.info("server stopped");
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        public void stop() throws Exception {
            server.stop();
        }
    }

    //sometimes we run this using jetty:run
    private boolean checkPort(int port) {
        boolean success = false;
        try {
            Server server = new Server(port);
            server.start();
            success = true;
            server.stop();
            server = null;
        }   catch (Exception e){
            success=false;
        }
        return success;
    }


}
