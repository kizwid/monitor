package kizwid.sqlLoader.dao;

import kizwid.shared.dao.GenericDaoSpringJdbcImpl;
import kizwid.shared.dao.PrimaryKey;
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
public class DatabaseReleaseDaoImpl extends GenericDaoSpringJdbcImpl<DatabaseRelease> implements DatabaseReleaseDao{
    private final Logger logger = LoggerFactory.getLogger(DatabaseReleaseDaoImpl.class);

    public DatabaseReleaseDaoImpl(DataSource dataSource) {
        super(dataSource,
                "select * from MONITOR_APP_USER.database_release",
                new PrimaryKey() {
                    @Override
                    public String[] getFields() {
                        return new String[]{"script"};
                    }
                    @Override
                    public Object[] getValues() {
                        return new Object[0];
                    }
                });
    }

    //------------------------------------------------------
    // BaseDao implementation
    //------------------------------------------------------
    @Override
    public void save(DatabaseRelease databaseRelease) {
        try {
            String sql;
            sql = "INSERT INTO MONITOR_APP_USER.database_release ( script, deployed_at) " +
                    "VALUES ( ?, to_timestamp(?,'YYYY-MM-DD HH24:MI:SS.FF'))";    //to_timestamp(?,'YYYY-MM-DD HH24:MI:SS.FF') <-> CAST(? AS TIMESTAMP)
            logger.debug(sql);
            jdbcTemplate.update(dialectFriendlySql(sql), new Object[]{
                    databaseRelease.getScript(),
                    FormatUtil.formatSqlDateTime(databaseRelease.getDeployed_at())
            });

        } catch (DataAccessException exc) {
            logger.error("SAVE DatabaseRelease FAILED! {} {}",
                    new Object[]{databaseRelease.getScript(),
                    FormatUtil.formatSqlDateTime(databaseRelease.getDeployed_at())}, exc);
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
                    rs.getString("script"),
                    new Date(rs.getTimestamp("deployed_at").getTime())
                    );
            return e;
        }
    }


}
