package kizwid.caterr.dao;

import kizwid.caterr.domain.ErrorAction;
import kizwid.caterr.domain.PricingError;
import kizwid.shared.dao.PrimaryKey;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: kizwid
 * Date: 2012-02-02
 */
public class ErrorActionDaoTest extends DatabaseTxTestFixture{

    @Resource
    ErrorActionDao errorActionDao;

    @Resource
    ErrorEventDao errorEventDao;

    @Resource
    PricingErrorDao pricingErrorDao;

    private int rowCountErrorEventBefore;
    private int rowCountPricingErrorBefore;
    private int rowCountErrorActionBefore;
    private int rowCountErrorActionPricingErrorBefore;

    @Test
    @Rollback(true)
    public void canSaveAndRetrieveErrorAction(){

        //create a few errors
        errorEventDao.save(createErrorEvent("a", "FOO", "BAR", "BAZ", 0, 6));

        //create an error action
        ErrorAction firstErrorAction = new ErrorAction(20120202, "kizwid", new Date(), "test1");
        errorActionDao.save(firstErrorAction);

        //verify action was created
        ErrorAction check = errorActionDao.findById(createErrorActionPk(firstErrorAction));
        Assert.assertEquals(firstErrorAction, check);

        String filter = "";

        //attach unactioned errors to 1st action
        List<PricingError> errors = pricingErrorDao.findUnactioned(filter);
        assertTrue(errors.size() == 6);
        firstErrorAction.setPricingErrors(errors);
        errorActionDao.save(firstErrorAction);

        //now add more errors
        errorEventDao.save(createErrorEvent("b", "BUZ", "BAR", "BAZ", 0, 2));
        Assert.assertEquals(2, pricingErrorDao.findUnactioned(filter).size());
        
        List<ErrorAction> errorActionsToday =
                errorActionDao.findByBusinessDate(20120202);
        System.out.println("errorActionsToday: " + errorActionsToday);
        

        //and attach to another action
        ErrorAction secondErrorAction = new ErrorAction(20120202, "kizwid", new Date(), "test2");
        errorActionDao.save(secondErrorAction);
        secondErrorAction.setPricingErrors(pricingErrorDao.findUnactioned(filter));
        errorActionDao.save(secondErrorAction);

        //verify the correct errors are attached to each action
        //System.out.println(errorActionDao.read(ErrorAction.class, SimpleCriteria.EMPTY_CRITERIA));
        assertEquals(6, jdbcTemplate.queryForInt("select count(*) from error_action_pricing_error where error_action_id = " + firstErrorAction.getId()));
        assertEquals(2, jdbcTemplate.queryForInt("select count(*) from error_action_pricing_error where error_action_id = " + secondErrorAction.getId()));


        //now add more errors with filter
        filter="dbax";
        errorEventDao.save(createErrorEvent("d", "BUZ", "BAR", "BAZ", 0, 10));
        Assert.assertEquals(10, pricingErrorDao.findUnactioned("").size());

        final List<PricingError> filteredErrors = pricingErrorDao.findUnactioned(filter);
        assertTrue(filteredErrors.size() <= 10);
        
        ErrorAction filteredAction = new ErrorAction(20120202,"kevin",new Date(),"xxx");
        filteredAction.setPricingErrors(filteredErrors);
        errorActionDao.save(filteredAction);

        //verify it was save correctly
        ErrorAction checkFilter = errorActionDao.findById(createErrorActionPk(filteredAction));
        Assert.assertEquals(filteredAction, checkFilter);


        //check error actions: should have mixture of commented (Actioned) and non-commented ones
        List<ErrorAction> summary = errorActionDao.findByBusinessDate(20120202);
        System.out.println("summary: " + summary);

    }

    private PrimaryKey createErrorActionPk(final ErrorAction errorAction) {
        return new PrimaryKey() {
                @Override
                public String[] getFields() {
                    return new String[]{"error_action_id"};
                }

                @Override
                public Object[] getValues() {
                    return new Object[]{errorAction.getId()};
                }
            };
    }

}
