package kizwid.caterr.dao;

import kizwid.caterr.domain.PricingError;
import kizwid.shared.dao.discriminator.SimpleCriteria;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;

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
        pricingError.setId(-1); //only works when this is the first entry
        pricingError.setDictionary("DICT");
        pricingError.setMarketData("MKT");
        pricingError.setSplit("SPLIT");
        pricingError.setErrorMessage("error message");

        pricingErrorDao.save(pricingError);
        for (PricingError error : pricingErrorDao.find(SimpleCriteria.EMPTY_CRITERIA)) {
            System.out.println(error);
        }
        PricingError check = pricingErrorDao.findById(pricingError.getId());

        Assert.assertEquals(pricingError, check);

    }


}
