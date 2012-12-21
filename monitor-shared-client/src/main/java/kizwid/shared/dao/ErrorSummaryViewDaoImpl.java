package kizwid.shared.dao;

import kizwid.shared.domain.ErrorSummaryView;
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
public class ErrorSummaryViewDaoImpl extends AbstractGenericDao implements ErrorSummaryViewDao {
    private final static Logger logger = LoggerFactory.getLogger(ErrorSummaryViewDaoImpl.class);
    public static final String SQL_FIELD_LIST = "error_action_id,business_date,updated_by,updated_at,action_comment";

    public ErrorSummaryViewDaoImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate,
                "select " + SQL_FIELD_LIST + ",count(*) num_items  from MONITOR_APP_USER.v_error_details",
                "error_action_id",
                " group by " + SQL_FIELD_LIST,
                " order by " + SQL_FIELD_LIST);
    }

    //------------------------------------------------------
    // GenericDao implementation
    //------------------------------------------------------
    @Override
    public void save(Object entity) {
        throw new UnsupportedOperationException("view is read-only");
    }

    @Override
    protected RowMapper createRowMapper() {
        return new ErrorSummaryViewMapper();
    }

    @Override
    public void deleteById(Class clazz, long id){
        final String sql = "delete from MONITOR_APP_USER.error_action_pricing_error where error_action_id = ?";
        int count = jdbcTemplate.update(dialectFriendlySql(sql), id);
        logger.info("dropped {} pricingErrors from errorAction {}", count, id);
    }

    //------------------------------------------------------
    // unique to this dao
    //------------------------------------------------------
    private static final class ErrorSummaryViewMapper implements RowMapper {
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            return  new ErrorSummaryView(
                    rs.getLong("error_action_id"),
                    rs.getInt("business_date"),
                    rs.getString("updated_by"),
                    rs.getTimestamp("updated_at")== null ? null: new Date(rs.getTimestamp("updated_at").getTime()),
                    rs.getString("action_comment"),
                    rs.getInt("num_items")
            );
        }
    }

}
