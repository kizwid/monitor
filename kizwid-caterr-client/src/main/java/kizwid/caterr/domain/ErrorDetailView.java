package kizwid.caterr.domain;

import kizwid.shared.dao.Identifiable;

import java.util.Date;

/**
 * User: kizwid
 * Date: 2012-02-21
 */
public class ErrorDetailView implements Identifiable<Object[]> {

    private final long errorActionId,runId,errorEventId,pricingErrorId;
    private final String updatedBy,
            runLabel,
            actionComment,
            configId,
            launchEventId,rollup,riskGroup,batch,dictionary,marketData,split,errorMessage;
    private final int runBusinessDate,actionBusinessDate;
    private final Date runCreatedAt,actionUpdatedAt,errorCreatedAt;

    public ErrorDetailView(long errorActionId, int actionBusinessDate, String updatedBy, Date actionUpdatedAt, String actionComment, long runId,
                           Date runCreatedAt, String configId, String runLabel, int runBusinessDate, long errorEventId, Date errorCreatedAt, String launchEventId, String rollup, String riskGroup, String batch, long pricingErrorId,
                           String dictionary, String marketData,
                           String split, String errorMessage) {
        this.errorActionId = errorActionId;
        this.runId = runId;
        this.errorEventId = errorEventId;
        this.pricingErrorId = pricingErrorId;
        this.updatedBy = updatedBy;
        this.runLabel = runLabel;
        this.actionComment = actionComment;
        this.configId = configId;
        this.launchEventId = launchEventId;
        this.rollup = rollup;
        this.riskGroup = riskGroup;
        this.batch = batch;
        this.dictionary = dictionary;
        this.marketData = marketData;
        this.split = split;
        this.errorMessage = errorMessage;
        this.runBusinessDate = runBusinessDate;
        this.actionBusinessDate = actionBusinessDate;
        this.runCreatedAt = runCreatedAt;
        this.actionUpdatedAt = actionUpdatedAt;
        this.errorCreatedAt = errorCreatedAt;
    }

    public long getErrorActionId() {
        return errorActionId;
    }

    public long getRunId() {
        return runId;
    }

    public long getErrorEventId() {
        return errorEventId;
    }

