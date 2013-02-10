package kizwid.caterr.dao;

import kizwid.caterr.domain.ErrorAction;
import kizwid.shared.dao.GenericDao;
import kizwid.shared.dao.discriminator.SimpleCriteria;

import java.util.List;

public interface ErrorActionDao extends GenericDao<ErrorAction> {
    List<ErrorAction> findByBusinessDate(int businessDate);
    void attachPricingErrorsToAction(long errorActionId, List<Long> pricingErrorIds);
    void attachPricingErrorsFromCriteria(long errorActionId, SimpleCriteria criteria);
}
