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
        int settle = randomPastDate();
        int maturity = randomFutureDate();
        float notional = random.nextFloat();
        float rate = random.nextFloat();
        TradeType tradeType = TradeType.values()[random.nextInt(7)];
        return new Trade(settle, maturity, notional, rate, tradeType);
    }

    private static int randomFutureDate() {
        return 20150122;
    }

    private static int randomPastDate() {
        return 20140122;
    }

    public static Trade createTrade(int settle, int maturity, float notional, float rate, TradeType tradeType) {
        return new Trade(settle, maturity, notional, rate, tradeType);
    }
}
