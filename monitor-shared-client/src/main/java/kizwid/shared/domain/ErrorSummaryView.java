package kizwid.shared.domain;

import java.util.Date;

/**
 * User: kizwid
 * Date: 2012-02-21
 */
public class ErrorSummaryView {

    private long errorActionId;
    private int businessDate;
    private String updatedBy;
    private Date updatedAt;
    private String actionComment;
    private int numberOfItems;

    public ErrorSummaryView(long errorActionId, int businessDate, String updatedBy, Date updatedAt, String actionComment, int numberOfItems) {
        this.errorActionId = errorActionId;
        this.businessDate = businessDate;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
        this.actionComment = actionComment;
        this.numberOfItems = numberOfItems;
    }

    public long getErrorActionId() {
        return errorActionId;
    }

    public int getBusinessDate() {
        return businessDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getActionComment() {
        return actionComment;
    }

    public int getNumberOfItems() {
        return numberOfItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ErrorSummaryView)) return false;

        ErrorSummaryView that = (ErrorSummaryView) o;

        if (businessDate != that.businessDate) return false;
        if (errorActionId != that.errorActionId) return false;
        if (numberOfItems != that.numberOfItems) return false;
        if (actionComment != null ? !actionComment.equals(that.actionComment) : that.actionComment != null)
            return false;
        if (updatedAt != null ? !updatedAt.equals(that.updatedAt) : that.updatedAt != null) return false;
        if (updatedBy != null ? !updatedBy.equals(that.updatedBy) : that.updatedBy != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (errorActionId ^ (errorActionId >>> 32));
        result = 31 * result + businessDate;
        result = 31 * result + (updatedBy != null ? updatedBy.hashCode() : 0);
        result = 31 * result + (updatedAt != null ? updatedAt.hashCode() : 0);
        result = 31 * result + (actionComment != null ? actionComment.hashCode() : 0);
        result = 31 * result + numberOfItems;
        return result;
    }

    @Override
    public String toString() {
        return "ErrorSummaryView{" +
                "errorActionId=" + errorActionId +
                ", businessDate=" + businessDate +
                ", updatedBy='" + updatedBy + '\'' +
                ", updatedAt=" + updatedAt +
                ", actionComment='" + actionComment + '\'' +
                ", numberOfItems=" + numberOfItems +
                '}';
    }
}
