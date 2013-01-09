package kizwid.caterr.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * User Action to deal with the PricingErrors
 *
 * User: kizwid
 * Date: 2012-01-31
 */
@Entity
@Table(name = "error_action")
public class ErrorAction {

    private long id;
    private int businessDate;
    private String updatedBy;
    private Date updatedAt;
    private String comment;
    private List<PricingError> pricingErrors;

    public ErrorAction(int businessDate, String updatedBy, Date updatedAt, String comment) {
        this(-1, businessDate, updatedBy, updatedAt, comment); //new record
    }
    public ErrorAction(long id, int businessDate, String updatedBy, Date updatedAt, String comment) {
        this.id = id;
        this.businessDate = businessDate;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
        this.comment = comment;
        this.pricingErrors = Collections.emptyList();
    }

    @Id
    @Column(name = "error_action_id", unique = true, nullable = false)
    public long getId() {
        return id;
    }

    @Column(name = "business_date", nullable = false)
    public int getBusinessDate() {
        return businessDate;
    }

    @Column(name = "updated_by", nullable = false)
    public String getUpdatedBy() {
        return updatedBy;
    }

    @Column(name = "updated_at", nullable = false)
    public Date getUpdatedAt() {
        return updatedAt;
    }

    @Column(name = "action_comment", nullable = false)
    public String getComment() {
        return comment;
    }

    public List<PricingError> getPricingErrors() {
        return pricingErrors;
    }

    public void setPricingErrors(List<PricingError> pricingErrors) {
        this.pricingErrors = pricingErrors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ErrorAction)) return false;

        ErrorAction that = (ErrorAction) o;

        if (id != that.id) return false;
        if (businessDate != that.businessDate) return false;
        if( pricingErrors != null){
            if( pricingErrors.size() != that.pricingErrors.size()) return false;
            for (int n = 0; n < pricingErrors.size(); n++) {
                if( !pricingErrors.get(n).equals(that.pricingErrors.get(n)))return false;
            }
        }else{
            if(that.pricingErrors != null) return false;
        }
        if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
        if (updatedAt != null ? !updatedAt.equals(that.updatedAt) : that.updatedAt != null) return false;
        if (updatedBy != null ? !updatedBy.equals(that.updatedBy) : that.updatedBy != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + businessDate;
        result = 31 * result + (updatedBy != null ? updatedBy.hashCode() : 0);
        result = 31 * result + (updatedAt != null ? updatedAt.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (pricingErrors != null ? pricingErrors.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ErrorAction{" +
                "id=" + id +
                ", businessDate=" + businessDate +
                ", updatedBy='" + updatedBy + '\'' +
                ", updatedAt=" + updatedAt +
                ", comment='" + comment + '\'' +
                ", pricingErrors=" + pricingErrors +
                '}';
    }

    //hmmm
    public void setId(long id) {
        this.id = id;
    }
}

