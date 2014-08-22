package kizwid.datastore;

import com.sun.deploy.util.ArrayUtil;

import java.util.*;

/**
 * Created by kevin on 22/08/2014.
 */
public class CompositeDict extends Dict{
    private Set<DictItem> baseDict;
    private Set<DictItem> overrides;
    private DictItemKey[] dictItemOrder;

    public CompositeDict(Set<DictItem> baseDict, Set<DictItem> overrides, DictItemKey[] dictItemOrder) {
        super(mergeDictItems(baseDict, overrides, dictItemOrder));
        this.baseDict = baseDict;
        this.overrides = overrides;
        this.dictItemOrder = dictItemOrder;
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
