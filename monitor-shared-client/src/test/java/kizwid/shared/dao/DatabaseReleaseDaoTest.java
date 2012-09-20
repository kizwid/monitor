package kizwid.shared.dao;

import kizwid.shared.domain.database.release.DatabaseRelease;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import javax.annotation.Resource;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: kizwid
 * Date: 2012-01-31
 */
public class DatabaseReleaseDaoTest extends DatabaseTxTestFixture{

    @Resource
    DatabaseReleaseDao databaseReleaseDao;

    @Test
    @Rollback(true)
    public void canAddRowToDatabaseReleaseTable(){
        int sizeBefore = jdbcTemplate.queryForInt("select count(*) from database_release");
        DatabaseRelease databaseRelease = new DatabaseRelease("foo", new Date());
        databaseReleaseDao.save(databaseRelease);
        assertTrue(jdbcTemplate.queryForInt("select count(*) from database_release") == sizeBefore + 1);
        DatabaseRelease check = databaseReleaseDao.findByScript(DatabaseRelease.class, "foo");
        assertEquals(databaseRelease, check);
    }


}
