package kizwid.shared.dao;

/**
 * User: kizwid
 * Date: 2012-01-31
 */
public interface DatabaseReleaseDao extends BaseDao{
    <T> T findByScript(Class<T> clazz, String id);
}
