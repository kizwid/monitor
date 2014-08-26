package kizwid.datastore;

import kizwid.shared.Builder;
import org.apache.commons.codec.binary.Hex;

import java.util.*;

import static kizwid.datastore.Const.*;

/**
 * Created by kevin on 19/08/2014.
 */
public class Dict implements DictItem{
    public static final Dict EMPTY = new Dict(Collections.singleton(RegularDictItem.EMPTY));
    private long id;
    private byte[] naturalKey;
    private Set<DictItem> dictItems;

    public Dict(Set<DictItem> dictItems) {
        this(-1L, dictItems, NaturalKeyFactory.create(dictItems));
    }
    public Dict(long id, Set<DictItem> dictItems, byte[] naturalKey) {
        this.id = id;
        this.dictItems = new LinkedHashSet<DictItem>(dictItems);
        this.naturalKey = NaturalKeyFactory.create(dictItems);
        if (!Arrays.equals(naturalKey, NaturalKeyFactory.UNKNOWN_NATURAL_KEY))
            //only compare calculated with expected when expected is already known
            if (!Arrays.equals(naturalKey, this.naturalKey)) {
                throw new IllegalStateException("Calculated naturalKey not matched given(" + Hex.encodeHexString(naturalKey) + ") calculated(" + Hex.encodeHexString(this.naturalKey) + ")");
            }
    }

    public long getId() {
        return id;
    }

    public byte[] getNaturalKey() {
        return naturalKey;
    }

    public Set<DictItem> getDictItems() {
        return dictItems;
    }

    @Override
    public String getLabel() {
        return "DICTIONARY";
    }

    @Override
    public String getValue() {
        return Hex.encodeHexString(naturalKey);
    }

    @Override
    public String getVersion() {
        return getValue();
    }

    @Override
    public DictItemType getDictItemType() {
        return DictItemType.Collection;
    }

    @Override
    public Set<DictItem> getChildren() {
        return getDictItems();
    }

    @Override
    public String getValueAndVersion() {
        String version = getVersion();
        String value = getValue();
        return (version.length()==0)?
                value :
                new StringBuilder(value)
                        .append(':')
                        .append(version)
                        .toString();
    }

    public String toSdos(){
        StringBuilder sb = new StringBuilder()
                .append("NEWPRICER").append(LF)
                .append("DB_ClassName")
                .append(TAB)
                .append(SPEECHMARK).append("cDictionary").append(SPEECHMARK)
                .append(LF)
                .append("DB_Name")
                .append(TAB)
                .append(SPEECHMARK).append(Hex.encodeHexString(naturalKey)).append(SPEECHMARK)
                .append(LF)
                .append("SubTableBegin DictInfo").append(LF);
        for (DictItem dictItem : dictItems) {
            sb
                    .append(SPEECHMARK).append(dictItem.getLabel()).append(SPEECHMARK)
                    .append(TAB)
                    .append(SPEECHMARK).append(dictItem.getValue()).append(SPEECHMARK)
                    .append(LF);
        }
        sb.append("SubTableEnd").append(LF);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dict dict = (Dict) o;

        if (id != dict.id) return false;
        if (dictItems != null ? !dictItems.equals(dict.dictItems) : dict.dictItems != null) return false;
        if (!Arrays.equals(naturalKey, dict.naturalKey)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (naturalKey != null ? Arrays.hashCode(naturalKey) : 0);
        result = 31 * result + (dictItems != null ? dictItems.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Dict{" +
                "id=" + id +
                ", naturalKey=" + Hex.encodeHexString(naturalKey) +
                ", dictItems=" + dictItems +
                '}';
    }

    public static class DictBuilder implements Builder<Dict> {
        private final Dict template;

        public DictBuilder() {
            this(new Dict(EMPTY.dictItems));
        }
        public DictBuilder(Dict prototype) {
            this.template = new Dict(prototype.getDictItems());

        }

        public DictBuilder withId(long p){template.id = p;return this;}
        public DictBuilder withDictItems(Set<DictItem> p){template.dictItems = new LinkedHashSet<DictItem>(p);return this;}
        public DictBuilder withDictItem(DictItem p){template.dictItems.add(p);return this;}

        /**
         * replace the dictItem identified by its key
         *
         * @param dictItemKey
         * @param dictItem
         * @return
         */
        public DictBuilder replaceDictItem(DictItemKey dictItemKey, DictItem dictItem) {
            Map<DictItemKey, DictItem> data = new LinkedHashMap<DictItemKey, DictItem>();
            for (DictItem item : template.getDictItems()) {
                data.put(new DictItemKey(item), item);
            }
            if(!data.containsKey(dictItemKey)){
                throw new IllegalArgumentException("DictItemKey " + dictItemKey + " not found in Dict " + data);
            }
            data.put(dictItemKey, dictItem);
            template.dictItems = new LinkedHashSet<DictItem>(data.values());
            return this;
        }

        @Override
        public Dict build() {
            template.naturalKey = NaturalKeyFactory.create(template.dictItems);
            return template;
        }

    }

}
