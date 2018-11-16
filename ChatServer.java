package chatapplication;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

import static java.lang.Integer.parseInt;

final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    private String fileName;
    private ChatFilter cf;

    private ChatServer(int port, String words_to_filter){
        this.port = port;
        this.fileName = words_to_filter;
        this.cf = new ChatFilter(fileName);
    }

    private ChatServer(int port) {
        this(port, "src\\badwords.txt");
    }

    private ChatServer(){
        this(1500, "src\\badwords.txt");
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while(true) {
                Socket socket = serverSocket.accept();
                Runnable r = new ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);
                clients.add((ClientThread) r);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        ChatServer server;
        try {
            if (args.length == 1) {
                server = new ChatServer(parseInt(args[0]));
            } else if (args.length == 2) {
                server = new ChatServer(parseInt(args[0]), args[1]);
            } else if(args.length >2){
                server = new ChatServer();
                System.out.println("Please Enter a valid command:");
                System.out.println("\"java ChatServer <port> <name>\"");
                System.exit(1);
            } else {
                server = new ChatServer(1500);
            }
            server.start();
        } catch ( Exception e ){

            System.out.println("Please Enter a valid command:");
            System.out.println("\"java ChatServer <port> <name>\"");
            System.exit(1);
        }


    }


    /*
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     */
    private final class ClientThread implements Runnable  {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;
        String pattern = "HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String date = simpleDateFormat.format(new Date());
        boolean running = true;

        private boolean writeMessage(String msg)
        {
            boolean write = false;
            if(!socket.isConnected())
            {
                write = false;
            }
            else
            {
                try {
                    if(sInput != null)
                    {
                        sOutput.writeObject(msg);
                    }
                    write = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return write;
        }

        private synchronized void broadcast(String message)
        {
            StringBuilder list = new StringBuilder((date + " list of users connected right now: "));
            message = cf.filter(message);
            System.out.println(message);
            if(message.equals("/list"))
            {


                for (ClientThread c: clients)
                {
                    if(c.username.equals(username))
                    {
                        list.append("");
                    }
                    else
                    {
                        list.append(username).append(", ");
                    }

                }
                list.replace(list.length()-2, list.length()-1, "");
                System.out.println(list);

            }
            else
            {
                for (ClientThread c: clients)
                {
                    c.writeMessage(date + " " + username + ": " + message + '\n');
                }
            }
        }

        private synchronized void remove(int id)
        {
            clients.remove(this);
        }

        private boolean getRunning(){return running;}

        private boolean directMessage(String message, String username){
            message = cf.filter(message);
            boolean sent = false;
            if(this.username.equals( username)){
                return sent;
            }
            else {

                for (int i= 0; i < clients.size(); i++){
                    if(clients.get(i).getUsername().equals(username)){
                        String toSend = date + " " +this.username +" -> " + clients.get(i).getUsername() + ": "+  message+"\n";
                        clients.get(i).writeMessage(toSend);
                        System.out.print(toSend);
                        this.writeMessage(toSend);
                        sent = true;
                    }
                }
            }
            return sent;
        }

        public String getUsername(){
            return username;
        }

        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            for (ClientThread c : clients){
                if(username.equals(c.getUsername())){
                    this.running = false;
                    this.writeMessage("Logout Successful.");
                }
            }

        }

        private void close(){
            writeMessage("Logout Successful.");
            running = false;
            try {
                socket.close();
                sInput.close();
                sOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            // Read the username sent to you by client
            while(running) {
                try {
                    cm = (ChatMessage) sInput.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
               // System.out.println(username + ": Ping");


                // Send message back to the client
                try {
                    if (cm.getMessType() == ChatMessage.broadcast) {
                        broadcast(cm.getMessage());
                    } else if (cm.getMessType() == ChatMessage.dm){
                        directMessage(cm.getMessage(), cm.getRecipient());
                    } else if (cm.getMessType() == ChatMessage.list){
                        String list = date + " List of users connected: ";
                        for (int i = 0; i < clients.size(); i ++){
                            
                            if(i < clients.size() -1 && !clients.get(i).getUsername().equals(username) ){
                                list += clients.get(i).getUsername() + ", ";
                            }
                            else if (!clients.get(i).getUsername().equals(username) ){
                                list += clients.get(i).getUsername();
                            }
                        }
                        this.writeMessage(list);
                    }
                    else {
                        clients.remove(this);
                        this.close();
                        running = false;
                    }
                    //sOutput.writeObject("Pong");
                } catch (Exception e) {
                   System.out.println("Logout Successful.");
                }
            }
        }
    }
}
