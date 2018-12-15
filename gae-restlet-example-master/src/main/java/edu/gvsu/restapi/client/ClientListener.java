package edu.gvsu.restapi.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientListener implements Runnable{

    private Integer listenPort;

    public ClientListener(Integer listenPort) {
        this.listenPort = listenPort;
    }

    public void run() {
        ServerSocket echoServer;
        Socket clientSocket;

        try {
            echoServer = new ServerSocket(this.listenPort);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

        while(true) {
            try {
                clientSocket = echoServer.accept();
                Thread thread  = new Thread(new IncomingRequest(clientSocket));
                thread.start();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }
}