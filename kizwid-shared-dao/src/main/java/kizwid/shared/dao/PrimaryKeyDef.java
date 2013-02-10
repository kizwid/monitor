package kizwid.shared.dao;

import java.io.Serializable;

/**
 *
 */
public interface PrimaryKeyDef extends Serializable {
    String[] getFields();
}
