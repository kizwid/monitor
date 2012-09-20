package kizwid.shared.util;

/**
 * mock implementation
 * User: kizwid
 * Date: 2012-07-16
 */
public class Cryptography {
    public static String decrypt(String encrypted, String encryptKey) {
        return encrypted;
    }

    public static String decrypt(String encrypted) {
        return decrypt(encrypted, "");
    }
}
