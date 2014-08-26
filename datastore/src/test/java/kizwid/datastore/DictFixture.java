package kizwid.datastore;

import kizwid.datastore.biz.Trade;
import kizwid.datastore.biz.TradeType;
import org.apache.commons.codec.binary.Hex;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by kevin on 24/08/2014.
 */
public class DictFixture {

    public static Dict createStandard(){
        return createStandard(createBook());
    }
    public static Dict createStandard(Set<Trade> trades){
        return createStandard(createBook(trades));
    }
    public static Dict createStandard(DictItem book){
        return new Dict.DictBuilder()
                .withId(1L)
                .withDictItems(new LinkedHashSet<DictItem>())
                .withDictItem(new RegularDictItem("MODE", "PRICE", "", DictItemType.Instruction, Collections.<DictItem>emptySet()))
                .withDictItem(new RegularDictItem("MODEL", "DBOPT", "xyz", DictItemType.Sdos, Collections.<DictItem>emptySet()))
                .withDictItem(new RegularDictItem("CURVE", "EUR", "abc", DictItemType.Sdos, Collections.<DictItem>emptySet()))
                .withDictItem(book)
                .withDictItem(new RegularDictItem("RESULTS", "yyy", "", DictItemType.Instruction, Collections.<DictItem>emptySet()))
                .withDictItem(new RegularDictItem("GRIMIS_BOOK", "xxx", "", DictItemType.Instruction, Collections.<DictItem>emptySet()))
                .build();
    }

    public static DictItem createBook() {
        Set<Trade> trades = new LinkedHashSet<Trade>();
        trades.add(TradeFixture.createTrade(20140426, 20150426, 100f, 0.4f, TradeType.FXFWD));
        trades.add(TradeFixture.createTrade(20140426, 20150426, 100f, 0.4f, TradeType.FXSPOT));
        trades.add(TradeFixture.createTrade(20140426, 20150426, 100f, 0.4f, TradeType.FIXFLOATSWAP));
        return createBook(trades);
    }
    public static DictItem createBook(Set<Trade> trades) {
        Set<DictItem> tradeItems = new LinkedHashSet<DictItem>(trades.size());
        for (Trade trade : trades) {
            tradeItems.add(new RegularDictItem("SECURITY", trade.getName(), Hex.encodeHexString(trade.getNaturalKey()), DictItemType.Sdos, Collections.<DictItem>emptySet()));
        }
        return new RegularDictItem.DictItemBuilder()
                    .withChildren(tradeItems)
                    .withVersion(Hex.encodeHexString(NaturalKeyFactory.create(tradeItems)))
                    .withDictItemType(DictItemType.Collection)
                    .withId(10L)
                    .withLabel("BOOK")
                    .withValue("some-book")
                    .build();
    }
}
