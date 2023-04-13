package org.example;

import org.apache.commons.codec.binary.Base64;
import org.example.d_hellman.SecretKeyExchange;
import org.example.tasks.ReceiveMessage;
import org.example.tasks.SendMessage;
import org.example.utils.CipherUtils;
import org.example.utils.LoggingMessage;

import java.io.*;
import java.net.Socket;
import java.security.Principal;
import java.security.PublicKey;

public class ClientApplication implements SecretKeyExchange.SendKey{
    private Socket clientSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private boolean isClientConnected;
    private SecretKeyExchange secretKeyExchange;
    private ReceiveMessage receiveMessage;
    private ReceiveMessage.ReceiveMessageResponse receiveMessageResponse;
    private SendMessage.SendMessageResponse sendMessageResponse;
    private TerminalApplicationResponse terminalApplicationResponse;

    //private ConnectionResponse connectionResponse;

    public ClientApplication() {

        this.receiveMessage = new ReceiveMessage(this);
    }

    public void setTerminalApplicationResponse(TerminalApplicationResponse terminalApplicationResponse) {
        this.terminalApplicationResponse = terminalApplicationResponse;
    }

    public void executeSecretKeyExchange(byte[] pubKey) throws InterruptedException {
        secretKeyExchange = new SecretKeyExchange(this, pubKey);
        secretKeyExchange.start();
    }

    public void executeReceive(){
        receiveMessage.setReceiveMessageResponse(receiveMessageResponse);
        receiveMessage.setClientConnected(isClientConnected);
        receiveMessage.setInputStream(inputStream);
        receiveMessage.start();
    }

    public void executeSend(String msg) throws Exception {
        if (msg.isEmpty())
            throw new Exception("YOU MUST ENTER A MESSAGE.");

        SendMessage sendMessage;
        sendMessage = new SendMessage(msg);
        sendMessage.setSendMessageResponse(sendMessageResponse);
        sendMessage.setOutputStream(outputStream);
        sendMessage.start();
    }

    public String decrypt(String msg) throws Exception {
        return CipherUtils.decrypt(msg, secretKeyExchange.getAESKey());
    }

    public String encrypt(String msg) throws Exception{
        return CipherUtils.encrypt(msg, secretKeyExchange.getAESKey());
    }

    public void sendMessage(String msg) throws Exception {
        executeSend(CipherUtils.ENCRYPTED_MESSAGE+CipherUtils.PROTOCOL_SEP+msg);
    }

    public void ready() throws Exception {
        CipherUtils.wait(1000);
        executeSend(CipherUtils.READY+CipherUtils.PROTOCOL_SEP+"ready");
        // sending to server
        LoggingMessage.printOutStream("Client Sent its ready state to the server");

    }
    public void connect(String ipAddress, String port) throws InterruptedException {
        Connection connect = new Connection(ipAddress, port);
        connect.start();
    }


    @Override
    public void sendPublicKey(PublicKey publicKey) {
        byte[] encodedPubKey = publicKey.getEncoded();
        String base64PubKey = Base64.encodeBase64String(encodedPubKey);
        String formatPBK = CipherUtils.PUBLIC_KEY+CipherUtils.PROTOCOL_SEP+base64PubKey;

        try {
            executeSend(formatPBK);
            LoggingMessage.printOutStream("Client Public Key sent to Server "+ LoggingMessage.UNLOCKED_PADLOCK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendError(String error) {

    }

    @Override
    public void showPrivateKey(String s) {

        System.out.printf("PRINTING PRIVATE KEY: "+s);
    }

    private void initStreams(Socket socket){
        try {
            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeConnection(){
        try {
            outputStream.close();
            inputStream.close();
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private class Connection extends Thread{
        private String ip;
        private String port;

        public Connection(String ip, String port){
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void run() {

            terminalApplicationResponse.showConnectionProcess();

            try {

                clientSocket = new Socket(ip, Integer.parseInt(port));
                LoggingMessage.printSucceededMessage("CLIENT SUCCESSFULLY CONNECTED");
                isClientConnected = true;
                Thread.sleep(1000);
                initStreams(clientSocket);

            } catch (IOException | InterruptedException e) {
                isClientConnected = false;
                LoggingMessage.printFailedConnect("COULD NOT CONNECT, CHECK IP AND PORT");
                closeConnection();
            }

            if (isClientConnected){
                terminalApplicationResponse.openTalk();
            }
        }

    }



    public boolean isClientConnected(){
        return isClientConnected;
    }
    public void setReceiveMessageResponse(ReceiveMessage.ReceiveMessageResponse receiveMessageResponse){
        this.receiveMessageResponse = receiveMessageResponse;
    }
    public void setSendMessageResponse(SendMessage.SendMessageResponse sendMessageResponse){
        this.sendMessageResponse = sendMessageResponse;
    }

    public interface TerminalApplicationResponse{
        void openTalk();
        void showConnectionProcess();
    }
   // traffic interface here
}

