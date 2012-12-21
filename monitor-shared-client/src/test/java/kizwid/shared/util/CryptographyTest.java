package kizwid.shared.util;

import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: kevsanders
 * Date: 02/12/2012
 * Time: 23:09
 * To change this template use File | Settings | File Templates.
 */
public class CryptographyTest {

    private String test_data =
                    "secret,AAAAAAAA,Foo                                     ,B0731969FDE211C9C9249D02A3698242F72D68BA885E198423AA68D771BBDA3CF72D68BA885E198423AA68D771BBDA3C" + "\n" +
                    "secret,12345678,/System/Library/Java/JavaVirtualMachines,11ECE7B271600EA4C031D8866F103465F1C4E2DAD6FD989FA7A4F9887A801AE9B9995FB5173B76135E6EA6FDAC0FDA2E" + "\n" +
                    "E1F4E133DE134,12345678,/System/Library/Java/JavaVirtualMachines,EDBB83717CA6A4D79BE873173E4923DF91A3D93A08F1914BE3B6C7C1D270EE233A75D3D4503DC776DC32F8808BFBBC2A" + "\n" +
                    "";

    Cryptography crypt;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        crypt = new Cryptography("secret");
    }

    @Test
    public void testDecrypt() throws Exception {

        for (String row : test_data.split("\n")) {
            String[] data = row.split(",");
            String password = data[0];
            String salt = data[1];
            String plain = data[2].trim();
            String encrypted = data[3];
            crypt = new Cryptography(password);
            String act = crypt.encrypt(plain, salt);
            assertEquals(encrypted, act);
            assertEquals(plain, crypt.decrypt(act, salt));
        }


        {
            crypt = new Cryptography("top_secret");
            String encryptedText = crypt.encrypt("foo bar baz", "19816304");
            System.out.println(encryptedText);

            String plainText = crypt.decrypt("F06CF3AC30DA377D025A53069D75CBBECA2E920986152C055C3ABACBB90C8158CA2E920986152C055C3ABACBB90C8158", "19816304");
            System.out.println(plainText);
        }

        {
            crypt = new Cryptography("top_secret");
            String encryptedText = crypt.encrypt("foo bar baz", "9900bf00d8212c3e");
            System.out.println(encryptedText);

            String plainText = crypt.decrypt("7A7E8F45EBE1A91FD9B34916C29E331AAC84D89947D24ECE9BF2C63F9E6DC9F1AC84D89947D24ECE9BF2C63F9E6DC9F1", "9900bf00d8212c3e");
            System.out.println(plainText);
        }

        {
            crypt = new Cryptography("top_secret");
            String encryptedText = crypt.encrypt("foo bar baz");
            System.out.println(encryptedText);

            String plainText = crypt.decrypt("7A7E8F45EBE1A91FD9B34916C29E331AAC84D89947D24ECE9BF2C63F9E6DC9F1AC84D89947D24ECE9BF2C63F9E6DC9F1", "9900bf00d8212c3e");
            System.out.println(plainText);
        }

    }





}
