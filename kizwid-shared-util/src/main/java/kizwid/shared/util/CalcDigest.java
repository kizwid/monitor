package kizwid.shared.util;

import org.apache.commons.io.input.ReaderInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * User: kizwid
 * Date: 2012-02-27
 */
public class CalcDigest {
    final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    /**
     * calculate message digest with "SHA" algorithm
     *
     * @param text data to be calculated against
     */
    public static String checksum(String text) throws NoSuchAlgorithmException, IOException {
        return checksum(new StringReader(text));
    }

    public static String checksum(Reader reader) throws NoSuchAlgorithmException, IOException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        InputStream fis = new ReaderInputStream(reader);
        byte[] data = new byte[1024];
        int read;
        while ((read = fis.read(data)) != -1) {
            sha1.update(data, 0, read);
        };
        return bytesToHex(sha1.digest());
    }

   public static String bytesToHex(byte[] bytes) {
       char[] hexChars = new char[bytes.length * 2];
       int v;
       for ( int j = 0; j < bytes.length; j++ ) {
           v = bytes[j] & 0xFF;
           hexChars[j * 2] = hexArray[v >>> 4];
           hexChars[j * 2 + 1] = hexArray[v & 0x0F];
       }
       return new String(hexChars);
   }

}
