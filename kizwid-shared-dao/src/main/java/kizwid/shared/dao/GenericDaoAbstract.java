package kizwid.shared.dao;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 *
 */
public abstract class GenericDaoAbstract<T> implements GenericDao<T> {

    protected final  Class< T > type;

    public GenericDaoAbstract(){
        Type t = getClass().getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) t;
        type = (Class) pt.getActualTypeArguments()[0];

    }

}
