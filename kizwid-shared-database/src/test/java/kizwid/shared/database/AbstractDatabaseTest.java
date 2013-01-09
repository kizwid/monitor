package kizwid.shared.database;

import kizwid.shared.util.EncryptedPropertyPlaceholderConfigurer;
import org.dbmaintain.DbMaintainer;
import org.dbmaintain.MainFactory;
import org.dbmaintain.structure.clean.DBCleaner;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:sqlLoader/jdbc.spring.xml"})
public class AbstractDatabaseTest {

    @Resource
    protected DataSource dataSource;

    @Resource
    protected EncryptedPropertyPlaceholderConfigurer commonProperties;

    protected static DbMaintainer dbMaintainer;
    protected static DBCleaner dbCleaner;
    protected static MainFactory dbMaintainMainFactory;

    private static Connection conn = null;

    protected void initDataBase() throws URISyntaxException, IOException, SQLException {
        createDBMainainer().updateDatabase(false);
        createDBCleaner().cleanDatabase();
    }

    private MainFactory createDBMaintainMainFactory() throws URISyntaxException, IOException, SQLException {
        if (dbMaintainMainFactory == null) {
            URL resource = ClassLoader.getSystemClassLoader().getResource("dbmaintain.properties");
            File file = new File(resource.toURI());
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            commonProperties.processProperties(properties);//resolve placeholders
            dbMaintainMainFactory = new MainFactory(properties);
            conn = dataSource.getConnection();
        }
        return dbMaintainMainFactory;
    }

    private DBCleaner createDBCleaner() throws URISyntaxException, IOException, SQLException {
        createDBMaintainMainFactory();
        dbCleaner = dbMaintainMainFactory.createDBCleaner();
        return dbCleaner;
    }

    private DbMaintainer createDBMainainer() throws URISyntaxException, IOException, SQLException {
        createDBMaintainMainFactory();
        dbMaintainer = dbMaintainMainFactory.createDbMaintainer();
        return dbMaintainer;
    }

    @Test
    public void testInit() throws SQLException, IOException, URISyntaxException {

        initDataBase();

        Connection connection = dataSource.getConnection();
        {
            PreparedStatement statement = connection.prepareStatement("SELECT sysdate FROM dual");
            boolean execute = statement.execute();
            Assert.assertTrue(execute);
        }
        //
        {
            PreparedStatement statement = connection.prepareStatement("SELECT count(*) FROM error_event");
            boolean execute = statement.execute();
            Assert.assertTrue(execute);
        }

        connection.commit();
    }


    @AfterClass
    public static void tearDown() throws Exception {
        //capture final state of database
        IDatabaseConnection connection = new DatabaseConnection(conn);
        // full database export
        IDataSet fullDataSet = connection.createDataSet();
        FlatXmlDataSet.write(fullDataSet, new FileOutputStream("target/full-dataset-dbmaintain.xml"));
    }

}
