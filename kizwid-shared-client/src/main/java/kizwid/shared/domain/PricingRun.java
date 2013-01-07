package kizwid.shared.domain;

import java.util.Date;

/**
 * User: kizwid
 * Date: 2012-02-21
 */
public class PricingRun {

    private final long runId;
    private final String runLabel;
    private final int businessDate;
    private final String configId;
    private final Date createdAt;

    public PricingRun(long runId, String runLabel, int businessDate,
                      String configId, Date createdAt) {
        this.runId = runId;
        this.runLabel = runLabel;
        this.businessDate = businessDate;
        this.configId = configId;
        this.createdAt = createdAt;
    }

    public long getRunId() {
        return runId;
    }

    public String getRunLabel() {
        return runLabel;
    }

    public int getBusinessDate() {
        return businessDate;
    }

    public String getConfigId() {
        return configId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PricingRun)) return false;

        PricingRun that = (PricingRun) o;

        if (businessDate != that.businessDate) return false;
        if (runId != that.runId) return false;
        if (configId != null ? !configId.equals(that.configId) : that.configId != null) return false;
        if (createdAt != null ? !createdAt.equals(that.createdAt) : that.createdAt != null) return false;
        if (runLabel != null ? !runLabel.equals(that.runLabel) : that.runLabel != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (runId ^ (runId >>> 32));
        result = 31 * result + (runLabel != null ? runLabel.hashCode() : 0);
        result = 31 * result + businessDate;
        result = 31 * result + (configId != null ? configId.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PricingRun{" +
                "runId=" + runId +
                ", runLabel='" + runLabel + '\'' +
                ", businessDate=" + businessDate +
                ", configId='" + configId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
