package org.example;

import org.example.ue.TerminalApplication;
import org.example.utils.LoggingMessage;

import java.util.Scanner;

public class Application {

    public static void main(String[] args) throws InterruptedException {
        LoggingMessage.printWelcomeText("WELCOME TO ROCKY ROC CLIENT! ");
        String ipAddress;
        int port;
        Scanner sc = new Scanner(System.in);

        System.out.println("[--] ENTER SERVER INFO BELOW: " + LoggingMessage.POINT_DOWN);

        System.out.println(LoggingMessage.ANSI_RED+"[--] SERVER IP: "+LoggingMessage.ANSI_RESET);
        ipAddress = sc.nextLine();
        System.out.println(LoggingMessage.ANSI_YELLOW+"[--] PORT NUMBER: "+LoggingMessage.ANSI_RESET);
        port = sc.nextInt();

        ClientApplication clientApplication = new ClientApplication();
        new TerminalApplication(ipAddress, port, clientApplication);
    }
}
