package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ChatServer {
    private static final int PORT = 12345;
    private static final Map<String, ClientHandler> clients = Collections.synchronizedMap(new HashMap<>());

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

                String line = in.readLine();
                if (line.startsWith("USER:")) {
                    userName = line.substring(5);
                    clients.put(userName, this);
                    broadcastUserList();
                }

                String msg;
                while ((msg = in.readLine()) != null) {
                    if (msg.startsWith("TO:")) {
                        String[] parts = msg.split(":", 3);
                        String targetUser = parts[1];
                        String content = parts[2];
                        ClientHandler targetHandler = clients.get(targetUser);
                        if (targetHandler != null) {
                            targetHandler.sendMessage(userName + ": " + content);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(userName + " disconnected.");
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {}
                if (userName != null) {
                    clients.remove(userName);
                    broadcastUserList();
                }
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        private void broadcastUserList() {
            String userList = "USERS:[" + String.join(",", clients.keySet().stream()
                    .map(name -> "\"" + name + "\"") // Surround names with quotes
                    .toList()) + "]";
            for (ClientHandler client : clients.values()) {
                client.sendMessage(userList);
            }
        }
    }
}