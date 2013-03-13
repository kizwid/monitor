package kizwid.caterr.dao;

import kizwid.caterr.domain.ErrorSummaryView;
import kizwid.shared.dao.GenericDaoAbstractSpringJdbc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * User: kizwid
 * Date: 2012-02-02
 */
public class ErrorSummaryViewDaoImpl extends GenericDaoAbstractSpringJdbc<ErrorSummaryView, Long> implements ErrorSummaryViewDao {
    private final static Logger logger = LoggerFactory.getLogger(ErrorSummaryViewDaoImpl.class);
    public static final String SQL_FIELD_LIST = "error_action_id,business_date,updated_by,updated_at,action_comment";

    public ErrorSummaryViewDaoImpl(DataSource dataSource) {
        super(dataSource,
                "select " + SQL_FIELD_LIST + ",count(*) num_items  from MONITOR_APP_USER.v_error_details",
                "error_action_id"/*,
                " group by " + SQL_FIELD_LIST,
                " order by " + SQL_FIELD_LIST*/);
    }

    //------------------------------------------------------
    // BaseDao implementation
    //------------------------------------------------------
    @Override
    public void save(ErrorSummaryView entity) {
        throw new UnsupportedOperationException("view is read-only");
    }

    @Override
    protected RowMapper createRowMapper() {
        return new ErrorSummaryViewMapper();
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
