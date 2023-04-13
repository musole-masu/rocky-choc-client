package org.example.d_hellman;

import org.apache.commons.codec.binary.Base64;
import org.example.utils.CipherUtils;
import org.example.utils.LoggingMessage;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class SecretKeyExchange extends Thread{
    private PublicKey publicKey;
    private PrivateKey privateKey;

    private KeyFactory keyFactory;
    private KeyPair keyPair;
    private KeyAgreement keyAgreement;
    private KeyPairGenerator keyPairGenerator;
    private X509EncodedKeySpec x509EncodedKeySpec;

    private byte[] commonSecret;
    private SendKey sender;

    private byte[] bytePubKey;
    private volatile String strPrivateKey;

    public SecretKeyExchange(SendKey send, byte[] pbk){
        this.sender = send;
        this.bytePubKey = pbk;

    }

    public PublicKey receivePBKFromServer(byte[] publicKey) throws NoSuchAlgorithmException, InterruptedException, InvalidKeySpecException {
        keyFactory = KeyFactory.getInstance("DH");
        x509EncodedKeySpec = new X509EncodedKeySpec(publicKey);

        LoggingMessage.printProgressiveAction("Generating Keys ..");
        PublicKey serverPublicKey = keyFactory.generatePublic(x509EncodedKeySpec);

        try{
            generateDHKeyPair(serverPublicKey);
        } catch (InvalidAlgorithmParameterException e){
            e.printStackTrace();
        }
        return serverPublicKey;
    }

    public DHParameterSpec retrieveDHParamFromPB(PublicKey key){
        return ((DHPublicKey) key).getParams();
    }

    public void generateDHKeyPair(PublicKey serverPublicKey) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        DHParameterSpec DHParam = retrieveDHParamFromPB(serverPublicKey);

        keyPairGenerator = KeyPairGenerator.getInstance("DH");
        keyPairGenerator.initialize(DHParam);
        keyPair = keyPairGenerator.generateKeyPair();

        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();

        try {
            initDHKeyAgreement();
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public void initDHKeyAgreement() throws NoSuchAlgorithmException, InvalidKeyException {
        this.privateKey = keyPair.getPrivate();

        keyAgreement = KeyAgreement.getInstance("DH");
        keyAgreement.init(privateKey);
    }

    public void doPhase(PublicKey publicKey) throws InvalidKeyException {
        keyAgreement.doPhase(publicKey, true);
    }
    public PublicKey getPublicKey(){
        return publicKey;
    }
    public SecretKeySpec getAESKey(){
        return CipherUtils.generateAESKey(commonSecret);
    }

    @Override
    public void run() {
        PublicKey publicKey;

        try {
            CipherUtils.wait(5000);

            publicKey = receivePBKFromServer(bytePubKey);

            // send public key to server
            sender.sendPublicKey(getPublicKey());

            LoggingMessage.printProgressiveAction("Establishing a shared secret key ...");
            CipherUtils.wait(5000);

            doPhase(publicKey);
            this.commonSecret = keyAgreement.generateSecret();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        this.strPrivateKey = Base64.encodeBase64String(CipherUtils.generateAESKey(commonSecret).getEncoded());
        System.out.println("*****************************************************************");
        LoggingMessage.printColoredText(LoggingMessage.CHEER_BEER +" Common Shared Secret Successfully Generated", LoggingMessage.ANSI_RED);
        System.out.println("*****************************************************************");
    }

    public String getStrPrivateKey() {
        return strPrivateKey;
    }

    public interface SendKey{
        void sendPublicKey(PublicKey publicKey);
        void sendError(String error);
        void showPrivateKey(String s);
    }
}
