package kizwid.datastore;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class DictTest {

    @Test
    public void canCreateEmpty(){
        Dict empty = Dict.EMPTY;
        assertEquals(-1, empty.getId());
        assertEquals("EMPTY.null", empty.getDictItems().iterator().next().toString());
    }

    @Test public void canCreateFromBuilder(){

        Set<DictItem> trades = new LinkedHashSet<DictItem>();
        trades.add(new RegularDictItem("SECURITY", "trade-1", "cde", DictItemType.Sdos, Collections.<DictItem>emptySet()));
        trades.add(new RegularDictItem("SECURITY", "trade-2", "cdf", DictItemType.Sdos, Collections.<DictItem>emptySet()));
        trades.add(new RegularDictItem("SECURITY", "trade-3", "cdg", DictItemType.Sdos, Collections.<DictItem>emptySet()));

        DictItem book = new RegularDictItem.DictItemBuilder()
                .withChildren(trades)
                .withVersion(Hex.encodeHexString(NaturalKeyFactory.create(trades)))
                .withDictItemType(DictItemType.Collection)
                .withId(10L)
                .withLabel("BOOK")
                .withValue("some-book")
                .build();

        Dict dict1 = new Dict.DictBuilder()
                .withId(1L)
                .withDictItems(new LinkedHashSet<DictItem>())
                .withDictItem(new RegularDictItem("MODE", "PRICE", "", DictItemType.Instruction, Collections.<DictItem>emptySet()))
                .withDictItem(new RegularDictItem("MODEL", "DBOPT", "xyz", DictItemType.Sdos, Collections.<DictItem>emptySet()))
                .withDictItem(new RegularDictItem("CURVE", "EUR", "abc", DictItemType.Sdos, Collections.<DictItem>emptySet()))
                .withDictItem(book)
                .build();
        System.out.println(dict1);
        System.out.println(dict1.toSdos());

        Dict dict2 = new Dict.DictBuilder()
                .withId(2L)
                .withDictItems(new LinkedHashSet<DictItem>())
                .withDictItem(new RegularDictItem("MODE", "PRICE", "", DictItemType.Instruction, Collections.<DictItem>emptySet()))
                .withDictItem(new RegularDictItem("MODEL", "DBOPT", "xyz", DictItemType.Sdos, Collections.<DictItem>emptySet()))
                .withDictItem(new RegularDictItem("CURVE", "IPV.EUR", "abc", DictItemType.Sdos, Collections.<DictItem>emptySet()))
                .withDictItem(book)
                .build();
        System.out.println(dict2);
        System.out.println(dict2.toSdos());

        Dict dict3 = new Dict.DictBuilder()
                .withId(3L)
                .withDictItems(new LinkedHashSet<DictItem>())
                .withDictItem(new RegularDictItem("MODE", "PRICEPL", "", DictItemType.Instruction, Collections.<DictItem>emptySet()))
                .withDictItem(dict1)
                .withDictItem(dict2)
                .build();
        System.out.println(dict3);
        System.out.println(dict3.toSdos());

    }

}