    public long getPricingErrorId() {
        return pricingErrorId;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public String getRunLabel() {
        return runLabel;
    }

    public String getActionComment() {
        return actionComment;
    }

    public String getConfigId() {
        return configId;
    }

    public String getLaunchEventId() {
        return launchEventId;
    }

    public String getRollup() {
        return rollup;
    }

    public String getRiskGroup() {
        return riskGroup;
    }

    public String getBatch() {
        return batch;
    }

    public String getDictionary() {
        return dictionary;
    }

    public String getMarketData() {
        return marketData;
    }

    public String getSplit() {
        return split;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getRunBusinessDate() {
        return runBusinessDate;
    }

    public int getActionBusinessDate() {
        return actionBusinessDate;
    }

    public Date getRunCreatedAt() {
        return runCreatedAt;
    }

    public Date getActionUpdatedAt() {
        return actionUpdatedAt;
    }

    public Date getErrorCreatedAt() {
        return errorCreatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ErrorDetailView)) return false;

        ErrorDetailView that = (ErrorDetailView) o;

        if (actionBusinessDate != that.actionBusinessDate) return false;
        if (errorActionId != that.errorActionId) return false;
        if (errorEventId != that.errorEventId) return false;
        if (pricingErrorId != that.pricingErrorId) return false;
        if (runBusinessDate != that.runBusinessDate) return false;
        if (runId != that.runId) return false;
        if (actionComment != null ? !actionComment.equals(that.actionComment) : that.actionComment != null)
            return false;
        if (actionUpdatedAt != null ? !actionUpdatedAt.equals(that.actionUpdatedAt) : that.actionUpdatedAt != null)
            return false;
        if (batch != null ? !batch.equals(that.batch) : that.batch != null) return false;
        if (configId != null ? !configId.equals(that.configId) : that.configId != null) return false;
        if (dictionary != null ? !dictionary.equals(that.dictionary) : that.dictionary != null) return false;
        if (errorCreatedAt != null ? !errorCreatedAt.equals(that.errorCreatedAt) : that.errorCreatedAt != null)
            return false;
        if (errorMessage != null ? !errorMessage.equals(that.errorMessage) : that.errorMessage != null) return false;
        if (launchEventId != null ? !launchEventId.equals(that.launchEventId) : that.launchEventId != null)
            return false;
        if (marketData != null ? !marketData.equals(that.marketData) : that.marketData != null) return false;
        if (riskGroup != null ? !riskGroup.equals(that.riskGroup) : that.riskGroup != null) return false;
        if (rollup != null ? !rollup.equals(that.rollup) : that.rollup != null) return false;
        if (runCreatedAt != null ? !runCreatedAt.equals(that.runCreatedAt) : that.runCreatedAt != null) return false;
        if (runLabel != null ? !runLabel.equals(that.runLabel) : that.runLabel != null) return false;
        if (split != null ? !split.equals(that.split) : that.split != null) return false;
        if (updatedBy != null ? !updatedBy.equals(that.updatedBy) : that.updatedBy != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (errorActionId ^ (errorActionId >>> 32));
        result = 31 * result + (int) (runId ^ (runId >>> 32));
        result = 31 * result + (int) (errorEventId ^ (errorEventId >>> 32));
        result = 31 * result + (int) (pricingErrorId ^ (pricingErrorId >>> 32));
        result = 31 * result + (updatedBy != null ? updatedBy.hashCode() : 0);
        result = 31 * result + (runLabel != null ? runLabel.hashCode() : 0);
        result = 31 * result + (actionComment != null ? actionComment.hashCode() : 0);
        result = 31 * result + (configId != null ? configId.hashCode() : 0);
        result = 31 * result + (launchEventId != null ? launchEventId.hashCode() : 0);
        result = 31 * result + (rollup != null ? rollup.hashCode() : 0);
        result = 31 * result + (riskGroup != null ? riskGroup.hashCode() : 0);
        result = 31 * result + (batch != null ? batch.hashCode() : 0);
        result = 31 * result + (dictionary != null ? dictionary.hashCode() : 0);
        result = 31 * result + (marketData != null ? marketData.hashCode() : 0);
        result = 31 * result + (split != null ? split.hashCode() : 0);
        result = 31 * result + (errorMessage != null ? errorMessage.hashCode() : 0);
        result = 31 * result + runBusinessDate;
        result = 31 * result + actionBusinessDate;
        result = 31 * result + (runCreatedAt != null ? runCreatedAt.hashCode() : 0);
        result = 31 * result + (actionUpdatedAt != null ? actionUpdatedAt.hashCode() : 0);
        result = 31 * result + (errorCreatedAt != null ? errorCreatedAt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ErrorDetailView{" +
                "errorActionId=" + errorActionId +
                ", runId=" + runId +
                ", errorEventId=" + errorEventId +
                ", pricingErrorId=" + pricingErrorId +
                ", updatedBy='" + updatedBy + '\'' +
                ", runLabel='" + runLabel + '\'' +
                ", actionComment='" + actionComment + '\'' +
                ", configId='" + configId + '\'' +
                ", launchEventId='" + launchEventId + '\'' +
                ", rollup='" + rollup + '\'' +
                ", riskGroup='" + riskGroup + '\'' +
                ", batch='" + batch + '\'' +
                ", dictionary='" + dictionary + '\'' +
                ", marketData='" + marketData + '\'' +
                ", split='" + split + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", runBusinessDate=" + runBusinessDate +
                ", actionBusinessDate=" + actionBusinessDate +
                ", runCreatedAt=" + runCreatedAt +
                ", actionUpdatedAt=" + actionUpdatedAt +
                ", errorCreatedAt=" + errorCreatedAt +
                '}';
    }

    @Override
    public Object[] getId() {
        return BaseObject.pk(errorActionId, pricingErrorId);
    }
}
