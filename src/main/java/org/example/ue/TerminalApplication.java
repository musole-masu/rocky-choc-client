package org.example.ue;

import org.example.ClientApplication;
import org.example.TerminalResponseInterface;
import org.example.utils.CipherUtils;
import org.example.utils.LoggingMessage;

public class TerminalApplication implements TerminalResponseInterface, ClientApplication.TerminalApplicationResponse {
    private String serverAddress;
    private String serverPort;

    public ClientApplication clientApplication;
    private ClientApplication.TerminalApplicationResponse terminalApplicationResponse;


    public TerminalApplication(String serverAddress, int serverPort, ClientApplication clientApplication) throws InterruptedException {
        this.serverAddress = serverAddress;
        this.serverPort = Integer.toString(serverPort);

        this.clientApplication = clientApplication;
        clientApplication.setTerminalApplicationResponse(this);
        clientApplication.connect(serverAddress, this.serverPort);
    }

    @Override
    public void promptMessage(String s) {

    }

    @Override
    public void promptSuccessMessage(String s) {

    }

    @Override
    public void promptFailureMessage(String s) {

    }

    @Override
    public void connectionReady() {
    }

    @Override
    public void connectionClosed() {
    }

    @Override
    public void openTalk() {
        CipherUtils.wait(1000);
        LoggingMessage.printSucceededMessage("Server and Client Tunnel Opening ...");
        clientApplication.executeReceive();
        CipherUtils.wait(1000);
        LoggingMessage.printSucceededMessage("Client getting Ready to receive server public key " + "\uD83D\uDD13");

        try {
            clientApplication.ready();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void showConnectionProcess() {
        System.out.println("\nCONNECTING ...");
        for (int i = 0; i < 31; i++){
            CipherUtils.wait(50);
            System.out.print(LoggingMessage.ROCKET);
        }
        System.out.println("\n");
    }
}
