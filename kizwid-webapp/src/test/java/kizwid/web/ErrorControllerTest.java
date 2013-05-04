package kizwid.web;

import kizwid.sqlLoader.SqlLoader;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.AfterClass;
import org.junit.Before;
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

import javax.annotation.Resource;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;

/**
 * User: kizwid
 * Date: 2012-02-06
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:sqlLoader/sqlLoader.spring.xml"})
public class ErrorControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(ErrorControllerTest.class);
    public static final int PORT = 8090;
    public static final String HOST = "localhost";
    public static final String CONTEXTPATH = "/monitorApp";
    public static final String URL_BASE = "http://" + HOST + ":" + PORT + CONTEXTPATH +"/service/errors?";
    private Executor executor;
    private static ErrorControllerTest.WebServer webServer;
    private StringWriter sw;

    @Resource
    private SqlLoader sqlLoader;


    @Before
    public void setUp() throws InterruptedException, IOException {

        sqlLoader.load("releases", "views");

        //just start for 1st test (or reuse running instance if using jetty:run)
        if(checkPort(PORT)){
            executor = Executors.newSingleThreadExecutor();
            CountDownLatch latch = new CountDownLatch(1);
            webServer = new WebServer(latch);
            executor.execute(webServer);
            latch.await();
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
        assertTrue(page.indexOf("<title>Monitor Dashboard</title>") >= 0);

    }


    @Test
    public void runForever() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
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
