package kizwid.shared.dao;

import kizwid.shared.dao.discriminator.SimpleCriteria;
import kizwid.shared.dao.discriminator.SimpleCriterion;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.engine.TypedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;


public class GenericDaoAbstractHibernate<T extends Identifiable<ID>, ID extends Serializable> extends GenericDaoAbstract<T, ID> implements GenericDao<T, ID> {
    private static final Logger logger = LoggerFactory.getLogger(GenericDaoAbstractHibernate.class);

    protected HibernateTemplate hibernateTemplate;

    public GenericDaoAbstractHibernate() {
        super();
    }

    //TODO: consider adding this to Generic Interface
    public void delete(T deleteableObject) {
        hibernateTemplate.delete(deleteableObject);
    }

    @Override
    public void delete(SimpleCriteria criteria) {
        for (T deleteable : find(criteria)) {
            delete(deleteable);
        }
    }

    @Override
    public void deleteById(ID pk) {
        T deletable = findById(pk);
        if (deletable != null) {
            delete(deletable);
        } else {
            logger.warn("Attempted to delete an instance of: {} with id: {} but no such instance exists", type.getSimpleName(), pk);
        }
    }

    @Override
    public T findById(ID id) {
        return (T) hibernateTemplate.get(type, id);
    }

    @Override
    public List<T> find(final SimpleCriteria criteria) {
        return (List<T>) hibernateTemplate.execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                final Criteria sessionCriteria = session.createCriteria(type);
                for (final SimpleCriterion criterion : criteria.getCriteria()) {
                    sessionCriteria.add(new Criterion() {
                        @Override
                        public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
                            return criterion.getField() + criterion.getOperator() + criterion.getValue();
                        }

                        @Override
                        public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
                            return new TypedValue[0];
                        }
                    });
                }
                return sessionCriteria.list();
            }
        });
    }

    @Override
    public void save(T saveableObject) {
        hibernateTemplate.saveOrUpdate(saveableObject);
    }

    @Override
    public void saveAll(Collection<T> entities) {
        hibernateTemplate.saveOrUpdateAll(entities);
    }

    @Override
    public boolean exists(ID pk) {
        T found = findById(pk);
        return found != null;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        hibernateTemplate = new HibernateTemplate(sessionFactory);
    }
}
