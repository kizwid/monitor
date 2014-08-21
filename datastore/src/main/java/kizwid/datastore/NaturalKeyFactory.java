package kizwid.datastore;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Set;
import static kizwid.datastore.Const.*;

/**
 * Created by kevin on 20/08/2014.
 */
public class NaturalKeyFactory {

    public static byte[] create(Set<DictItem> dictItems) {
        StringBuilder sb = new StringBuilder();
        for (DictItem dictItem : dictItems) {
            if(sb.length() > 0)sb.append(LF);
            sb.append(dictItem.toString());
        }
        return create(sb.toString());
    }

    public static byte[] create(RegularDictItem dictItem) {
        return create(dictItem.toString());
    }
    public static byte[] create(String text) {
        //return DigestUtils.sha256(text);
        return DigestUtils.sha1(text);
    }
}
