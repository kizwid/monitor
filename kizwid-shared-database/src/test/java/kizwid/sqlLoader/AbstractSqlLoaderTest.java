package kizwid.sqlLoader;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: kevsanders
 * Date: 07/01/2013
 * Time: 17:05
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:sqlLoader/sqlLoader.spring.xml"})
public abstract class AbstractSqlLoaderTest {

    @Resource
    private SqlLoader sqlLoader;

    @Resource
    protected DataSource dataSource;

    protected JdbcTemplate jdbcTemplate;

    private static Connection conn = null;

    @Before
    public void setUp() throws IOException, SQLException {
        sqlLoader.load("releases");
        jdbcTemplate = new JdbcTemplate(dataSource);
        conn = dataSource.getConnection();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        //capture final state of database
        IDatabaseConnection connection = new DatabaseConnection(conn);
        // full database export
        IDataSet fullDataSet = connection.createDataSet();
        FlatXmlDataSet.write(fullDataSet, new FileOutputStream("full-dataset.xml"));
    }
}
