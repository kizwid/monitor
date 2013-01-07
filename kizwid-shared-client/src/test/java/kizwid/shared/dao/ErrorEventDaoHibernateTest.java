package kizwid.shared.dao;

import kizwid.sqlLoader.dao.DatabaseReleaseDao;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * User: kizwid
 * Date: 2012-01-30
 */

@Ignore//TODO:until we decide on ORM strategy
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:dao/hibernate-dao.spring.xml"})
public class ErrorEventDaoHibernateTest extends  DatabaseTxTestFixture{

    @Resource
    ErrorEventDaoHibernate errorEventDaoHibernate;

    @Resource
    JdbcTemplate jdbcTemplate;

    @Resource
    DatabaseReleaseDao databaseReleaseDao;

    @Test
    public void canSaveErrors(){

        int rowCountErrorEventBefore = getRowCount("error_event");
        int rowCountPricingErrorBefore = getRowCount("pricing_error");

//        ErrorEvent errorEvent = createErrorEvent(2, "FOO", "BAR", "BAZ", 0, 3);
//        errorEventDaoHibernate.save(errorEvent);
//
//        ErrorEvent check = errorEventDaoHibernate.readById(ErrorEvent.class, errorEvent.getErrorEventId());
//        assertEquals(errorEvent, check);
//
//        assertEquals( "row count not expected: ", rowCountErrorEventBefore + 1, getRowCount("error_event"));
//        assertEquals( "row count not expected: ", rowCountPricingErrorBefore + 3, getRowCount("pricing_error"));
//
//        List<ErrorEvent> retrievedErrorEvent = errorEventDaoHibernate.findByRiskGroup("BAR");
//        for (ErrorEvent event : retrievedErrorEvent) {
//            System.out.println(event);
//        }
//        assertTrue( retrievedErrorEvent.size() == 1);
//        assertTrue( retrievedErrorEvent.get(0).getPricingErrors().size() == 3);

    }

}
