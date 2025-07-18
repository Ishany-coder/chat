package Client;

import java.io.*;
import java.net.*;

public class ChatClient {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 12345);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

        String serverMessage = in.readLine();
        System.out.println(serverMessage);
        String username = userInput.readLine();
        out.println(username);

        System.out.println(in.readLine());

        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    if (!line.startsWith(username + ":")) {
                        System.out.println(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        String input;
        while ((input = userInput.readLine()) != null) {
            if (input.equalsIgnoreCase("exit")) {
                out.println("exit");
                break;
            }
            System.out.print("\r");  // Move cursor to start
            System.out.print("\033[2K");  // Clear the line (ANSI escape code)

            out.println(input);
        }

        socket.close();
        System.exit(0);
    }
}