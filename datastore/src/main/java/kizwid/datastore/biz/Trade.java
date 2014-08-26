package kizwid.datastore.biz;

import kizwid.datastore.NaturalKeyFactory;
import kizwid.util.FormatUtil;

import java.util.Arrays;
import java.util.Date;

import static kizwid.datastore.Const.*;

/**
 * Created by kevin on 24/08/2014.
 */
public class Trade {

    private final int settleDate;
    private final int maturityDate;
    private final float notional;
    private final float rate;
    private final TradeType tradeType;
    private final transient String name;
    private final transient byte[] naturalKey;

    public Trade(int settleDate, int maturityDate, float notional, float rate, TradeType tradeType) {
        this.settleDate = settleDate;
        this.maturityDate = maturityDate;
        this.notional = notional;
        this.rate = rate;
        this.tradeType = tradeType;
        this.name = resolveName();
        this.naturalKey = NaturalKeyFactory.create(toSdos());
    }

    public int getSettleDate() {
        return settleDate;
    }

    public int getMaturityDate() {
        return maturityDate;
    }

    public float getNotional() {
        return notional;
    }

    public float getRate() {
        return rate;
    }

    public TradeType getTradeType() {
        return tradeType;
    }

    public String getName() {
        return name;
    }

    public byte[] getNaturalKey() {
        return naturalKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Trade trade = (Trade) o;

        if (!Arrays.equals(naturalKey, trade.naturalKey)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return naturalKey != null ? Arrays.hashCode(naturalKey) : 0;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "settleDate=" + settleDate +
                ", maturityDate=" + maturityDate +
                ", notional=" + notional +
                ", rate=" + rate +
                ", tradeType=" + tradeType +
                '}';
    }

    public String toSdos(){
        StringBuilder sb = new StringBuilder("NEWDEAL\n")
                .append("SettlementDate").append(TAB).append(settleDate).append(LF)
                .append("MaturityDate").append(TAB).append(maturityDate).append(LF)
                .append("Notional").append(TAB).append(notional).append(LF)
                .append("Rate").append(TAB).append(rate).append(LF)
                .append("TradeType").append(TAB).append(SPEECHMARK).append(tradeType.name()).append(SPEECHMARK).append(LF)
                //.append("DB_Name").append(TAB).append(SPEECHMARK).append(resolveName()).append(SPEECHMARK).append(LF)
                ;
        return sb.toString();
    }

    private String resolveName(){
        return new String("T-" + tradeType.name() + "." + maturityDate + "." + notional + "/" + rate);
    }
}
