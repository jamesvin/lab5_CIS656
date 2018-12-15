package edu.gvsu.restapi.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class IncomingRequest implements Runnable {
    private Socket clientSocket;

    public IncomingRequest(Socket clientSocket) {
        super();
        this.clientSocket = clientSocket;
    }

    public void run() {
        String line;
        BufferedReader is;

        try {
            is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            while(true) {
                line = is.readLine();
                if(line == null) {
                    break;
                }
                System.out.println(line);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}