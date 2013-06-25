package kizwid.sqlLoader.domain;

import kizwid.shared.dao.Identifiable;
import kizwid.shared.util.CalcDigest;
import kizwid.shared.util.FormatUtil;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

/**
 * User: kizwid
 * Date: 2012-01-31
 */
public class DatabaseRelease implements Identifiable<String> {
    
    //<DBMAINTAIN_SCRIPTS
    // CHECKSUM="e10cf11c9e3ebc6719e8ad946bcfcdc3"
    // EXECUTED_AT="2013-06-08 23:22:10"
    // FILE_LAST_MODIFIED_AT="1357256521000"
    // FILE_NAME="schema.sql"
    // SUCCEEDED="1"/>

    private final String fileName;
    private final Date executedAt;
    private String checkSum;
    private long fileLastModifiedAt;
    private int  succeeded;

    public DatabaseRelease(String resource) {
        this(resource, new Date());
    }

    public DatabaseRelease(String resource, Date executedAt) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
        int slash = resource.lastIndexOf('/');

        this.fileName = resource.substring(slash + 1);
        this.executedAt = executedAt;
        this.succeeded = 1;
        URLConnection urlConnection = null;
        try {
            urlConnection = url.openConnection();
            this.fileLastModifiedAt = urlConnection.getLastModified();
            this.checkSum = CalcDigest.checksum(new InputStreamReader(urlConnection.getInputStream()));
        } catch (Exception e) {
            this.fileLastModifiedAt = 0;
            this.checkSum = "xxx";
        } finally {
            try{
                urlConnection.getInputStream().close();
            }catch (Exception ex){}
    }

    }
    public DatabaseRelease(String fileName, Date executedAt, String checksum, long fileLastModifiedAt, int succeeded) {
        this.fileName = fileName;
        this.executedAt = executedAt;
        this.checkSum = checksum;
        this.fileLastModifiedAt = fileLastModifiedAt;
        this.succeeded = succeeded;
    }

    @Override
    public String getId() {
        return fileName;
    }

    public Date getExecutedAt() {
        return executedAt;
    }

    public String getCheckSum() {
        return checkSum;
    }

    public long getFileLastModifiedAt() {
        return fileLastModifiedAt;
    }

    public int getSucceeded() {
        return succeeded;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatabaseRelease)) return false;

        DatabaseRelease that = (DatabaseRelease) o;

        if (executedAt != null ? !executedAt.equals(that.executedAt) : that.executedAt != null) return false;
        if (fileName != null ? !fileName.equals(that.fileName) : that.fileName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fileName != null ? fileName.hashCode() : 0;
        result = 31 * result + (executedAt != null ? executedAt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DatabaseRelease{" +
                "fileName='" + fileName + '\'' +
                ", executedAt=" +  FormatUtil.formatSqlDateTime(executedAt) +
                '}';
    }

}
