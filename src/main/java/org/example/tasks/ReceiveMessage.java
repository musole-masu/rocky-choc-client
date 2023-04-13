package org.example.tasks;

import org.example.ClientApplication;
import org.example.utils.CipherUtils;
import org.example.utils.LoggingMessage;

import java.io.DataInputStream;
import java.io.IOException;

public class ReceiveMessage extends Thread{
    private ClientApplication clientApplication;
    private boolean isClientConnected;
    private DataInputStream inputStream;
    private ReceiveMessageResponse receiveMessageResponse;

    public ReceiveMessage(ClientApplication clientApplication){
        this.clientApplication = clientApplication;
    }

    @Override
    public void run() {
        while (isClientConnected){
            String data;
            CipherUtils.wait(500);

            data = readMessage();

            if (data != null){
                int command = CipherUtils.retrieveCommand(data);
                String msg = CipherUtils.retrieveMessage(data);
                try {
                    performOperation(command, msg);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void performOperation(int cmd, String message) throws InterruptedException {
        if(cmd == CipherUtils.REQUEST_PUBLIC_KEY){
            // received server pub key
            LoggingMessage.printInStream("Client Received server public key ", LoggingMessage.UNLOCKED_PADLOCK);
            CipherUtils.wait(1000);
            byte[] decodedPublicKey = CipherUtils.decodeBase64(message);
            clientApplication.executeSecretKeyExchange(decodedPublicKey);
            LoggingMessage.printColoredText("Client about to generate its public key", LoggingMessage.ANSI_GREEN);

        } else if (cmd == CipherUtils.ENCRYPTED_MESSAGE){
            // start some progress action in the background
            CipherUtils.wait(2000);
            System.out.println("Message Encrypted " + message);
        } else if (cmd == CipherUtils.REQUEST_NEW_KEY){
            byte[] decodedPublicKey = CipherUtils.decodeBase64(message);
            clientApplication.executeSecretKeyExchange(decodedPublicKey);
        }
    }

    private String readMessage(){
        String msg = null;

        try {
            msg = inputStream.readUTF();
        } catch (IOException e) {
            // should find a proper way to print out error
            throw new RuntimeException(e);
        }

        return msg;

    }

    public void setClientConnected(boolean clientConnected) {

        isClientConnected = clientConnected;
    }

    public void setInputStream(DataInputStream inputStream) {

        this.inputStream = inputStream;
    }

    public void setReceiveMessageResponse(ReceiveMessageResponse receiveMessageResponse) {
        this.receiveMessageResponse = receiveMessageResponse;
    }

    public interface ReceiveMessageResponse{
        void notifyMessageReceived();
        void showEncryptedMessageReceived(String msg);
        void displayError(String err);
    }
}
