package kizwid.shared.dao;

import kizwid.shared.domain.ErrorDetailView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * User: kizwid
 * Date: 2012-02-02
 */
public class ErrorDetailViewDaoImpl extends AbstractBaseDao implements ErrorDetailViewDao {
    private final static Logger logger = LoggerFactory.getLogger(ErrorDetailViewDaoImpl.class);

    public ErrorDetailViewDaoImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate,
                "select * from MONITOR_APP_USER.v_error_details",
                "pricing_run_id");
    }

    //------------------------------------------------------
    // BaseDao implementation
    //------------------------------------------------------
    @Override
    public void save(Object entity) {
        throw new UnsupportedOperationException("view is read-only");
    }

    @Override
    protected RowMapper createRowMapper() {
        return new ErrorDetailViewMapper();
    }

    //------------------------------------------------------
    // unique to this dao
    //------------------------------------------------------
    private static final class ErrorDetailViewMapper implements RowMapper {
        private final int MAX_ROWS = 1000;
        private int rowCount = 0;
        private static ErrorDetailView EMPTY_ROW =  new ErrorDetailView(
                -2L,
                0,
                "-",
                null,
                "...continued",
                0L,
                null,
                "-",
                "-",
                0,
                0L,
                null,
                "-",
                "-",
                "-",
                "-",
                0L,
                "-",
                "-",
                "-",
                "Too many errors to show them all... view truncated"
                );

        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            if(rowCount++ >= MAX_ROWS){
                while(rs.next()){

                }
                return EMPTY_ROW;//crude limit to prevent overflowing browser
            }
            return  new ErrorDetailView(
                    rs.getLong("error_action_id"),
                    rs.getInt("business_date"),
                    rs.getString("updated_by"),
                    rs.getTimestamp("updated_at")== null ? null: new Date(rs.getTimestamp("updated_at").getTime()),
                    rs.getString("action_comment"),
                    rs.getLong("run_id"),
                    rs.getTimestamp("run_created_at")== null ? null:new Date(rs.getTimestamp("run_created_at").getTime()),
                    rs.getString("config_id"),
                    rs.getString("run_label"),
                    rs.getInt("run_business_date"),
                    rs.getLong("error_event_id"),
                    rs.getTimestamp("error_created_at")== null ? null:new Date(rs.getTimestamp("error_created_at").getTime()),
                    rs.getString("launch_event_id"),
                    rs.getString("rollup"),
                    rs.getString("risk_group"),
                    rs.getString("batch"),
                    rs.getLong("pricing_error_id"),
                    rs.getString("dictionary"),
                    rs.getString("market_data"),
                    rs.getString("split"),
                    rs.getString("error_message")
            );
        }
    }

}
