package kizwid.datastore;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class DictUtilTest {

    @Test public void canSplitDict(){

        Dict eod = DictFixture.createStandard();
        CompositeDict compositeDict = DictUtil.splitDict(eod);

        Set<DictItem> baseDict = compositeDict.getBaseDict();
        Set<DictItem> overrides = compositeDict.getOverrides();
        DictItemKey[] keys = compositeDict.getDictItemOrder();

        assertEquals(compositeDict, new CompositeDict(baseDict, overrides, keys));
        assertEquals(eod, new Dict(eod.getId(), compositeDict.getDictItems(), eod.getNaturalKey()));

    }

}