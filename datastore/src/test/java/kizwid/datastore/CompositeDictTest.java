package kizwid.datastore;

import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CompositeDictTest {

    @Test public void canRecomposeInCorrectOrder(){

        DictItem book = DictFixture.createBook();

        Set<DictItem> baseDict = new LinkedHashSet<DictItem>();
        baseDict.add(
                new RegularDictItem("MODEL", "DBOPT", "xyz", DictItemType.Sdos, Collections.<DictItem>emptySet())
        );
        baseDict.add(
                new RegularDictItem("CURVE", "EUR", "abc", DictItemType.Sdos, Collections.<DictItem>emptySet())
        );
        Set<DictItem> overrides = new LinkedHashSet<DictItem>();
        overrides.add(
                new RegularDictItem("MODE", "PRICE", "", DictItemType.Instruction, Collections.<DictItem>emptySet())
        );
        overrides.add(
                book
        );
        overrides.add(
                new RegularDictItem("GRIMIS_BOOK", "kevins-book", "", DictItemType.Instruction, Collections.<DictItem>emptySet())
        );
        DictItemKey[] dictItemOrder = new DictItemKey[baseDict.size() + overrides.size()];
        dictItemOrder[0] = new DictItemKey("MODE", "PRICE");
        dictItemOrder[1] = new DictItemKey("MODEL", "DBOPT");
        dictItemOrder[2] = new DictItemKey("GRIMIS_BOOK", "kevins-book");
        dictItemOrder[3] = new DictItemKey("CURVE", "EUR");
        dictItemOrder[4] = new DictItemKey("BOOK", "some-book");

        CompositeDict compositeDict = new CompositeDict(baseDict, overrides, dictItemOrder);

        assertEquals(baseDict, compositeDict.getBaseDict());
        assertEquals(overrides, compositeDict.getOverrides());
        assertArrayEquals(dictItemOrder, compositeDict.getDictItemOrder());

        Dict check = new Dict(compositeDict.getDictItems());
        assertEquals(compositeDict.getDictItems(), check.getDictItems());

    }

}