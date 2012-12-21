package kizwid.web.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * User: kizwid
 * Date: 2012-02-27
 */
public class CalcDigest {
    /**
     * calculate message digest with "SHA" algorithm
     *
     * @param text data to be calculated against
     */
    public static String calcDigest(String text) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA");
        md.update(text.getBytes());

        byte bytes[] = md.digest();
        StringBuilder sb = new StringBuilder(bytes.length * 2);

        sb.append(text.length());
        sb.append(' ');

        for (byte byt : bytes) {
            if (((int) byt & 0xff) < 0x10) sb.append("0");
            sb.append(Long.toHexString((int) byt & 0xff));
        }
        return sb.toString().replace(' ', 'x');
    }


}
