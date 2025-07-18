package Server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());

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
                    clients.add(this);
                }
                out.println("Welcome, " + userName + "! Type 'exit' to leave.");

                broadcast(userName + " has joined the chat.");

                String input;
                while ((input = in.readLine()) != null) {
                    if (input.equalsIgnoreCase("exit")) {
                        break;
                    }
                    broadcast(userName + ": " + input);
                }
            } catch (IOException e) {
                System.out.println(userName + " disconnected.");
            } finally {
                try { socket.close(); } catch (IOException e) {}
                synchronized (clients) {
                    clients.remove(this);
                }
                broadcast(userName + " has left the chat.");
            }
        }

        private void broadcast(String message) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    client.out.println(message);
                }
            }
        }
    }
}