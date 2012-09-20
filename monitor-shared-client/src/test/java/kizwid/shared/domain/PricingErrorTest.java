package kizwid.shared.domain;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * User: kizwid
 * Date: 2012-01-30
 */
public class PricingErrorTest {

    private static final Logger logger = LoggerFactory.getLogger(PricingErrorTest.class);

    @Test
    public void checkEqualsContract(){

        PricingError one = createPricingError("d","m","s","boo",1);
        PricingError two = createPricingError("d","m","s","boo",1);
        assertEquals(one,two);
        assertEquals(one.hashCode(),two.hashCode());
        Set<PricingError> setOne = new LinkedHashSet<PricingError>();
        setOne.add(one);
        setOne.add(two);
        Set<PricingError> setTwo = new LinkedHashSet<PricingError>();
        setTwo.add(one);
        setTwo.add(two);
        assertEquals(setOne, setTwo);
        assertEquals(setOne.hashCode(), setTwo.hashCode());

        logger.info("PricingErrorTest is {}", "OK");

    }

    private PricingError createPricingError(String dictionary, String marketData, String split, String errorMessage, int errorEventId) {
        PricingError pricingError = new PricingError();
        pricingError.setDictionary(dictionary);
        pricingError.setMarketData(marketData);
        pricingError.setSplit(split);
        pricingError.setErrorMessage(errorMessage);
        pricingError.setErrorEventId(errorEventId);
        return pricingError;
    }

}
