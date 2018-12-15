package edu.gvsu.restapi.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

import edu.gvsu.restapi.common.RegistrationInfo;	


public class App {
	
    public static void main( String[] args ){
    	
    	String address = "localhost";
        try {
        	
        	address = InetAddress.getLocalHost().getHostAddress();
        
        	BufferedReader is = new BufferedReader(new InputStreamReader(System.in)); 
        	System.out.println("Please enter your name.");
	        String username = is.readLine();
	        
	        ChatClient client = new ChatClient();
	   
	        System.out.println("What port will you receive messages on?");
	        Integer listenPort = Integer.parseInt(is.readLine());
	        
	        RegistrationInfo regInfo = new RegistrationInfo(username, address, listenPort, true);
	        Thread listenerThread = new Thread(new ClientListener(listenPort));
	        listenerThread.start();

            RegistrationInfo extantUser = client.lookup(username);
            if (extantUser == null) {
                client.register(regInfo);
            } else {
                System.out.println("Server already has a user by that name");
                System.exit(1);
            }

            Boolean running = true;
            while (running) {
                System.out.println("What would you like to do?");
                System.out.println("Enter 'friends' - list all available users");
                System.out.println("Enter 'talk {username} {message}' - send a message to user");
                System.out.println("Enter 'broadcast {message}' - send a message to all users");
                System.out.println("Enter 'busy' - set your status to 'busy'");
                System.out.println("Enter 'available' - set your status to 'available'");
                System.out.println("Enter 'exit' - exit the chat\n\n");
                
                String input = is.readLine();
                String[] inputTokens = input.split(" ");
                if (inputTokens.length < 1) {
                    inputTokens = new String[1];
                    inputTokens[0] = "";
                }
                switch (inputTokens[0]) {
                    case "friends":
                        RegistrationInfo[] users = client.listRegisteredUsers();
                        for (RegistrationInfo user : users) {
                            System.out.println(user.getUserName() + " :: online: " + user.getStatus() + "\n");
                        }
                        break;
                    case "talk":
                    	String message = "";
                        String destinationUser = inputTokens[1];
                        RegistrationInfo destUser = client.lookup(destinationUser);
                        if(destUser.getStatus()) {
	                        
	                        for (int i = 2; i < inputTokens.length; i++) {
	                            message += inputTokens[i] + " ";
	                        }
                        }
                        sendMessageToUser(username, message, client, destinationUser);
                        break;
                        
                    case "broadcast":
                    	String bmessage = "";
                        System.out.println("broadcast");
                        RegistrationInfo[] allUsers = client.listRegisteredUsers();
                       
                        for (int i = 1; i < inputTokens.length; i++) {
                        	bmessage += inputTokens[i] + " ";
                        }
                        
                        for (RegistrationInfo user : allUsers) {
                            if (!user.getUserName().equals(username) && user.getStatus()) {
                                sendMessageToUser(username, " (broadcast) " + bmessage, client, user.getUserName());
                            }
                        }
                        break;
                    case "busy":
                        RegistrationInfo info = client.lookup(username);
                        if (info != null) {
                            regInfo.setStatus(false);
                            client.setStatus(username, false);
                            System.out.println("Set user:- " + username + " to 'busy'\n");
                        }
                        break;
                    case "available":
                        RegistrationInfo info1 = client.lookup(username);
                        if (info1 != null) {
                            regInfo.setStatus(true);
                            client.setStatus(username, true);
                            System.out.println("Set user:-" + username + " to 'available'\n");
                        }
                        break;
                    case "exit":
                        System.out.println("Bye!");
                        client.unregister(username);
                        System.exit(0);
                    default:
                        System.out.println("Sorry, didn't understand that\n");
                }
            }
        } catch (Exception e) {
            System.err.println("Client exception:");
            e.printStackTrace();
        }
    }

    private static void sendMessageToUser(String username, String message, ChatClient client, String destinationUser) throws Exception {
    
    	RegistrationInfo destUserInfo = client.lookup(destinationUser);
        
    	if (destUserInfo != null) {
        	Boolean talkTargetStatus = destUserInfo.getStatus();
            if (talkTargetStatus) {
            	String destHost = destUserInfo.getHost();
                int destPort = destUserInfo.getPort();
                Socket clientSocket = new Socket(destHost, destPort);
                DataOutputStream os = new DataOutputStream(clientSocket.getOutputStream());
                os.writeBytes(username + " : " + message);
                os.close();
                clientSocket.close();
            }
        }
    }

}