package kizwid.datastore;


import java.util.*;

/**
 * Created by kevin on 22/08/2014.
 */
public class CompositeDict extends Dict{
    private Set<DictItem> baseDict;
    private Set<DictItem> overrides;
    private DictItemKey[] dictItemOrder;

    public CompositeDict(Set<DictItem> baseDict, Set<DictItem> overrides, DictItemKey[] dictItemOrder) {
        //this(-1L, baseDict, overrides, dictItemOrder, NaturalKeyFactory.create(mergeDictItems(baseDict, overrides, dictItemOrder)));//TODO: unknown naturalKey
        this(-1L, baseDict, overrides, dictItemOrder, NaturalKeyFactory.UNKNOWN_NATURAL_KEY);//TODO: unknown naturalKey
    }
    public CompositeDict(long id, Set<DictItem> baseDict, Set<DictItem> overrides, DictItemKey[] dictItemOrder, byte[] naturalKey) {
        super(id, mergeDictItems(baseDict, overrides, dictItemOrder), naturalKey);
        this.baseDict = baseDict;
        this.overrides = overrides;
        this.dictItemOrder = dictItemOrder;
    }

    public Set<DictItem> getBaseDict() {
        return baseDict;
    }

    public Set<DictItem> getOverrides() {
        return overrides;
    }

    public DictItemKey[] getDictItemOrder() {
        return dictItemOrder;
    }

    @Override
    public String toString() {
        return "CompositeDict{" +
                "baseDict=" + baseDict +
                ", overrides=" + overrides +
                ", dictItemOrder=" + Arrays.toString(dictItemOrder) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CompositeDict that = (CompositeDict) o;

        if (baseDict != null ? !baseDict.equals(that.baseDict) : that.baseDict != null) return false;
        if (!Arrays.equals(dictItemOrder, that.dictItemOrder)) return false;
        if (overrides != null ? !overrides.equals(that.overrides) : that.overrides != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (baseDict != null ? baseDict.hashCode() : 0);
        result = 31 * result + (overrides != null ? overrides.hashCode() : 0);
        result = 31 * result + (dictItemOrder != null ? Arrays.hashCode(dictItemOrder) : 0);
        return result;
    }

    public static Set<DictItem> mergeDictItems(Set<DictItem> baseDict, Set<DictItem> overrides, DictItemKey[] dictItemOrder){
        Map<DictItemKey, DictItem> preMerge = new HashMap<DictItemKey, DictItem>();
        for (DictItem dictItem : baseDict) {
            preMerge.put(new DictItemKey(dictItem), dictItem);
        }
        for (DictItem dictItem : overrides) {
            preMerge.put(new DictItemKey(dictItem), dictItem);
        }
        Set<DictItem> orderedSet = new LinkedHashSet<DictItem>();
        for (DictItemKey dictItemKey : dictItemOrder) {
            DictItem nextItem = preMerge.get(dictItemKey);
            if(nextItem == null){
                throw new IllegalStateException("Unable to merge CompositeDict as the next ordered item is not present in either the base or the overrides expected:" + nextItem + " from: " + preMerge);
            }
            orderedSet.add(nextItem);
        }
        return orderedSet;
    }
}
