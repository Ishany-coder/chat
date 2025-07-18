package Server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Map<String, ClientHandler> clients = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started at port " + PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            new ClientHandler(socket).start();
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private String userName;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("Enter your name:");
                userName = in.readLine();
                synchronized (clients) {
                    clients.put(userName, this);
                }
                out.println("Welcome, " + userName + "! You can now send messages in the format: recipient: message");

                String input;
                while ((input = in.readLine()) != null) {
                    if (input.equalsIgnoreCase("exit")) break;
                    int separator = input.indexOf(":");
                    if (separator != -1) {
                        String recipient = input.substring(0, separator).trim();
                        String message = input.substring(separator + 1).trim();
                        sendMessage(recipient, userName + ": " + message);
                    }
                }
            } catch (IOException e) {
                System.out.println(userName + " disconnected.");
            } finally {
                try { socket.close(); } catch (IOException e) { }
                synchronized (clients) {
                    clients.remove(userName);
                }
            }
        }

        private void sendMessage(String recipient, String message) {
            ClientHandler client = clients.get(recipient);
            if (client != null) {
                client.out.println(message);
            } else {
                out.println("User '" + recipient + "' not found.");
            }
        }
    }
}