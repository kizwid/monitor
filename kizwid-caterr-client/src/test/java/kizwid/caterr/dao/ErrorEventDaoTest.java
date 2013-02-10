package kizwid.caterr.dao;

import kizwid.caterr.domain.ErrorEvent;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.annotation.Rollback;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.*;

/**
 * User: kizwid
 * Date: 2012-01-30
 */
public class ErrorEventDaoTest extends DatabaseTxTestFixture{

    @Resource
    ErrorEventDao errorEventDao;

    @Test
    @Rollback(true)
    public void canSaveAndRetrieve(){
        ErrorEvent errorEvent = createErrorEvent("a", "FOO", "BAR", "BAZ", 0, 2);
        errorEventDao.save(errorEvent);
        ErrorEvent check = errorEventDao.findById(errorEvent.getPrimaryKey());
        Assert.assertEquals(errorEvent, check);
    }

    @Test
    @Rollback(true)
    public void canSaveErrors(){

        int rowCountErrorEventBefore = getRowCount("error_event");
        int rowCountPricingErrorBefore = getRowCount("pricing_error");

        ErrorEvent errorEvent = createErrorEvent("a", "FOO", "BAR", "BAZ", 0, 3);
        errorEventDao.save(errorEvent);

        ErrorEvent check = errorEventDao.findById(errorEvent.getPrimaryKey());
        Assert.assertEquals(errorEvent, check);
        Assert.assertEquals(errorEvent.getErrorEventId(), check.getErrorEventId());
        Assert.assertEquals(errorEvent.getPricingErrors().size(), check.getPricingErrors().size());

        assertEquals( "row count not expected: ", rowCountErrorEventBefore + 1, getRowCount("error_event"));
        assertEquals( "row count not expected: ", rowCountPricingErrorBefore + 3, getRowCount("pricing_error"));

        //TODO: use Criteria instead
        List<ErrorEvent> retrievedErrorEvent = errorEventDao.findByRiskGroup("BAR");
        for (ErrorEvent event : retrievedErrorEvent) {
            System.out.println(event);
        }
        assertTrue( retrievedErrorEvent.size() == 1);
        assertTrue( retrievedErrorEvent.get(0).getPricingErrors().size() == 3);


        //TODO: use Criteria instead
        List<ErrorEvent> unactioned = errorEventDao.findUnActioned("");
        assertTrue( unactioned.size() == 1);
        assertTrue( unactioned.get(0).getPricingErrors().size() == 3);

    }


    @Test
    @Rollback(true)
    public void canLookupByLaunchEventId() {

        ErrorEvent errorEvent = createErrorEvent("a", "FOO", "BAR", "BAZ", 0, 1);
        errorEventDao.save(errorEvent);
        ErrorEvent check = errorEventDao.findById(errorEvent.getPrimaryKey());
        Assert.assertEquals(errorEvent, check);

        long errorEventId = errorEventDao.lookupErrorEventIdFromLaunchEventId(errorEvent.getLaunchEventId());
        Assert.assertEquals(errorEvent.getErrorEventId(), errorEventId);
        
            }

    @Test
    @Rollback(true)
    public void willReturnNegativeOneIfLaunchIdNotFound() {
        long errorEventId = errorEventDao.lookupErrorEventIdFromLaunchEventId("*non-existant-launch-event-id*");
        assertEquals(-1, errorEventId);
    }

    @Test(expected = DuplicateKeyException.class)
    @Rollback(true)
    public void cantSave2ErrorEventsWithSameLaunchEventId() {
        ErrorEvent first = createErrorEvent("a", "FOO", "BAR", "BAZ", 0, 1);
        errorEventDao.save(first);
        ErrorEvent second = createErrorEvent("a", "FOO", "BAR", "BAZ", 0, 1);
        errorEventDao.save(second);
        fail();
    }

}
