package kizwid.sqlLoader;

import kizwid.shared.dao.discriminator.SimpleCriteria;
import kizwid.shared.database.AbstractDatabaseTest;
import kizwid.sqlLoader.dao.DatabaseReleaseDao;
import kizwid.sqlLoader.domain.DatabaseRelease;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * User: kizwid
 * Date: 2012-02-01
 */
public class SqlLoaderTest
        extends AbstractDatabaseTest
{

    @Resource
    DatabaseReleaseDao databaseReleaseDao;

    @Test
    @Rollback(true)
    public void canLoadReleaseScripts() throws IOException, URISyntaxException, SQLException {

        List<DatabaseRelease> releases = databaseReleaseDao.find(SimpleCriteria.EMPTY_CRITERIA);
        for (DatabaseRelease release : releases) {
            System.out.println(release);
        }
        assertTrue(releases.size() >= 1);

    }

}
