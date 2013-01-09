package kizwid.shared.dao;

import kizwid.shared.dao.discriminator.SimpleCriteria;
import kizwid.shared.domain.ErrorAction;
import kizwid.shared.domain.PricingError;
import kizwid.shared.util.FormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ErrorActionDaoImpl extends GenericDaoAbstractSpringJdbc<ErrorAction> implements ErrorActionDao {

    private final Logger logger = LoggerFactory.getLogger(ErrorActionDaoImpl.class);
    public static final SimpleDateFormat YYYYMMDD = new SimpleDateFormat("yyyyMMdd");
    private final PricingErrorDao pricingErrorDao;

    public ErrorActionDaoImpl(DataSource dataSource, PricingErrorDao pricingErrorDao){
        super(dataSource,
                "select * from MONITOR_APP_USER.error_action",
                new PrimaryKey() {
                    @Override
                    public String[] getFields() {
                        return new String[]{"error_action_id"};
                    }

                    @Override
                    public Object[] getValues() {
                        return new Object[0];
                    }
                });
        this.pricingErrorDao = pricingErrorDao;
    }

    @Override
    public void save(ErrorAction errorAction) {
        try {

            boolean isNew = (errorAction.getId() == -1);
            //check to see if this one already exists
            if (isNew) {

                long nextId = nextId();
                errorAction.setId(nextId);

                jdbcTemplate.update(dialectFriendlySql("INSERT INTO MONITOR_APP_USER.error_action ( error_action_id, business_date, updated_by, updated_at, action_comment) " +
                        "VALUES ( ?, ?, ?, to_timestamp(?,'YYYY-MM-DD HH24:MI:SS.FF'), ?)"), nextId,
                        errorAction.getBusinessDate(),
                        errorAction.getUpdatedBy(),
                        FormatUtil.formatSqlDateTime(errorAction.getUpdatedAt()),
                        errorAction.getComment());

            } else {
                jdbcTemplate.update(dialectFriendlySql("UPDATE MONITOR_APP_USER.error_action set updated_by=?, " +
                        "updated_at=to_timestamp(?,'YYYY-MM-DD HH24:MI:SS.FF')," +   //to_timestamp(?,'YYYY-MM-DD HH24:MI:SS.FF') <-> CAST(? AS TIMESTAMP)
                        "action_comment=? " +
                        "where error_action_id = ?"), errorAction.getUpdatedBy(),
                        FormatUtil.formatSqlDateTime(errorAction.getUpdatedAt()),
                        errorAction.getComment(),
                        errorAction.getId());
                jdbcTemplate.update(dialectFriendlySql("delete from MONITOR_APP_USER.error_action_pricing_error where error_action_id = ?"), errorAction.getId());
            }


            //pricingErrors are never created by errorAction: only ever associated to one
            List<Long> pricingErrorIds = new ArrayList<Long>(errorAction.getPricingErrors().size());
            for (PricingError pricingError : errorAction.getPricingErrors()) {
                pricingErrorIds.add(pricingError.getPricingErrorId());
            }
            attachPricingErrorsToAction(errorAction.getId(), pricingErrorIds);

        } catch (DataAccessException exc) {
            logger.error("SAVE ErrorAction FAILED for {}", errorAction, exc);
            throw exc;
        }
    }


    @Override
    protected RowMapper createRowMapper() {
        return new ErrorActionMapper();
    }

    //------------------------------------------------------
    // unique to this dao
    //------------------------------------------------------


    @Override
    public ErrorAction findById(PrimaryKey primaryKey) {
        ErrorAction errorAction = super.findById(primaryKey);
        attachChildren(Collections.singletonList(errorAction));
        return errorAction;
    }

    @Override
    public List<ErrorAction> findByBusinessDate(int businessDate) {
        String sql = "select * from MONITOR_APP_USER.error_action where business_date = ? order by error_action_id desc";
        try {
            List<ErrorAction> events = jdbcTemplate.query(dialectFriendlySql(sql), new Object[]{businessDate}, new ErrorActionMapper());
            attachChildren(events);

            //and then add any errors without an action
            List<PricingError> pending = pricingErrorDao.findUnactioned("");
            logger.info("adding {} new errors", pending.size());
            if (pending.size() > 0) {
                ErrorAction unhandled = new ErrorAction(
                        parseDate(null), "-", null, "New Errors"
                );
                unhandled.setPricingErrors(pending);
                events.add(0, unhandled);//add this to top of the list
            }

            return events;

        } catch (DataAccessException e) {
            logger.warn("failed to findByBusinessDate: {} {}", businessDate, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public void attachPricingErrorsToAction(long errorActionId, List<Long> pricingErrorIds) {
        for (long pricingErrorId : pricingErrorIds) {
            //pricing error can only belong to one action
            jdbcTemplate.update(dialectFriendlySql("delete from MONITOR_APP_USER.error_action_pricing_error where pricing_error_id = ?"),
                    pricingErrorId);
            jdbcTemplate.update(dialectFriendlySql("insert into MONITOR_APP_USER.error_action_pricing_error (error_action_id,pricing_error_id) values (?,?)"),
                    errorActionId,
                    pricingErrorId);
        }
    }

    @Override
    public void attachPricingErrorsFromCriteria(long errorActionId, SimpleCriteria criteria) {
        jdbcTemplate.update(dialectFriendlySql(
                "delete from MONITOR_APP_USER.error_action_pricing_error where pricing_error_id in(" +
                        "select pricing_error_id from MONITOR_APP_USER.v_error_details " + criteria.resolveWhereClause() + ")"));
        jdbcTemplate.update(dialectFriendlySql(
                "insert into MONITOR_APP_USER.error_action_pricing_error (error_action_id,pricing_error_id) " +
                        "select " + errorActionId + ", pricing_error_id from MONITOR_APP_USER.v_error_details " +
                        criteria.resolveWhereClause()));
    }

    private long nextId() {
        return jdbcTemplate.queryForInt(dialectFriendlySql("select MONITOR_APP_USER.error_action_seq.NEXTVAL from dual"));
    }

    private void attachChildren(List<ErrorAction> events) {
        //then attach lists of errors
        for (ErrorAction event : events) {
            List<PricingError> errors = pricingErrorDao.findByErrorActionId(event.getId());
            event.setPricingErrors(errors);
        }
    }

    private static final class ErrorActionMapper implements RowMapper {
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {

            ErrorAction e = new ErrorAction(
                    rs.getLong("error_action_id"),
                    rs.getInt("business_date"),
                    rs.getString("updated_by"),
                    new Date(rs.getTimestamp("updated_at").getTime()),
                    rs.getString("action_comment")
            );
            return e;
        }
    }

    private int parseDate(String yyyymmdd) {
        if (yyyymmdd == null) {
            yyyymmdd = yyyymmdd(new Date());
        }
        return Integer.parseInt(yyyymmdd);
    }

    public static String yyyymmdd(Date date) {
        return YYYYMMDD.format(date);
    }
}
