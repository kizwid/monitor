package kizwid.caterr.dao;

import kizwid.caterr.domain.ErrorEvent;
import kizwid.caterr.domain.PricingError;
import kizwid.shared.dao.discriminator.SimpleCriteria;
import kizwid.sqlLoader.SqlLoader;
import kizwid.sqlLoader.dao.DatabaseReleaseDao;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: kizwid
 * Date: 2012-02-01
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:caterr/dao.spring.xml"
        ,"classpath:sqlLoader/sqlLoader.spring.xml"
})
public abstract class DatabaseTxTestFixture extends AbstractTransactionalJUnit4SpringContextTests {

    private final Logger logger = LoggerFactory.getLogger(DatabaseTxTestFixture.class);

    @Resource protected JdbcTemplate jdbcTemplate;
    @Resource protected DatabaseReleaseDao databaseReleaseDao;
    @Resource protected SqlLoader sqlLoader;

    private int rowCountErrorEventBefore;
    private int rowCountPricingErrorBefore;
    private int rowCountErrorActionBefore;
    private int rowCountErrorActionPricingErrorBefore;

    @BeforeTransaction
    public void createTxnExpectation() throws IOException {
        sqlLoader.load("releases","views");
        assertTrue(databaseReleaseDao.find(SimpleCriteria.EMPTY_CRITERIA).size() > 1);

        rowCountErrorEventBefore = getRowCount("error_event");
        rowCountPricingErrorBefore = getRowCount("pricing_error");
        rowCountErrorActionBefore = getRowCount("error_action");
        rowCountErrorActionPricingErrorBefore = getRowCount("error_action_pricing_error");

    }

    @AfterTransaction
    public void verifyTxnExpectation() {
        assertEquals(rowCountErrorEventBefore, getRowCount("error_event"));
        assertEquals(rowCountPricingErrorBefore, getRowCount("pricing_error"));
        assertEquals(rowCountErrorActionBefore, getRowCount("error_action"));
        assertEquals(rowCountErrorActionPricingErrorBefore, getRowCount("error_action_pricing_error"));
            }

    int getRowCount(final String table) {
        return jdbcTemplate.queryForInt("select count(*) from " + table);
    }

    protected ErrorEvent createErrorEvent(String launchEventId, String batchName, String riskGroupName,
                                String rollupName, long runId, int numberToCreate) {
        ErrorEvent errorEvent = new ErrorEvent(launchEventId,new Date(),runId,
                rollupName,riskGroupName,batchName);
        errorEvent.setPricingErrors( createRandomPricingErrors(errorEvent, numberToCreate));
        return errorEvent;
    }

    protected List<PricingError> createRandomPricingErrors(ErrorEvent errorEvent,int numberToCreate){

        Random random = new Random();
        String[] dicts = new String[]{"PRICE","DELTA","VEGA","THETA","COMPLEX_VEGA"};
        String[] mkts = new String[]{"COB","COB_PRICE","AMEND","INTRA","YEST","AMENDYEST"};
        String[] splits = new String[]{"EUR","USD","EQ1","FTSE","EQ2","OTHER"};
        String[] messages = new String[]{"missing mktdata","broken config","weird dbax thing","weird propCalc thing"};

        List<PricingError> errors = new ArrayList<PricingError>(numberToCreate);
        for (int n = 0; n < numberToCreate; n++) {
            errors.add(createPricingError(
                    dicts[random.nextInt(dicts.length)],
                    mkts[random.nextInt(mkts.length)],
                    splits[random.nextInt(splits.length)],
                    messages[random.nextInt(messages.length)]
            ));
        }

        return errors;
    }

    protected PricingError createPricingError(String dictionary, String marketData, String split, String errorMessage) {
        PricingError pricingError = new PricingError(dictionary, marketData, split, errorMessage);
        return pricingError;
    }

}
