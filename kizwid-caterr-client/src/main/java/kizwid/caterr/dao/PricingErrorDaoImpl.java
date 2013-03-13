package kizwid.caterr.dao;

import kizwid.caterr.domain.PricingError;
import kizwid.shared.dao.GenericDaoAbstractSpringJdbc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * User: kizwid
 * Date: 2012-02-02
 */
public class PricingErrorDaoImpl extends GenericDaoAbstractSpringJdbc<PricingError, Long> implements PricingErrorDao {
    private final static Logger logger = LoggerFactory.getLogger(PricingErrorDaoImpl.class);

    public PricingErrorDaoImpl(DataSource dataSource) {
        super(dataSource,
                "select * from MONITOR_APP_USER.pricing_error",
                "pricing_error_id");
    }

    //------------------------------------------------------
    // BaseDao implementation
    //------------------------------------------------------
    @Override
    public void save(PricingError pricingError) {
        String msg = String.format("PricingError row (%s,%s,%s,%s,%s,%s)",
                new Object[]{
                        pricingError.getId(),
                        pricingError.getErrorEventId(),
                        pricingError.getDictionary(),
                        pricingError.getMarketData(),
                        pricingError.getSplit(),
                        truncate(pricingError.getErrorMessage(),500)
                });

        try {
            long nextId = nextId();
            pricingError.setId(nextId);
            String sql = "INSERT INTO MONITOR_APP_USER.pricing_error ( pricing_error_id, error_event_id, dictionary, market_data, split, error_message ) " +
                    "VALUES ( ?, ?, ?, ?, ?, ? )";
            jdbcTemplate.update(dialectFriendlySql(sql),
                    pricingError.getId(),
                    pricingError.getErrorEventId(),
                    pricingError.getDictionary(),
                    pricingError.getMarketData(),
                    pricingError.getSplit(),
                    truncate(pricingError.getErrorMessage(),500)
            );

        } catch (DataAccessException exc) {
            logger.error("!!!!!!!!!!! FAILED inserting " + msg + " FROM " + pricingError, exc);
            throw exc;
        }
    }

    @Override
    protected RowMapper createRowMapper() {
        return new PricingErrorMapper();
    }

    //------------------------------------------------------
    // unique to this dao
    //------------------------------------------------------
    @Override
    public List<PricingError> findByErrorEventId(final long errorEventId) {
        String sql = "select * from MONITOR_APP_USER.pricing_error where error_event_id = ?";
        return jdbcTemplate.query(dialectFriendlySql(sql), new Object[]{ errorEventId},
                new PricingErrorMapper());
    }

    @Override
    public List<PricingError> findByErrorActionId(final long errorActionId) {
        String sql = "select * from MONITOR_APP_USER.pricing_error pe, " +
                "MONITOR_APP_USER.error_action_pricing_error eape " +
                "where pe.pricing_error_id = eape.pricing_error_id " +
                "and eape.error_action_id = ? " +
                "order by pe.pricing_error_id";
        return jdbcTemplate.query(dialectFriendlySql(sql), new Object[]{ errorActionId},
                new PricingErrorMapper());
    }

    @Override
    @Deprecated
    public List<PricingError> findUnactioned(String filter) {
        String sql = "select * from MONITOR_APP_USER.pricing_error pe " +
                "where not exists(select 1 from MONITOR_APP_USER.error_action_pricing_error x where x.pricing_error_id = pe.pricing_error_id)" +
                (filter == null || filter.length() == 0 ? "":"and pe.error_message like '%" + filter.replace("'","''") + "%' " +
                        "order by pe.pricing_error_id");
        return jdbcTemplate.query(dialectFriendlySql(sql),
                new PricingErrorMapper());
    }

    private long nextId() {
        return jdbcTemplate.queryForInt(dialectFriendlySql("select MONITOR_APP_USER.pricing_error_seq.NEXTVAL from dual"));
    }

    private static final class PricingErrorMapper implements RowMapper {
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            return  new PricingError(
                    rs.getLong("error_event_id"),
                    rs.getLong("pricing_error_id"),
                    rs.getString("dictionary"),
                    rs.getString("market_data"),
                    rs.getString("split"),
                    rs.getString("error_message")
            );
        }
    }

    static String truncate(String data, int maxLength) {
        if(data==null||data.length()<=maxLength){
            return data;
        }
        StringBuilder sb = new StringBuilder();
        int len = data.length();
        if( maxLength > 10){
            //least interesting stuff is in the middle
            sb.append(data.substring(0, 5));
            sb.append("[...]");
            sb.append(data.substring( len-(maxLength - 10),len));
        } else{
            sb.append(data.substring(0, maxLength));
        }
        //logger.warn("truncated string[{}] to[{}]", new Object[]{data,sb.toString()});
        return sb.toString();
    }

}
