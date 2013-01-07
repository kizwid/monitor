package kizwid.shared.dao;

import kizwid.shared.domain.PricingRun;
import kizwid.shared.util.FormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * User: kizwid
 * Date: 2012-02-02
 */
public class PricingRunDaoImpl extends AbstractBaseDao implements PricingRunDao {
    private final static Logger logger = LoggerFactory.getLogger(PricingRunDaoImpl.class);

    public PricingRunDaoImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate,
                "select * from MONITOR_APP_USER.pricing_run",
                "pricing_run_id");
    }

    //------------------------------------------------------
    // BaseDao implementation
    //------------------------------------------------------
    @Override
    public void save(Object entity) {
        if( !(entity instanceof PricingRun)){
            throw new IllegalArgumentException("entity must be an instancof PricingRun: " + entity);
        }
        PricingRun pricingRun = (PricingRun)entity;
        try {
            String sql = "INSERT INTO MONITOR_APP_USER.pricing_run ( pricing_run_id, run_label, business_date, config_id, created_at) " +
                    "VALUES ( ?, ?, ?, ?, to_timestamp(?,'YYYY-MM-DD HH24:MI:SS.FF') )";
            jdbcTemplate.update(dialectFriendlySql(sql),
                    pricingRun.getRunId(),
                    pricingRun.getRunLabel(),
                    pricingRun.getBusinessDate(),
                    pricingRun.getConfigId(),
                    FormatUtil.formatSqlDateTime(pricingRun.getCreatedAt())
                    );

        } catch (DataAccessException exc) {
            logger.error("SAVE PricingRun FAILED inserting row {},{},{},{},{}",
                    new Object[]{
                            pricingRun.getRunId(),
                            pricingRun.getRunLabel(),
                            pricingRun.getBusinessDate(),
                            pricingRun.getConfigId(),
                            FormatUtil.formatSqlDateTime(pricingRun.getCreatedAt())
                    },
                    exc);
            throw exc;
        }
    }

    @Override
    protected RowMapper createRowMapper() {
        return new PricingRunMapper();
    }

    //------------------------------------------------------
    // unique to this dao
    //------------------------------------------------------
    private long nextId() {
        return jdbcTemplate.queryForInt(dialectFriendlySql("select MONITOR_APP_USER.pricing_run_seq.NEXTVAL from dual"));
    }

    private static final class PricingRunMapper implements RowMapper {
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            return  new PricingRun(
                    rs.getLong("pricing_run_id"),
                    rs.getString("run_label"),
                    rs.getInt("business_date"),
                    rs.getString("config_id"),
                    new Date(rs.getTimestamp("created_at").getTime())
            );
        }
    }

}
