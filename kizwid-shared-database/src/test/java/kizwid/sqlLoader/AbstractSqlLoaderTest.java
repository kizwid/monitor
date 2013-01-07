package kizwid.sqlLoader;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;

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

    @Before
    public void setUp() throws IOException {
        sqlLoader.load("releases");
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

}
