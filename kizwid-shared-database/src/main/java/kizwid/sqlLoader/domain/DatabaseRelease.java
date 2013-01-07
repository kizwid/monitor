package kizwid.sqlLoader.domain;

import kizwid.shared.util.FormatUtil;

import java.util.Date;

/**
 * User: kizwid
 * Date: 2012-01-31
 */
public class DatabaseRelease {
    
    private String script;
    private Date deployed_at;

    public DatabaseRelease(String script, Date deployed_at) {
        this.script = script;
        this.deployed_at = deployed_at;
    }

    public DatabaseRelease(String script) {
        this(script, new Date());
    }

    public String getScript() {
        return script;
    }

    public Date getDeployed_at() {
        return deployed_at;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatabaseRelease)) return false;

        DatabaseRelease that = (DatabaseRelease) o;

        if (deployed_at != null ? !deployed_at.equals(that.deployed_at) : that.deployed_at != null) return false;
        if (script != null ? !script.equals(that.script) : that.script != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = script != null ? script.hashCode() : 0;
        result = 31 * result + (deployed_at != null ? deployed_at.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DatabaseRelease{" +
                "script='" + script + '\'' +
                ", deployed_at=" +  FormatUtil.formatSqlDateTime(deployed_at) +
                '}';
    }

}
