package kizwid.shared.dao;

import kizwid.shared.dao.discriminator.SimpleCriteria;
import kizwid.shared.domain.ErrorAction;

import java.util.List;

public interface ErrorActionDao extends GenericDao {
    List<ErrorAction> findByBusinessDate(int businessDate);
    void attachPricingErrorsToAction(long errorActionId, List<Long> pricingErrorIds);
    void attachPricingErrorsFromCriteria(long errorActionId, SimpleCriteria criteria);
}
