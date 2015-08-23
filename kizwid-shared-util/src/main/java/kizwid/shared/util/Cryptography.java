package kizwid.shared.util;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Random;

/**
 * encrypt/decrypt based on public or private key
 * User: kizwid
 * Date: 2012-07-16
 * http://stackoverflow.com/questions/992019/java-256-bit-aes-password-based-encryption?lq=1
 * http://www.digizol.com/2009/10/java-encrypt-decrypt-jce-salt.html
 * http://www.javamex.com/tutorials/cryptography/pbe_salt.shtml
 */
public class Cryptography {

    private static final String CHARSET = "UTF-8";
    private static final String defaultCipherSpec = "AES/ECB/NoPadding"; //"AES/ECB/NoPadding"; AES/CBC/PKCS5Padding
    private final String cipherSpec;
    private final String algorithm;
    private static Random secureRandom;
    private final String password;

    public Cryptography(String password) throws UnsupportedEncodingException {
        this(password, defaultCipherSpec);

    }

    public Cryptography(String password, String cipherSpec) throws UnsupportedEncodingException {

        //hack to avoid max enstription length restriction
        //better to install the unrestricted policy
        //http://stackoverflow.com/questions/6481627/java-security-illegal-key-size-or-default-parameters
        //http://opensourceforgeeks.blogspot.in/2014/09/how-to-install-java-cryptography.html#sthash.wy6yAtkI.dpuf
        try {
            Field field = Class.forName("javax.crypto.JceSecurity").
                    getDeclaredField("isRestricted");
            field.setAccessible(true);
            field.set(null, java.lang.Boolean.FALSE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Security.insertProviderAt(new BouncyCastleProvider(), 1);
        secureRandom = new SecureRandom();
        this.password = password;
        this.cipherSpec = cipherSpec;
        algorithm = cipherSpec.split("/")[0];
    }


    public String encrypt(String plainText, String salt) throws InvalidKeySpecException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchProviderException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException {
        return doCipher(padBytes(plainText, 48), generateKey(password, salt.getBytes(CHARSET)), Cipher.ENCRYPT_MODE);
    }
    public String encrypt(String plainText) throws InvalidKeySpecException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchProviderException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException {
        byte[] salt = new byte[8];
        secureRandom.nextBytes(salt);
        salt = Hex.encode(salt);
        System.out.println("using salt: " + new String(salt));
        return doCipher(padBytes(plainText, 48), generateKey(password, salt), Cipher.ENCRYPT_MODE);
    }

    public String decrypt(String encrypted, String salt) throws InvalidKeyException, NoSuchProviderException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidKeySpecException, InvalidParameterSpecException {
        return doCipher(HexBin.decode(encrypted), generateKey(password, salt.getBytes(CHARSET)), Cipher.DECRYPT_MODE);
    }

    private String doCipher(byte[] bytesToCipher, Key key, int decryptMode) throws InvalidKeyException, NoSuchProviderException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException {
        Cipher cipher = getCipher();
        cipher.init(decryptMode, key);
        String encryptedHex = new String(Hex.encode(cipher.doFinal(bytesToCipher)), CHARSET);
        if(decryptMode == Cipher.ENCRYPT_MODE){
            return encryptedHex.toUpperCase();
        }else {
            return convertHexToString(encryptedHex).trim();
        }
    }

    private byte[] padBytes(String textToPad, int lengthToPad) throws UnsupportedEncodingException {
        byte[] paddedBytes = new byte[lengthToPad];
        byte[] in = textToPad.getBytes(CHARSET);
        if(in.length > lengthToPad){
            throw new IllegalArgumentException("Can only encrypt bytes upto a max length of " + lengthToPad);
        }
        Arrays.fill(paddedBytes, (byte)32);
        System.arraycopy(in, 0, paddedBytes, 0, in.length);
        return paddedBytes;
    }

    private String convertHexToString(String encryptedHex) {
        StringBuilder sb = new StringBuilder();
        for(int n = 0; n < encryptedHex.length()-1; n+=2){
            //read hex in pairs
            String hex = encryptedHex.substring(n, (n+2));
            //convert from hex to decimal
            int decimal = Integer.parseInt(hex, 16);
            sb.append((char)decimal);
        }
        return sb.toString();
    }

    private Key generateKey(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        /* Derive the key, given password and salt.*/
        //char[] password
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 10, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), algorithm);
        return  secret;
    }

    private Cipher getCipher() throws NoSuchProviderException, NoSuchAlgorithmException, NoSuchPaddingException {
        return Cipher.getInstance(cipherSpec, "BC");
    }

}
