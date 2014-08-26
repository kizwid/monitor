package kizwid.datastore;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by kevin on 22/08/2014.
 */
public class DictUtil {

    public static CompositeDict splitDict(Dict dict){

        //separate the original dictionary into its base dictionary (shared)
        //and overrides (most likely not shared)
        //whilst maintaining the original order

        if(dict instanceof CompositeDict){
            //no need to split it twice
            return (CompositeDict)dict;
        }

        Set<DictItem> baseDict = new LinkedHashSet<DictItem>();
        Set<DictItem> overrides = new LinkedHashSet<DictItem>();
        DictItemKey[] dictItemOrder = new DictItemKey[dict.getDictItems().size()];
        int n = 0;
        for (DictItem dictItem : dict.getDictItems()) {
            dictItemOrder[n++]=new DictItemKey(dictItem);
            if(dictItem.getLabel().startsWith("GRIMIS_")||
                    "BOOK".equals(dictItem.getLabel())||
                    "other-rule".equals(dictItem.getLabel())){
                overrides.add(dictItem);
            }else {
                baseDict.add(dictItem);
            }
        }
        return new CompositeDict(baseDict, overrides, dictItemOrder);


    }


}
