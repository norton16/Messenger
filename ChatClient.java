package chatapplication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;


final class ChatClient {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private final String server;
    private final String username;
    private final int port;

    private ChatClient(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    private ChatClient(int port, String username) {
        this("localhost", port, username);
    }

    private ChatClient(String username) {
        this("localhost", 1500, username);
    }

    private ChatClient() {
        this("localhost", 1500, "Anonymous");
    }

    /*
     * This starts the Chat Client
     */
    private boolean start() {
        // Create a socket
        try {
            socket = new Socket(server, port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create your input and output streams
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This thread will listen from the server for incoming messages
        Runnable r = new ListenFromServer();
        Thread t = new Thread(r);
        t.start();

        // After starting, send the clients username to the server.
        try {
            sOutput.writeObject(username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    /*
     * This method is used to send a ChatMessage Objects to the server
     */
    private void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * To start the Client use one of the following command
     * > java chatapplication.ChatClient
     * > java chatapplication.ChatClient username
     * > java chatapplication.ChatClient username portNumber
     * > java chatapplication.ChatClient username portNumber serverAddress
     *
     * If the portNumber is not specified 1500 should be used
     * If the serverAddress is not specified "localHost" should be used
     * If the username is not specified "Anonymous" should be used
     */
    public static void main(String[] args) {
        // Get proper arguments and override defaults

        // Create your client and start it
        ChatClient client;
        // = new chatapplication.ChatClient("localhost", 1500, "CS 180 Student");

        // Send an empty message to the server
        switch (args.length) {
            case 3:
                client = new ChatClient(args[2],Integer.parseInt(args[1]),args[0]);
                break;
            case 2:
                client = new ChatClient(Integer.parseInt(args[1]), args[0]);
                break;
            case 1:
                client = new ChatClient(args[0]);
                break;
            default:
                client = new ChatClient();
                break;

        }

        client.start();
        Scanner in = new Scanner(System.in);
        while (true) {
            String line = in.nextLine();
            ChatMessage cm2 = null;
            if (line.equalsIgnoreCase("/logout")) {
                ChatMessage cm = new ChatMessage(ChatMessage.logout, "logout");
                client.sendMessage(cm);
                try {
                    client.sInput.close();
                    client.sOutput.close();
                    client.socket.close();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.exit(0);
                break;
            } else if (line.contains(" ") && line.substring(0, line.indexOf(" ")).equals("/msg")) {
                String[] message = line.split(" ");
                String recipient = message[1];
                line = line.substring(line.indexOf(message[1]) + message[1].length() + 1);
                cm2 = new ChatMessage(ChatMessage.dm, line, recipient);
            } else if (line.equalsIgnoreCase("/list")) {

                cm2 = new ChatMessage(ChatMessage.list, "list");

            } else {
                cm2 = new ChatMessage(ChatMessage.broadcast, line);
            }

            client.sendMessage(cm2);
        }

    }




    /*
     * This is a private class inside of the chatapplication.ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     */
    private final class ListenFromServer implements Runnable {
        public void run() {
            while  (true) {
                try {
                    String msg = (String) sInput.readObject();
                    System.out.print(msg);
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("logged out");
                    break;
                }
            }
        }
    }
}
