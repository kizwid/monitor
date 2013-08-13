package kizwid.caterr.dao;

import kizwid.caterr.domain.ErrorEvent;
import kizwid.caterr.domain.PricingError;
import kizwid.shared.database.AbstractDatabaseTest;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * User: kizwid
 * Date: 2012-02-01
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:caterr/dao.spring.xml"
})
@TransactionConfiguration(defaultRollback=true)
@Transactional
public abstract class DatabaseTxTestFixture extends AbstractDatabaseTest {

    private final Logger logger = LoggerFactory.getLogger(DatabaseTxTestFixture.class);
    //@Resource protected JdbcTemplate jdbcTemplate;

    protected ErrorEvent createErrorEvent(String launchEventId, String batchName, String riskGroupName,
                                String rollupName, long runId, int numberToCreate) {
        ErrorEvent errorEvent = new ErrorEvent(launchEventId,new Date(),runId,
                rollupName,riskGroupName,batchName);
        errorEvent.setPricingErrors( createRandomPricingErrors(numberToCreate));
        return errorEvent;
    }

    protected List<PricingError> createRandomPricingErrors(int numberToCreate){

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
        return new PricingError(dictionary, marketData, split, errorMessage);
    }

    protected int getRowCount(final String table) {
        return jdbcTemplate.queryForInt("select count(*) from " + table);
    }

}
