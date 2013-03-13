package kizwid.caterr.domain;

import kizwid.shared.dao.Identifiable;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name = "error_event")
public class ErrorEvent extends BaseObject implements Identifiable<Long> {

    private long errorEventId = -1;
    private String launchEventId;
    private Date createdAt;
    private Long runId;
    private String rollupName;
    private String riskGroupName;
    private String batchName;
    private List<PricingError> pricingErrors = new LinkedList<PricingError>();

    public ErrorEvent(String launchEventId, Date createdAt, Long runId, String rollupName,
                      String riskGroupName, String batchName) {
        this(-1, launchEventId, createdAt, runId,rollupName,
                riskGroupName,batchName, new LinkedList<PricingError>());
    }

    public ErrorEvent(long errorEventId, String launchEventId, Date createdAt, Long runId, String rollupName,
                      String riskGroupName, String batchName, List<PricingError> pricingErrors) {
        this.errorEventId = errorEventId;
        this.launchEventId = launchEventId;
        this.createdAt = createdAt;
        this.runId = runId;
        this.rollupName = rollupName;
        this.riskGroupName = riskGroupName;
        this.batchName = batchName;
        this.pricingErrors = pricingErrors;
    }

    @Id
    @Column(name = "error_event_id", unique = true, nullable = false)
    @Override
    public Long getId() {
        return errorEventId;
    }

    public void setErrorEventId(long errorEventId) {
        this.errorEventId = errorEventId;
    }

    @Column(name = "created_at", nullable = false)
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Column(name = "run_id", nullable = false)
    public Long getRunId() {
        return runId;
    }

    public void setRunId(long runId) {
        this.runId = runId;
    }

    @Column(name = "rollup", nullable = false)
    public String getRollupName() {
        return rollupName;
    }

    public void setRollupName(String rollupName) {
        this.rollupName = rollupName;
    }

    @Column(name = "risk_group", nullable = false)
    public String getRiskGroupName() {
        return riskGroupName;
    }

    public void setRiskGroupName(String riskGroupName) {
        this.riskGroupName = riskGroupName;
    }

    @Column(name = "batch", nullable = false)
    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @JoinColumn(name="error_event_id")
    @OrderBy("pricingErrorId")
    public List<PricingError> getPricingErrors() {
        return pricingErrors;
    }

    public void setPricingErrors(List<PricingError> pricingErrors) {
        this.pricingErrors = pricingErrors;
    }

    @Column(name = "launch_event_id", nullable = false)
    public String getLaunchEventId() {
        return launchEventId;
    }

    public void setLaunchEventId(String launchEventId) {
        this.launchEventId = launchEventId;
    }

    @Override
    public String toString() {

        return "ErrorEvent{" +
                "errorEventId=" + errorEventId +
                ", createdAt=" + formattedCreatedAt() +
                ", runId=" + runId +
                ", rollupName='" + rollupName + '\'' +
                ", riskGroupName='" + riskGroupName + '\'' +
                ", batchName='" + batchName + '\'' +
                ", pricingErrors=" + pricingErrors +
                '}';
    }

    private String formattedCreatedAt(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return (createdAt==null?"null":sdf.format(createdAt));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ErrorEvent)) return false;

        ErrorEvent that = (ErrorEvent) o;

        if (batchName != null ? !batchName.equals(that.batchName) : that.batchName != null) return false;
        if (createdAt != null ? !createdAt.equals(that.createdAt) : that.createdAt != null) return false;
        if (errorEventId != that.errorEventId) return false;
        if( pricingErrors != null){
            if( pricingErrors.size() != that.pricingErrors.size()) return false;
            for (int n = 0; n < pricingErrors.size(); n++) {
                if( !pricingErrors.get(n).equals(that.pricingErrors.get(n)))return false;
            }
        }else{
            if(that.pricingErrors != null) return false;
        }
        if (riskGroupName != null ? !riskGroupName.equals(that.riskGroupName) : that.riskGroupName != null)
            return false;
        if (rollupName != null ? !rollupName.equals(that.rollupName) : that.rollupName != null) return false;
        if (runId != null ? !runId.equals(that.runId) : that.runId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (errorEventId ^ (errorEventId >>> 32));
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (runId != null ? runId.hashCode() : 0);
        result = 31 * result + (rollupName != null ? rollupName.hashCode() : 0);
        result = 31 * result + (riskGroupName != null ? riskGroupName.hashCode() : 0);
        result = 31 * result + (batchName != null ? batchName.hashCode() : 0);
        result = 31 * result + (pricingErrors != null ? pricingErrors.hashCode() : 0);
        return result;
    }

}
