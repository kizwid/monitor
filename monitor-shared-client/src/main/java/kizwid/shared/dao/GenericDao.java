package kizwid.shared.dao;


import kizwid.shared.dao.discriminator.SimpleCriteria;

import java.util.Collection;
import java.util.List;

/**
 * Defines the basic responsibilities of all daos.
 * 
 * @author kizwid
 */
public interface GenericDao {  //<T>

    /**
     * Removes <code>deleteableObject</code>s from the database.
     * That matches the given criteria
     *
     * @param entity
     *            the object to be deleted
     * @param criteria
     */
    void delete(Object entity, SimpleCriteria criteria);

    /**
     * Removes an entity of type <code>clazz</code> identified by the given <code>id</code>.
     * 
     * @param clazz
     *            the type of the object to be deleted
     * @param id
 *            the unique identifier for the given object
     */
    @SuppressWarnings("unchecked")
    void deleteById(Class clazz, long id);

    /**
     * Retrieves a single instance of the given <code>clazz</code> assuming that the given <code>id</code> is its unique handle.
     * 
     *
     * @param clazz
     * @param id
     * @return
     */
    <T> T readById(Class<T> clazz, long id);

    /**
     * Retrieves all instances of the given <code>clazz</code>.
     * That matches the given criteria
     *
     *
     * @param clazz
     * @param criteria
     * @return
     */
    <T> List<T> read(Class<T> clazz, SimpleCriteria criteria);

   /**
     * Persists the given <code>saveableObject</code>. This method will save or update the given object depending on whether it is a new
     * instance or an update to an existing instance.
     * 
     * @param entity
     *            the object to be saved
     */
    void save(Object entity);

    void saveAll(Collection entities);

    /**
     * Check to see if entity of type <code>clazz</code> with the given <code>id</code> already exists.
     *
     *
     * @param clazz
     * @param id
     * @return
     */
    <T> boolean exists(Class<T> clazz, long id);
}
