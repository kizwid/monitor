package kizwid.shared.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

@Entity
@NamedQueries({
	@NamedQuery(name="PricingError.findById", query="from PricingError e where e.errorEventId = :errorEventId")
	,@NamedQuery(name="PricingError.findBySimiliarError", query="from PricingError e where e.errorMessage like :errorMessagePattern")
})
@Table(name="pricing_error")
public class PricingError {

    private static final Logger logger = LoggerFactory.getLogger(PricingError.class);

    private long errorEventId = -1;
    private long pricingErrorId = -1;
    private String dictionary;
    private String marketData;
    private String split;
    private String errorMessage;


    public PricingError() {
    }
    public PricingError(String dictionary, String marketData, String split, String errorMessage) {
        this(-1,-1,dictionary,marketData,split,errorMessage);
    }

    public PricingError(long errorEventId, long pricingErrorId, String dictionary,
                        String marketData, String split, String errorMessage) {
        this.errorEventId = errorEventId;
        this.pricingErrorId = pricingErrorId;
        this.dictionary = nullSafe(dictionary);
        this.marketData = nullSafe(marketData);
        this.split = nullSafe(split);
        this.errorMessage = nullSafe(errorMessage);
    }

    /* oracle converts emptyString to null
       convert null|empty string to '-'
       as nasty workaround to enable insert into non-nullable field
     */
    private String nullSafe(String value) {
        return ((value == null || value.trim().length() == 0) ? "-" : value);
    }
    @Id
    //@GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pricing_error_id", unique = true, nullable = false)
    public long getPricingErrorId() { return pricingErrorId; }
    public void setPricingErrorId(long pricingErrorId) { this.pricingErrorId = pricingErrorId; }

    public String getDictionary() {
        return dictionary;
    }

    public void setDictionary(String dictionary) {
        this.dictionary = dictionary;
    }

    @Column(name = "market_data", nullable = false)
    public String getMarketData() {
        return marketData;
    }

    public void setMarketData(String marketData) {
        this.marketData = marketData;
    }

    public String getSplit() {
        return split;
    }

    public void setSplit(String split) {
        this.split = split;
    }

    @Column(name = "error_message", nullable = false)
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Column(name = "error_event_id", nullable = false)
    public long getErrorEventId() {
        return errorEventId;
    }

    public void setErrorEventId(long errorEventId) {
        this.errorEventId = errorEventId;
    }

    @Override
    public String toString() {
        return "PricingError{" +
                "errorEventId=" + errorEventId +
                ", pricingErrorId=" + pricingErrorId +
                ", dictionary='" + dictionary + '\'' +
                ", marketData='" + marketData + '\'' +
                ", split='" + split + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PricingError)) return false;

        PricingError that = (PricingError) o;

        if (dictionary != null ? !dictionary.equals(that.dictionary) : that.dictionary != null) return false;
        if (errorEventId != that.errorEventId) return false;
        if (pricingErrorId != that.pricingErrorId) return false;
        if (errorMessage != null ? !errorMessage.equals(that.errorMessage) : that.errorMessage != null) return false;
        if (marketData != null ? !marketData.equals(that.marketData) : that.marketData != null) return false;
        if (split != null ? !split.equals(that.split) : that.split != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int)( errorEventId ^ (errorEventId >>>32));
        result = 31 * result + (int)( pricingErrorId ^ (pricingErrorId >>>32));
        result = 31 * result + (dictionary != null ? dictionary.hashCode() : 0);
        result = 31 * result + (marketData != null ? marketData.hashCode() : 0);
        result = 31 * result + (split != null ? split.hashCode() : 0);
        result = 31 * result + (errorMessage != null ? errorMessage.hashCode() : 0);
        return result;
    }
}
