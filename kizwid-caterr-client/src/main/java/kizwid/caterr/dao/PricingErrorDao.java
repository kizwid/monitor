package kizwid.caterr.dao;


import kizwid.caterr.domain.PricingError;
import kizwid.shared.dao.GenericDao;

import java.util.List;

/**
 * User: kizwid
 * Date: 2012-02-16
 */
public interface PricingErrorDao extends GenericDao<PricingError> {
    List<PricingError> findByErrorEventId(long errorEventId);
    List<PricingError> findByErrorActionId(long errorActionId);
    List<PricingError> findUnactioned(String filter);
}
