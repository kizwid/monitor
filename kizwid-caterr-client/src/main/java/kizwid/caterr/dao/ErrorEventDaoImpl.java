package kizwid.caterr.dao;

import kizwid.caterr.domain.ErrorEvent;
import kizwid.caterr.domain.PricingError;
import kizwid.shared.dao.GenericDaoAbstractSpringJdbc;
import kizwid.shared.dao.discriminator.SimpleCriteria;
import kizwid.shared.dao.discriminator.SimpleCriterion;
import kizwid.shared.util.FormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ErrorEventDaoImpl extends GenericDaoAbstractSpringJdbc<ErrorEvent, Long> implements ErrorEventDao {

    private final Logger logger = LoggerFactory.getLogger(ErrorEventDaoImpl.class);
    private final PricingErrorDao pricingErrorDao;

    public ErrorEventDaoImpl(DataSource dataSource, PricingErrorDao pricingErrorDao) {
        super(dataSource,
                "select * from MONITOR_APP_USER.error_event",
                "error_event_id");
        this.pricingErrorDao = pricingErrorDao;
    }

    //------------------------------------------------------
    // BaseDao implementation
    //------------------------------------------------------
    @Override
    public ErrorEvent findById(Long pk) {
        ErrorEvent entity = super.findById(pk);
        attachChildren(Collections.singletonList((ErrorEvent)entity));
        return entity;
    }

    @Override
    public List<ErrorEvent> find(SimpleCriteria criteria) {
        List<ErrorEvent> entities = super.find(criteria);
        attachChildren(entities);
        return entities;
    }

    @Override
    public void save(ErrorEvent errorEvent) {
        try {
            //only insert: no update required as data is immutable
            long nextId = nextId();
            errorEvent.setErrorEventId(nextId);
            String sql = "INSERT INTO MONITOR_APP_USER.error_event ( error_event_id, launch_event_id, created_at, run_id, rollup, risk_group, batch ) " +
                    "VALUES ( ?,?, to_timestamp(?,'YYYY-MM-DD HH24:MI:SS.FF'), ?, ?, ?, ? )"; //to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS.FF') <-> CAST(? AS TIMESTAMP)
            logger.debug(sql);

            jdbcTemplate.update(dialectFriendlySql(sql), errorEvent.getId(), errorEvent.getLaunchEventId(),
                    FormatUtil.formatSqlDateTime(errorEvent.getCreatedAt()),
                    errorEvent.getRunId(), errorEvent.getRollupName(),
                    errorEvent.getRiskGroupName(), errorEvent.getBatchName());

            for (PricingError pricingError : errorEvent.getPricingErrors()) {
                pricingError.setErrorEventId(nextId);
                pricingErrorDao.save(pricingError);
            }

        } catch (DataAccessException exc) {
            logger.error("SAVE ErrorEvent FAILED inserting row {},{},{},{},{},{},{}",
                    new Object[]{
                            errorEvent.getId(),
                            errorEvent.getLaunchEventId(),
                            FormatUtil.formatSqlDateTime(errorEvent.getCreatedAt()),
                            errorEvent.getRunId(),
                            errorEvent.getRollupName(),
                            errorEvent.getRiskGroupName(),
                            errorEvent.getBatchName()
                    },
                    exc);
            throw exc;
        }
    }

    @Override
    protected RowMapper createRowMapper() {
        return new ErrorEventMapper();
    }

    //------------------------------------------------------
    // unique to this dao
    //------------------------------------------------------
    @Override
    @SuppressWarnings("unchecked")
    public List<ErrorEvent> findByRiskGroup(final String riskGroup) {

        //get list of events
        String sql = "select * from MONITOR_APP_USER.error_event where risk_group = ?";
        List<ErrorEvent> events = jdbcTemplate.query(dialectFriendlySql(sql),new Object[]{ riskGroup},
                new ErrorEventMapper());

        //then attach lists of errors
        for (ErrorEvent event : events) {
            List<PricingError> errors = pricingErrorDao.findByErrorEventId(event.getId());
            event.setPricingErrors(errors);
        }

        return events;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ErrorEvent> findUnActioned(String filter) {

        if(filter == null){
            filter="";
        }

        //get list of errors
        List<PricingError> pending = pricingErrorDao.findUnactioned(filter);

        //sort the errors by errorEvent
        Map<Long,List<PricingError>> eventToErrorList = new HashMap<Long,List<PricingError>>();
        for (PricingError pricingError : pending) {
            long id = pricingError.getErrorEventId();
            List<PricingError> filtered = eventToErrorList.get(id);
            if(filtered == null){
                eventToErrorList.put(id, filtered = new LinkedList<PricingError>());
            }
            filtered.add(pricingError);
        }

        //fetch the errorEvent details
        List<ErrorEvent> events = new ArrayList<ErrorEvent>(eventToErrorList.size());
        for (final Long errorEventId : eventToErrorList.keySet()) {
            ErrorEvent errorEvent = findById(errorEventId);
            //attach the unactioned errors for this errorEvent
            errorEvent.setPricingErrors(eventToErrorList.get(errorEventId));
            events.add(errorEvent);
        }

        return events;
    }

    @Override
    public List<ErrorEvent> findByErrorActionId(long id) {
        //get list of errors
        SimpleCriteria criteria = new SimpleCriteria();
        criteria.addCriterion( new SimpleCriterion("error_action_id", SimpleCriterion.Operator.EQUALS, id));
        List<PricingError> pending = pricingErrorDao.find(criteria);

        //sort the errors by errorEvent
        Map<Long,List<PricingError>> eventToErrorList = new HashMap<Long,List<PricingError>>();
        for (PricingError pricingError : pending) {
            Long errorEventId = pricingError.getErrorEventId();
            List<PricingError> filtered = eventToErrorList.get(errorEventId);
            if(filtered == null){
                eventToErrorList.put(errorEventId, filtered = new LinkedList<PricingError>());
            }
            filtered.add(pricingError);
        }

        //fetch the errorEvent details
        List<ErrorEvent> events = new ArrayList<ErrorEvent>(eventToErrorList.size());
        for (final Long errorEventId : eventToErrorList.keySet()) {
            ErrorEvent errorEvent = findById(errorEventId);
            //attach the actioned errors for this errorEvent
            errorEvent.setPricingErrors(eventToErrorList.get(errorEventId));
            events.add(errorEvent);
        }

        return events;
    }


    @Override
    public long lookupErrorEventIdFromLaunchEventId(String id) {
        int errorEventId;
        try{
            errorEventId = jdbcTemplate.queryForInt(dialectFriendlySql("select error_event_id from MONITOR_APP_USER.error_event where launch_event_id = ?"),
                    id);
        }catch (EmptyResultDataAccessException ex){
            errorEventId = -1;
        }

        return errorEventId;
    }

    public long nextId() {
        return jdbcTemplate.queryForInt(dialectFriendlySql("select MONITOR_APP_USER.error_event_seq.NEXTVAL from dual"));
    }

    private static final class ErrorEventMapper implements RowMapper {
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ErrorEvent(
                    rs.getLong("error_event_id"),
                    rs.getString("launch_event_id"),
                    new Date(rs.getTimestamp("created_at").getTime()),
                    rs.getLong("run_id"),
                    rs.getString("rollup"),
                    rs.getString("risk_group"),
                    rs.getString("batch"),
                    new LinkedList<PricingError>()
            );
        }
    }

    private void attachChildren(List<ErrorEvent> events) {
        for (ErrorEvent event : events) {
            List<PricingError> errors = pricingErrorDao.findByErrorEventId(event.getId());
            event.setPricingErrors(errors);
        }
    }


}
