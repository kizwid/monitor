package kizwid.caterr.dao;


import kizwid.caterr.domain.ErrorEvent;
import kizwid.shared.dao.GenericDao;

import java.util.List;

/**
 * User: kizwid
 * Date: 2012-02-16
 */
public interface ErrorEventDao extends GenericDao<ErrorEvent> {
    List<ErrorEvent> findByRiskGroup(String riskGroup);
    List<ErrorEvent> findUnActioned(String filter);
    List<ErrorEvent> findByErrorActionId(long id);
    long lookupErrorEventIdFromLaunchEventId(String id);
}
