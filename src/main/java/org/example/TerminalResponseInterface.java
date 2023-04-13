package org.example;

public interface TerminalResponseInterface {
    public void promptMessage( String s ); //Prints to screen
    public void promptSuccessMessage(String s);

    public void promptFailureMessage(String s);
    public void connectionReady(); //Connection is ready for messages
    public void connectionClosed();
}
