package kizwid.web.util;

/**
 * User: kizwid
 * Date: 2012-02-10
 */
public class PropCalcError {
    private final String riskGroup;
    private final String dictionary;
    private final String marketData;
    private final String riskGroupSplit;
    private final String errorMessage;


    public PropCalcError(String riskGroup, String dictionary, String marketData, String riskGroupSplit, String errorMessage) {
        this.riskGroup = riskGroup;
        this.dictionary = dictionary;
        this.marketData = marketData;
        this.riskGroupSplit = riskGroupSplit;
        this.errorMessage = errorMessage;
    }

    public PropCalcError(String riskGroup, String errorMessage) {
        this(riskGroup, null, null, null, errorMessage);
    }

    public String getRiskGroupSplit() {
        return riskGroupSplit;
    }

    public String getRiskGroup() {
        return riskGroup;
    }

    public String getDictionary() {
        return dictionary;
    }

    public String getMarketData() {
        return marketData;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (null == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        PropCalcError that = (PropCalcError) o;

        if (dictionary != null ? !dictionary.equals(that.dictionary) : that.dictionary != null) return false;
        if (marketData != null ? !marketData.equals(that.marketData) : that.marketData != null) return false;
        if (riskGroup != null ? !riskGroup.equals(that.riskGroup) : that.riskGroup != null) return false;
        if (riskGroupSplit != null ? !riskGroupSplit.equals(that.riskGroupSplit) : that.riskGroupSplit != null)
            return false;
        return !(errorMessage != null ? !errorMessage.equals(that.errorMessage) : that.errorMessage != null);

    }

    @Override
    public int hashCode() {
        int result = riskGroup != null ? riskGroup.hashCode() : 0;
        result = 31 * result + (dictionary != null ? dictionary.hashCode() : 0);
        result = 31 * result + (marketData != null ? marketData.hashCode() : 0);
        result = 31 * result + (riskGroupSplit != null ? riskGroupSplit.hashCode() : 0);
        result = 31 * result + (errorMessage != null ? errorMessage.hashCode() : 0);
        return result;
    }
}
