package kizwid.datastore;

import kizwid.shared.Builder;
import org.apache.commons.codec.binary.Hex;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.lang.String.format;

/**
 * Created by kevin on 19/08/2014.
 */
public class RegularDictItem implements DictItem {
    public final static DictItem EMPTY = new RegularDictItem("EMPTY", "null", "", DictItemType.Instruction, Collections.<DictItem>emptySet());
    private long id;
    private byte[] naturalKey;
    private String label;
    private String value;
    private String version;
    private Set<DictItem> children;
    private DictItemType dictItemType;

    public RegularDictItem(DictItem prototype) {
        this(
                prototype.getId()
                , prototype.getLabel()
                , prototype.getValue()
                , prototype.getVersion()
                , prototype.getDictItemType()
                , prototype.getChildren()
        );
    }
    public RegularDictItem(String label, String value, String version, DictItemType dictItemType, Set<DictItem> children) {
        this(-1L, label, value, version, dictItemType, children);
    }
    public RegularDictItem(long id, String label, String value, String version, DictItemType dictItemType, Set<DictItem> children) {
        this.id = id;
        this.label = label;
        this.value = value;
        this.version = version;
        this.dictItemType = dictItemType;
        this.children = new LinkedHashSet<DictItem>(children);
        resolveNaturalKey(dictItemType, children);

    }

    private void resolveNaturalKey(DictItemType dictItemType, Set<DictItem> children) {
        switch (dictItemType){
            case Instruction:
            case Sdos:
                naturalKey = NaturalKeyFactory.create(this);
                break;
            case Collection:
            case Custom:
            default:
                naturalKey = NaturalKeyFactory.create(children);
                this.version = Hex.encodeHexString(naturalKey);
                break;
        }
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public DictItemType getDictItemType() {
        return dictItemType;
    }

    @Override
    public Set<DictItem> getChildren() {
        return Collections.unmodifiableSet(children);
    }

    @Override
    public String getValueAndVersion(){
        return (version.length()==0)?
                value:
                new StringBuilder(value)
                        .append(':')
                        .append(version)
                        .toString();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public byte[] getNaturalKey() {
        return naturalKey;
    }

    public String toString(){
        return format("%s.%s", getLabel(), getValueAndVersion());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegularDictItem that = (RegularDictItem) o;

        if (id != that.id) return false;
        if (children != null ? !children.equals(that.children) : that.children != null) return false;
        if (dictItemType != that.dictItemType) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (!Arrays.equals(naturalKey, that.naturalKey)) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (naturalKey != null ? Arrays.hashCode(naturalKey) : 0);
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (children != null ? children.hashCode() : 0);
        result = 31 * result + (dictItemType != null ? dictItemType.hashCode() : 0);
        return result;
    }

    public static final class DictItemBuilder implements Builder<DictItem>{
        private final RegularDictItem template;

        public DictItemBuilder() {
            this(EMPTY);
        }
        public DictItemBuilder(DictItem prototype) {
            this.template = new RegularDictItem(prototype);
        }
        public DictItemBuilder withId(long s){
            template.id = s;
            return this;
        }
        public DictItemBuilder withLabel(String s){
            template.label = s;
            return this;
        }

        public DictItemBuilder withValue(String s){
            template.value = s;
            return this;
        }

        public DictItemBuilder withVersion(String s){
            template.version = s;
            return this;
        }

        public DictItemBuilder withDictItemType(DictItemType t){
            template.dictItemType = t;
            return this;
        }

        public DictItemBuilder withChildren(Set<DictItem> t){
            template.children = new LinkedHashSet<DictItem>(t);
            return this;
        }

        public DictItemBuilder withChild(DictItem t){
            template.children.add(t);
            return this;
        }

        @Override
        public DictItem build() {
            switch (template.dictItemType){
                case Instruction:
                case Sdos:
                    template.naturalKey = NaturalKeyFactory.create(template);
                    break;
                case Collection:
                case Custom:
                default:
                    template.naturalKey = NaturalKeyFactory.create(template.children);
                    template.version = Hex.encodeHexString(template.naturalKey);
                    break;
            }
            return template;
        }
    }

}
