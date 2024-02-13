package server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class CharonServer {
    public static final int PORT = 12345;
    public static void main(String[] args) {
        System.out.println("Server is running on port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT);
             Socket clientSocket = serverSocket.accept();
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            System.out.println("Client connected.");

            String inputLine, outputLine;

            // 初期メッセージをクライアントに送信
            out.println("Welcome to the Server!");

            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received from client: " + inputLine);
                outputLine = "Echo from server: " + inputLine;
                out.println(outputLine); // クライアントにエコーバック

                // 特定の条件でループを抜ける
                if (inputLine.equals("bye")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + PORT + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}

