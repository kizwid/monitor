package kizwid.shared.dao;


import kizwid.shared.domain.PricingError;

import java.util.List;

/**
 * User: kizwid
 * Date: 2012-02-16
 */
public interface PricingErrorDao extends BaseDao {
    List<PricingError> findByErrorEventId(long errorEventId);
    List<PricingError> findByErrorActionId(long errorActionId);
    List<PricingError> findUnactioned(String filter);
}
