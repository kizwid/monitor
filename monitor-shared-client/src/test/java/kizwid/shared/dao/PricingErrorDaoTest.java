package kizwid.shared.dao;

import kizwid.shared.dao.discriminator.SimpleCriteria;
import kizwid.shared.domain.PricingError;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;

/**
 * User: kizwid
 * Date: 2012-01-30
 */
public class PricingErrorDaoTest extends DatabaseTxTestFixture{

    @Resource
    PricingErrorDao pricingErrorDao;

    @Test
    public void canSaveAndRetrieve(){

        PricingError pricingError = new PricingError();
        pricingError.setErrorEventId(-1);
        pricingError.setPricingErrorId(-1); //only works when this is the first entry
        pricingError.setDictionary("DICT");
        pricingError.setMarketData("MKT");
        pricingError.setSplit("SPLIT");
        pricingError.setErrorMessage("error message");

        pricingErrorDao.save(pricingError);
        for (PricingError error : pricingErrorDao.read(PricingError.class, SimpleCriteria.EMPTY_CRITERIA)) {
            System.out.println(error);
        }
        PricingError check = pricingErrorDao.readById(PricingError.class, pricingError.getPricingErrorId());

        assertEquals(pricingError, check);

    }


}
