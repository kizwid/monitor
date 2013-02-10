package kizwid.caterr.domain;

import kizwid.shared.dao.PrimaryKey;

/**
 * Created with IntelliJ IDEA.
 * User: kevsanders
 * Date: 27/01/2013
 * Time: 22:04
 * To change this template use File | Settings | File Templates.
 */
public class BaseObject {
    public static PrimaryKey createPrimaryKey(final String[] fields, final Object... values){
        return new PrimaryKey() {
            @Override
            public Object[] getValues() {
                return values;
            }

            @Override
            public String[] getFields() {
                return fields;
            }
        };
    }
}
