package kizwid.sqlLoader.dao;

import kizwid.shared.dao.discriminator.SimpleCriteria;
import kizwid.shared.database.AbstractDatabaseTest;
import kizwid.sqlLoader.domain.DatabaseRelease;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: kizwid
 * Date: 2012-01-31
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:sqlLoader/sqlLoader.spring.xml"
})
@TransactionConfiguration(defaultRollback=true)
@Transactional
public class DatabaseReleaseDaoTest extends AbstractDatabaseTest {

    @Resource
    DatabaseReleaseDao databaseReleaseDao;

    @Test
    @Rollback(true)
    public void canAddRowToDatabaseReleaseTable(){
        List<DatabaseRelease> releases = databaseReleaseDao.find(SimpleCriteria.EMPTY_CRITERIA);
        int sizeBefore = releases.size();
        DatabaseRelease databaseRelease = new DatabaseRelease("/foo.sql", new Date());
        databaseReleaseDao.save(databaseRelease);
        releases = databaseReleaseDao.find(SimpleCriteria.EMPTY_CRITERIA);
        assertTrue(releases.size() == sizeBefore + 1);
        DatabaseRelease check = databaseReleaseDao.findById(databaseRelease.getId());
        assertEquals(databaseRelease, check);
    }


}
