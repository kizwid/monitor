package kizwid.datastore;

import kizwid.datastore.biz.Trade;
import kizwid.datastore.biz.TradeType;
import kizwid.util.FormatUtil;

import java.text.ParseException;
import java.util.Date;
import java.util.Random;

/**
 * Created by kevin on 24/08/2014.
 */
public class TradeFixture {

    public static Trade createRandomTrade(){

        Random random = new Random();
        Date settle = randomPastDate();
        Date maturity = randomFutureDate();
        float notional = random.nextFloat();
        float rate = random.nextFloat();
        TradeType tradeType = TradeType.values()[random.nextInt((int)7)];
        return new Trade(settle, maturity, notional, rate, tradeType);
    }

    private static Date randomFutureDate() {
        try {
            return FormatUtil.yyyymmddToDate("20150122");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static Date randomPastDate() {
        try {
            return FormatUtil.yyyymmddToDate("20140122");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
