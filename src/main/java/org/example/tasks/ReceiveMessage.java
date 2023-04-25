package org.example.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.ClientApplication;
import org.example.utils.CipherUtils;
import org.example.utils.LoggingMessage;
import org.example.utils.SearchResult;
import org.example.utils.SearchResults;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

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

            if (message.split("##")[0].equals("queryResult")){
                try {
                    String decryptedQueryResponse = clientApplication.decrypt(message.split("##")[1]);

                    ObjectMapper objectMapper = new ObjectMapper();
                    SearchResults searchResults = objectMapper.readValue(decryptedQueryResponse, SearchResults.class);

                    LoggingMessage.printInStream("Query result from server: ", LoggingMessage.THUMBS_UP);
                    // Print the search results
                    List<SearchResult> results = searchResults.getResults();
                    for (SearchResult result : results) {
                        LoggingMessage.printColoredText(result.getPosition() + " - " + result.getTitle() + " - " + result.getUrl()+ " ==> "+result.getDescription(), LoggingMessage.ANSI_CYAN);
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            LoggingMessage.printColoredText("Type yes for a new query", LoggingMessage.ANSI_GREEN);

            Scanner sc = new Scanner(System.in);
            String yes = sc.nextLine();

            if (yes.equals("yes")){
                LoggingMessage.printColoredText( "FILL THE EMPTY SPACE BELOW WITH A SPECIFIC SEARCH QUERY "+LoggingMessage.CLOSED_MAILBOX+ ":", LoggingMessage.ANSI_GREEN);
                String toServer = sc.nextLine();

                try {
                    LoggingMessage.printProgress("ENCRYPTING DATA ...", LoggingMessage.CLOSED_LOCK_WITH_KEY);

                    String encryptedData = clientApplication.encrypt(toServer);
                    LoggingMessage.printColoredText("Encrypted Data: " + encryptedData, LoggingMessage.ANSI_GREEN);
                    LoggingMessage.printProgress("Sending Encrypted Data", LoggingMessage.PACKAGE);
                    clientApplication.sendMessage(encryptedData);
                    LoggingMessage.printOutStream("Data Sent to server");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("exit");
            }

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
