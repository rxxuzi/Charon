package client;

import static client.CltMessages.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.UUID;

public class CharonClient {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) {
        String clientId = UUID.randomUUID().toString();
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
                        notice("Server: " + fromServer);
                        if (fromServer.equals(CltMessages.KICKED_OUT) || fromServer.equals(CltMessages.SERVER_SHUTDOWN)) {
                            important(CltMessages.DISCONNECTED);
                            System.exit(1);
                        }

                    }
                } catch (IOException e) {
                    important("Error reading from server: " + e.getMessage());
                }
            }).start();

            String fromUser;
            while ((fromUser = stdIn.readLine()) != null && !socket.isClosed()) {
                if (fromUser.equalsIgnoreCase("/exit")) {
                    success(EXITING);
                    System.exit(0);
                    break; // /exit コマンドが入力された場合、ループを抜ける
                }
                out.println(fromUser);
            }
            // ソケットを閉じる
        } catch (IOException e) {
            important("Exception caught when trying to connect to " + HOST + " on port " + PORT + "\n" + e.getMessage());
        }
    }
}


