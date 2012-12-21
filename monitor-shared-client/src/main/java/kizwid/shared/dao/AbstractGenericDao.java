package kizwid.shared.dao;

import kizwid.shared.dao.discriminator.SimpleCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * User: kizwid
 * Date: 2012-02-16
 */
public abstract class AbstractGenericDao implements GenericDao {

    private final static Logger logger = LoggerFactory.getLogger(AbstractGenericDao.class);
    static {
        logger.info("loaded AbstractGenericDao version 20120309-1554");
    }
    protected final JdbcTemplate jdbcTemplate;
    private final String sqlSelectAll;
    private final String sqlGroupBy, sqlOrderBy;
    private final String sqlSelectUniqueEntity;
    private final String databaseDialect;


    public AbstractGenericDao(JdbcTemplate jdbcTemplate, String sqlSelectAll, String primaryKeyField) {
        this(jdbcTemplate,sqlSelectAll, primaryKeyField, "", "");
    }
    public AbstractGenericDao(JdbcTemplate jdbcTemplate, String sqlSelectAll, String primaryKeyField,
                              String sqlGroupBy, String sqlOrderBy) {
        this.jdbcTemplate = jdbcTemplate;
        this.databaseDialect = readDatabaseDialect(jdbcTemplate);
        this.sqlGroupBy = sqlGroupBy;
        this.sqlOrderBy = sqlOrderBy;
        this.sqlSelectAll =  dialectFriendlySql(sqlSelectAll);
        this.sqlSelectUniqueEntity = this.sqlSelectAll + " where " + primaryKeyField + " = ? " + sqlGroupBy + sqlOrderBy;
    }

    @Override
    public void delete(Object entity, SimpleCriteria criteria) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void deleteById(Class clazz, long id) {
        throw new UnsupportedOperationException();
    }
    @Override
    public <T> T readById(Class<T> clazz, long id) {
        List<T> events = jdbcTemplate.query(sqlSelectUniqueEntity,new Object[]{ id},
                createRowMapper());
        return (events.size() == 0? null:events.get(0));
    }
    @Override
    public <T> List<T> read(Class<T> clazz, SimpleCriteria criteria) {
        try {
            return jdbcTemplate.query(sqlSelectAll +
                    criteria.resolveWhereClause() +
                    sqlGroupBy +
                    sqlOrderBy,
                    createRowMapper());
        } catch (DataAccessException e) {
            logger.warn("failed to read " + clazz.getSimpleName() + " with error: " + e);
            return Collections.emptyList();
        }
    }
    @Override
    public void saveAll(Collection entities) {
        for (Object entity : entities) {
            save(entity);
        }
    }

    @Override
    public <T> boolean exists(Class<T> clazz, long id) {
        //extract the sql after the from clause
        int fromPos = sqlSelectUniqueEntity.toLowerCase().indexOf(" from ");
        String sql = "select count(*) " + sqlSelectUniqueEntity.substring(fromPos);
        int count = jdbcTemplate.queryForInt(sql, id);
        return (count > 0);
    }

    protected abstract RowMapper createRowMapper();

    /**
     * hsqldb and oracle use different cast methods to translate
     * from string to timestamp
     * @param jdbcTemplate
     * @return appropriateTimeStampCastStrategy
     */
    private String readDatabaseDialect(JdbcTemplate jdbcTemplate) {
        String dialect = "Unknown";
        Connection connection = null;
        try {
            connection = jdbcTemplate.getDataSource().getConnection();
            dialect = connection.getMetaData().getDatabaseProductName();
        } catch (Exception e) {
            logger.error("failed to read DatabaseProductName", e);
        }finally {
            try{connection.close();}catch (Exception ex){}
        }
        return dialect;
    }

    public String dialectFriendlySql(String sql){
        //replace oracle with hsql where appropriate
        if( "HSQL Database Engine".equals(databaseDialect)){
            String fixed = sql.replace("MONITOR_APP_USER.","");
            fixed = fixed.replace("to_timestamp(?,'YYYY-MM-DD HH24:MI:SS.FF')", "CAST(? AS TIMESTAMP)");
            return fixed;
        }else {
            return sql;
        }
    }

}
