package kizwid.shared.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;


public class  BaseDaoHibernateImpl {
    private static final Logger logger = LoggerFactory.getLogger(BaseDaoHibernateImpl.class);

    protected HibernateTemplate hibernateTemplate;

    public BaseDaoHibernateImpl() {
    }

    public void delete(Object deleteableObject) {
        hibernateTemplate.delete(deleteableObject);
    }

    @SuppressWarnings("unchecked")
    public void deleteById(Class clazz, long id) {
        Object deletable = readById(clazz, id);
        if (deletable != null) {
            delete(deletable);
        } else {
            logger.warn("Attempted to delete an instance of: {} with id: {} but no such instance exists", clazz.getSimpleName(), id);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T readById(Class<T> clazz, long id) {
        return (T) hibernateTemplate.get(clazz, id);
    }

    //@Override
    public <T> List<T> read(final Class<T> clazz) {
        return (List<T>) hibernateTemplate.execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createCriteria(clazz).list();
            }
        });
    }

    //@Override
    public void save(Object saveableObject) {
        hibernateTemplate.saveOrUpdate(saveableObject);
    }

    //@Override
    public void saveAll(Collection entities) {
        hibernateTemplate.saveOrUpdateAll(entities);
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        hibernateTemplate = new HibernateTemplate(sessionFactory);
    }
}
