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
        assertEquals(1, empty.getDictItems().size());
        assertEquals("EMPTY.null", empty.getDictItems().iterator().next().toString());
    }

    @Test public void canCreateFromBuilder(){

        Dict eod = DictFixture.createStandard();
        Dict ipv = new Dict.DictBuilder(eod)
                .withId(2)
                .replaceDictItem(
                        new DictItemKey("CURVE", "EUR"),
                        new RegularDictItem("CURVE", "IPV.EUR", "xabc", DictItemType.Sdos, Collections.<DictItem>emptySet())
                )
                .build();

        Dict ppl = new Dict.DictBuilder()
                .withId(3)
                .withDictItems(new LinkedHashSet<DictItem>())
                .withDictItem(new RegularDictItem("MODE", "PRICEPL", "", DictItemType.Instruction, Collections.<DictItem>emptySet()))
                .withDictItem(eod)
                .withDictItem(ipv)
                .build();

        assertEquals(eod.getDictItems().size(), ipv.getDictItems().size());

        //TODO: more assertions
        System.out.println(ppl.toString());

    }

}