package kizwid.datastore.biz;

import kizwid.util.FormatUtil;

import java.util.Date;

import static kizwid.datastore.Const.*;

/**
 * Created by kevin on 24/08/2014.
 */
public class Trade {

    private final Date settleDate;
    private final Date maturityDate;
    private final float notional;
    private final float rate;
    private final TradeType tradeType;

    public Trade(Date settleDate, Date maturityDate, float notional, float rate, TradeType tradeType) {
        this.settleDate = settleDate;
        this.maturityDate = maturityDate;
        this.notional = notional;
        this.rate = rate;
        this.tradeType = tradeType;
    }

    public Date getSettleDate() {
        return settleDate;
    }

    public Date getMaturityDate() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Trade trade = (Trade) o;

        if (Float.compare(trade.notional, notional) != 0) return false;
        if (Float.compare(trade.rate, rate) != 0) return false;
        if (maturityDate != null ? !maturityDate.equals(trade.maturityDate) : trade.maturityDate != null) return false;
        if (settleDate != null ? !settleDate.equals(trade.settleDate) : trade.settleDate != null) return false;
        if (tradeType != trade.tradeType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = settleDate != null ? settleDate.hashCode() : 0;
        result = 31 * result + (maturityDate != null ? maturityDate.hashCode() : 0);
        result = 31 * result + (notional != +0.0f ? Float.floatToIntBits(notional) : 0);
        result = 31 * result + (rate != +0.0f ? Float.floatToIntBits(rate) : 0);
        result = 31 * result + (tradeType != null ? tradeType.hashCode() : 0);
        return result;
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
                .append("SettlementDate").append(TAB).append(FormatUtil.yyyymmdd(settleDate)).append(LF)
                .append("MaturityDate").append(TAB).append(FormatUtil.yyyymmdd(maturityDate)).append(LF)
                .append("Notional").append(TAB).append(notional).append(LF)
                .append("Rate").append(TAB).append(rate).append(LF)
                .append("TradeType").append(TAB).append(SPEECHMARK).append(tradeType.name()).append(SPEECHMARK).append(LF)
                ;
        return sb.toString();
    }
}
