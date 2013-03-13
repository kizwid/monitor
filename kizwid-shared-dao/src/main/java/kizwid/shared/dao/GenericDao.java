package kizwid.shared.dao;

import kizwid.shared.dao.discriminator.SimpleCriteria;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevsanders
 * Date: 21/12/2012
 * Time: 23:18
 * To change this template use File | Settings | File Templates.
 */
public interface GenericDao<T extends Identifiable<ID>, ID extends Serializable> {
    void delete(SimpleCriteria criteria);
    void deleteById(ID primaryKey);
    T findById(ID primaryKey);
    List<T> find(SimpleCriteria criteria);
    void save(T entity);
    void saveAll(Collection<T> entities);
    boolean exists(ID primaryKey);
}
