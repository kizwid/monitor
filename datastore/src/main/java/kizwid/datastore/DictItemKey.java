package kizwid.datastore;

import java.io.Serializable;

/**
 * Created by kevin on 22/08/2014.
 */
public class DictItemKey implements Serializable{
    private final String label;
    private final String value;

    public DictItemKey(DictItem dictItem){
        this(dictItem.getLabel(), dictItem.getValue());
    }
    public DictItemKey(String label, String value) {
        this.label = label;
        this.value = value;
    }

    private String getLabel() {
        return label;
    }

    private String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DictItemKey)) return false;

        DictItemKey that = (DictItemKey) o;

        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = label != null ? label.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DictItemKey{" +
                "label='" + label + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

}
