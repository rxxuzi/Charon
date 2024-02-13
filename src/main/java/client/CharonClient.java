package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class CharonClient {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;
    private static final String ID = "user";

    public static void main(String[] args) {
        String clientId = "user";
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter client ID : ");
        if (sc.hasNextLine()) {
            clientId = sc.nextLine();
        }
        System.out.println("Client ID: " + clientId);

        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            out.println(clientId);

            new Thread(() -> {
                try {
                    String fromServer;
                    while ((fromServer = in.readLine()) != null) {
                        System.out.println("Server: " + fromServer);
                        if (fromServer.equals(Messages.KICKED_OUT) || fromServer.equals(Messages.SERVER_SHUTDOWN)) {
                            System.out.println(Messages.DISCONNECTED);
                            System.exit(1);
                        }

                    }
                } catch (IOException e) {
                    System.out.println("Error reading from server: " + e.getMessage());
                }
            }).start();

            String fromUser;
            while ((fromUser = stdIn.readLine()) != null && !socket.isClosed()) {
                if (fromUser.equalsIgnoreCase("/exit")) {
                    System.out.println("Exiting...");
                    break; // /exit コマンドが入力された場合、ループを抜ける
                }
                out.println(fromUser);
            }
            // ソケットを閉じる
        } catch (IOException e) {
            System.out.println("Exception caught when trying to connect to " + HOST + " on port " + PORT);
            System.out.println(e.getMessage());
        }
    }
}


