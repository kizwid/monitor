package kizwid.caterr.dao;

import kizwid.caterr.domain.ErrorEvent;
import kizwid.caterr.domain.PricingError;
import kizwid.shared.dao.GenericDaoAbstractHibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.List;

public class ErrorEventDaoHibernate
        extends GenericDaoAbstractHibernate<ErrorEvent> implements ErrorEventDao
{

    private final HibernateTemplate hibernateTemplate;

    public ErrorEventDaoHibernate(SessionFactory sessionFactory) {
        hibernateTemplate = new HibernateTemplate(sessionFactory);
    }

    @SuppressWarnings("unchecked")
	public List<ErrorEvent> findByRiskGroup( final String riskGroup) {
        return (List<ErrorEvent>) hibernateTemplate.execute(new HibernateCallback() {
            public Object doInHibernate(Session session) {
                Query query = session.getNamedQuery("ErrorEvent.findByRiskGroup");
                query.setParameter("riskGroupName", riskGroup);
                return query.list();
            }
        });
    }

    @Override
    public List<ErrorEvent> findUnActioned(String filter) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ErrorEvent> findByErrorActionId(long id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long lookupErrorEventIdFromLaunchEventId(String id) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void save(ErrorEvent errorEvent){
        if(errorEvent.getErrorEventId() == -1){
            int nextId = 0;//TODO
            errorEvent.setErrorEventId(nextId);
            for (PricingError pricingError : errorEvent.getPricingErrors()) {
                if(pricingError.getPricingErrorId() == -1){
                    //todo:nextId
                }
                pricingError.setErrorEventId(nextId);
            }
        }
    }


}
