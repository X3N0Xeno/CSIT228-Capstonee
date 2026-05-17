package com.capstone.javabox.csit228.games;

public class JavaboxAbstractController {

    protected Runnable quitCallback;
    public void setQuitCallback(Runnable quitCallback) {
        this.quitCallback = quitCallback;
    }

    protected void quitToLobby() {
        if (quitCallback != null) {
            quitCallback.run();
        } else {
            System.err.println("CRITICAL ERROR: Quit callback was never set for this controller!");
            System.err.println("This happens because you might have:");
            System.err.println("Extended from this class but your launcher hasn't implemented JavaboxGame interface,");
            System.err.println("The Runnable became 'lost' which may happen if you are manually switching the scenes and did not set up setQuitCallback()");
        }
    }
}
