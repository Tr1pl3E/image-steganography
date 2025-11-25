package org.example;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class CryptoUtil {


    // --- AES ---

    public static String encryptAES(String text, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, skey);
        byte[] encrypted = cipher.doFinal(text.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decryptAES(String encrypted, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, skey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encrypted));
        return new String(decrypted);
    }

    // --- DES ---

    public static String encryptDES(String text, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "DES");
        cipher.init(Cipher.ENCRYPT_MODE, skey);
        byte[] encrypted = cipher.doFinal(text.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decryptDES(String encrypted, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "DES");
        cipher.init(Cipher.DECRYPT_MODE, skey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encrypted));
        return new String(decrypted);
    }

    // --- XOR (Base64-safe) ---

    public static String encryptXOR(String text, String key) {
        if (key == null || key.isEmpty()) return text;

        char[] keyChars = key.toCharArray();
        char[] textChars = text.toCharArray();
        byte[] output = new byte[textChars.length];

        for (int i = 0; i < textChars.length; i++) {
            output[i] = (byte) (textChars[i] ^ keyChars[i % keyChars.length]);
        }

        return Base64.getEncoder().encodeToString(output);
    }

    public static String decryptXOR(String encrypted, String key) {
        if (key == null || key.isEmpty()) return encrypted;

        byte[] encryptedBytes = Base64.getDecoder().decode(encrypted);
        char[] keyChars = key.toCharArray();
        char[] output = new char[encryptedBytes.length];

        for (int i = 0; i < encryptedBytes.length; i++) {
            output[i] = (char) (encryptedBytes[i] ^ keyChars[i % keyChars.length]);
        }

        return new String(output);
    }
}
