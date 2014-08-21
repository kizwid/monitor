package kizwid.datastore;

import java.util.Set;

/**
 * Created by kevin on 21/08/2014.
 */
public interface DictItem {
    String getLabel();

    String getValue();

    String getVersion();

    DictItemType getDictItemType();

    Set<DictItem> getChildren();

    String getValueAndVersion();

    long getId();

    byte[] getNaturalKey();
}
