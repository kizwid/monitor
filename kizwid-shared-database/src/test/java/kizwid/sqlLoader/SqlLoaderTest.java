package kizwid.sqlLoader;

import kizwid.shared.dao.discriminator.SimpleCriteria;
import kizwid.sqlLoader.dao.DatabaseReleaseDao;
import kizwid.sqlLoader.domain.DatabaseRelease;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * User: kizwid
 * Date: 2012-02-01
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:sqlLoader/sqlLoader.spring.xml"})
public class SqlLoaderTest extends AbstractSqlLoaderTest{

    @Resource
    DatabaseReleaseDao databaseReleaseDao;

    @Test
    @Rollback(true)
    public void canLoadReleaseScripts() throws IOException {

        List<DatabaseRelease> releases = databaseReleaseDao.find(SimpleCriteria.EMPTY_CRITERIA);
        for (DatabaseRelease release : releases) {
            System.out.println(release);
        }
        assertTrue(releases.size() >= 1);

    }

}