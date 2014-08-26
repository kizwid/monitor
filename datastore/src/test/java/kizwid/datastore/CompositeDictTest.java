package kizwid.datastore;

import kizwid.datastore.biz.Trade;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class CompositeDictTest {

    @Test public void canRecomposeInCorrectOrder(){

        Set<Trade> trades = new LinkedHashSet<Trade>();
        trades.add(TradeFixture.createRandomTrade());
        trades.add(TradeFixture.createRandomTrade());
        trades.add(TradeFixture.createRandomTrade());

        Set<DictItem> tradeItems = new LinkedHashSet<DictItem>(trades.size());
        for (Trade trade : trades) {
            tradeItems.add(new RegularDictItem("SECURITY", trade.getName(), Hex.encodeHexString(trade.getNaturalKey()), DictItemType.Sdos, Collections.<DictItem>emptySet()));
        }
        //tradeItems.add(new RegularDictItem("SECURITY", "trade-1", "cde", DictItemType.Sdos, Collections.<DictItem>emptySet()));
        //tradeItems.add(new RegularDictItem("SECURITY", "trade-2", "cdf", DictItemType.Sdos, Collections.<DictItem>emptySet()));
        //tradeItems.add(new RegularDictItem("SECURITY", "trade-3", "cdg", DictItemType.Sdos, Collections.<DictItem>emptySet()));

        DictItem book = new RegularDictItem.DictItemBuilder()
                .withChildren(tradeItems)
                .withVersion(Hex.encodeHexString(NaturalKeyFactory.create(tradeItems)))
                .withDictItemType(DictItemType.Collection)
                .withId(10L)
                .withLabel("BOOK")
                .withValue("some-book")
                .build();

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
        System.out.println(compositeDict.toSdos());
        System.out.println(compositeDict.toString());
        System.out.println(book.getChildren());

    }

}