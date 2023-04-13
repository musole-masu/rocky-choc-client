package org.example.tasks;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class SendMessage extends Thread {
    private DataOutputStream outputStream;
    private SendMessageResponse sendMessageResponse;

    private String message;
    public SendMessage(String message){
        this.message = message;
    }

    @Override
    public void run() {
        String msg = message;

        try {
            outputStream.writeUTF(msg);
            outputStream.flush();
            // progress ui to be implemented
        } catch (IOException e) {
            sendMessageResponse.showErrorSendingMessage();
            throw new RuntimeException(e);
        }
    }

    public void setSendMessageResponse(SendMessageResponse response){
        this.sendMessageResponse = response;
    }
    public void setOutputStream(DataOutputStream outputStream){
        this.outputStream = outputStream;
    }
    public interface SendMessageResponse{
        void showMessageSentConfirmation();
        void showErrorSendingMessage();
    }
}
