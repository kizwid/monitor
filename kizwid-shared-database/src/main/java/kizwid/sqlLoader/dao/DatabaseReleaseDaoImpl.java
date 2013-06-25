package kizwid.sqlLoader.dao;

import kizwid.shared.dao.GenericDaoAbstractSpringJdbc;
import kizwid.shared.util.FormatUtil;
import kizwid.sqlLoader.domain.DatabaseRelease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * User: kizwid
 * Date: 2012-01-31
 */
public class DatabaseReleaseDaoImpl extends GenericDaoAbstractSpringJdbc<DatabaseRelease, String> implements DatabaseReleaseDao{
    private static final String RELEASE_TABLE = "database_release";
    private final Logger logger = LoggerFactory.getLogger(DatabaseReleaseDaoImpl.class);

    public DatabaseReleaseDaoImpl(DataSource dataSource) {
        super(dataSource,
                "select * from MONITOR_APP_USER." + RELEASE_TABLE,
                "file_name");
    }

    //------------------------------------------------------
    // BaseDao implementation
    //------------------------------------------------------
    @Override
    public void save(DatabaseRelease databaseRelease) {
        try {
            String sql;
            sql = "INSERT INTO MONITOR_APP_USER." + RELEASE_TABLE + " ( file_name, executed_at, checksum, file_last_modified_at, succeeded) " +
                    "VALUES ( ?, ?, ?, ?, ?)";    //to_timestamp(?,'YYYY-MM-DD HH24:MI:SS.FF') <-> CAST(? AS TIMESTAMP)
            logger.info(sql);
            logger.info(databaseRelease.toString());
            jdbcTemplate.update(dialectFriendlySql(sql), new Object[]{
                    databaseRelease.getId(),
                    FormatUtil.formatSqlDateTime(databaseRelease.getExecutedAt()),
                    databaseRelease.getCheckSum(),
                    databaseRelease.getFileLastModifiedAt(),
                    databaseRelease.getSucceeded()
            });

        } catch (DataAccessException exc) {
            logger.error("SAVE DatabaseRelease FAILED! {} {}",
                    new Object[]{
                            databaseRelease.getId(),
                            FormatUtil.formatSqlDateTime(databaseRelease.getExecutedAt()),
                            databaseRelease.getCheckSum(),
                            databaseRelease.getFileLastModifiedAt(),
                            databaseRelease.getSucceeded()
                    }, exc);
            throw exc;
        }
    }

    @Override
    protected RowMapper createRowMapper(){
        return new DatabaseReleaseMapper();
    }

    //------------------------------------------------------
    // unique to this dao
    //------------------------------------------------------
    private static final class DatabaseReleaseMapper implements RowMapper {
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {

            DatabaseRelease e = new DatabaseRelease(
                    rs.getString("file_name"),
                    new Date(rs.getTimestamp("executed_at").getTime()),
                    rs.getString("checksum"),
                    rs.getLong("file_last_modified_at"),
                    rs.getInt("succeeded")
                    );
            return e;
        }
    }


}
