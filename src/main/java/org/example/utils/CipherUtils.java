package org.example.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class CipherUtils {
    // a symmetric-key encryption algorithm that Cipher can use
    public static String AES = "AES";

    public static String PROTOCOL_SEP = "@@";

    public static int COMMAND_INDEX = 0;
    public static int MESSAGE_INDEX = 1;
    public static int PUBLIC_KEY = 2;
    public static int ENCRYPTED_MESSAGE = 3;
    public static int REQUEST_NEW_KEY = 4;
    public static int REQUEST_PUBLIC_KEY = 5;
    public static int CLOSE_CONNECTION = 6;
    public static int READY = 7;

    public static String encodeBase64(byte[] data){
        return Base64.encodeBase64String(data);
    }

    public static byte[] decodeBase64(String data){
        return Base64.decodeBase64(data);
    }

    public synchronized static String decrypt(String encryptedMessage, SecretKeySpec aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);

        LoggingMessage.printProgressiveAction("Decrypting ... "+encodeBase64(aesKey.getEncoded()));

        byte[] decodedMessage = decodeBase64(encryptedMessage);
        byte[] recovered = cipher.doFinal(decodedMessage);

        return new String(recovered);
    }

    public synchronized static String encrypt(String message, SecretKeySpec aesKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);

        byte[] plainText = message.getBytes();
        byte[] cipherText = cipher.doFinal(plainText);

        return encodeBase64(cipherText);
    }

    public synchronized static String decrypt(String encryptedMessage, String aesKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(AES);

        byte[] key = aesKey.getBytes();

        SecretKeySpec secretKeySpec = new SecretKeySpec(key, AES);

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

        byte[] decodedMessage = Base64.decodeBase64(encryptedMessage);
        byte[] recovered = cipher.doFinal(decodedMessage);

        return new String(recovered);
    }

    public synchronized static String encrypt(String message, String aesKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(AES);
        byte[] key = aesKey.getBytes();

        SecretKeySpec secretKeySpec = new SecretKeySpec(key, AES);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        byte[] plainText = message.getBytes();
        byte[] cipherText = cipher.doFinal(plainText);

        return encodeBase64(cipherText);
    }

    public static SecretKeySpec generateAESKey(byte[] secretKey){
        return new SecretKeySpec(secretKey, 0, 16, AES);
    }

    public static void wait(int timing){
        try {
            Thread.sleep(timing);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static int retrieveCommand(String data){
        return Integer.parseInt(data.split(PROTOCOL_SEP)[COMMAND_INDEX]);
    }

    public static String retrieveMessage(String data){
        return data.split(PROTOCOL_SEP)[MESSAGE_INDEX];
    }



}
