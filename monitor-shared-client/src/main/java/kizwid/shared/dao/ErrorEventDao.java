package kizwid.shared.dao;


import kizwid.shared.domain.ErrorEvent;

import java.util.List;

/**
 * User: kizwid
 * Date: 2012-02-16
 */
public interface ErrorEventDao extends BaseDao{
    List<ErrorEvent> findByRiskGroup(String riskGroup);
    List<ErrorEvent> findUnActioned(String filter);
    List<ErrorEvent> findByErrorActionId(long id);
    long lookupErrorEventIdFromLaunchEventId(String id);
}
