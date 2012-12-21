package kizwid.shared.dao;

import kizwid.shared.domain.ErrorEvent;
import kizwid.shared.domain.PricingError;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.List;

public class ErrorEventDaoHibernate
        //extends BaseDaoHibernateImpl implements GenericDao
{

    private final HibernateTemplate hibernateTemplate;

    public ErrorEventDaoHibernate() {
        hibernateTemplate = new HibernateTemplate();
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

    public void save(Object saveableObject){
        ErrorEvent errorEvent = (ErrorEvent)saveableObject;
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
