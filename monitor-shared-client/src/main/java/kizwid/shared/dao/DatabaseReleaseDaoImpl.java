package kizwid.shared.dao;

import kizwid.shared.dao.discriminator.SimpleCriteria;
import kizwid.shared.domain.database.release.DatabaseRelease;
import kizwid.shared.util.FormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * User: kizwid
 * Date: 2012-01-31
 */
public class DatabaseReleaseDaoImpl extends AbstractBaseDao implements DatabaseReleaseDao{
    private final Logger logger = LoggerFactory.getLogger(DatabaseReleaseDaoImpl.class);

    public DatabaseReleaseDaoImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate,
                "select * from MONITOR_APP_USER.database_release",
                "script");
    }

    //------------------------------------------------------
    // BaseDao implementation
    //------------------------------------------------------
    @Override
    public void delete(Object entity, SimpleCriteria criteria) {throw new UnsupportedOperationException();}
    @Override
    public void deleteById(Class clazz, long id) {throw new UnsupportedOperationException();}
    @Override
    public <T> T readById(Class<T> clazz, long id) {throw new UnsupportedOperationException();}
    @Override
    public void save(Object entity) {
        if( !(entity instanceof DatabaseRelease)){
            throw new IllegalArgumentException("entity must be an instancof DatabaseRelease: " + entity);
        }
        DatabaseRelease databaseRelease = (DatabaseRelease)entity;
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
    @Override
    public <T> T findByScript(Class<T> clazz, String id) {
        List<T> events = jdbcTemplate.query( dialectFriendlySql("select * from MONITOR_APP_USER.database_release where script = ?"),new Object[]{ id},
                new DatabaseReleaseMapper());
        return (events.size() == 0? null: events.get(0));
    }


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
