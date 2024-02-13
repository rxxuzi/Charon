package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CharonServer {
    public static final int PORT = 12345;
    public static boolean isRunning = true;
    private static final Map<String, PrintWriter> clientMap = new HashMap<>();

    public static void main(String[] args) throws Exception {
        System.out.println("Server is running on port " + PORT + "...");

        // コマンドリッスンスレッドの立ち上げ
        new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    if (!scanner.hasNextLine()) continue;
                    String commandLine = scanner.nextLine();
                    processCommand(commandLine);
                    if (commandLine.equals("/kill")) {
                        break; // コマンドリッスンスレッドを終了
                    }
                    if(commandLine.equals("/view")) {
                        for(int i = 0; i < clientMap.size() ; i++){
                            System.out.println(clientMap.keySet().toArray()[i]);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error handling server commands: " + e.getMessage());
            }
        }).start();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (isRunning) {
                try (Socket clientSocket = serverSocket.accept();
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                    System.out.println("Client connected.");
                    String clientId = in.readLine();
                    System.out.println("Client ID: " + clientId + " has connected.");
                    clientMap.put(clientId, out);
                    out.println("Welcome to the Server, your ID is: " + clientId);

                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        System.out.println("Received from " + clientId + ": " + inputLine);
                        if (inputLine.equals("/exit")) {
                            break; // クライアントの接続を終了
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Exception caught when trying to listen on port " + PORT + " or listening for a connection");
                    System.out.println(e.getMessage());
                } finally {
                    if (!isRunning) {
                        serverSocket.close();
                    }
                }
            }
        }
    }

    private static void processCommand(String commandLine) {
        if (commandLine.startsWith("/send")) {
            // /send コマンドの処理
            String[] parts = commandLine.split(" ", 3);
            if (parts.length >= 3) {
                if (parts[1].equals("-a")) {
                    // 全クライアントにメッセージを送信
                    String messageToSend = parts[2];
                    for (PrintWriter writer : clientMap.values()) {
                        writer.println("Message to all: " + messageToSend);
                    }
                } else {
                    // 特定のクライアントにメッセージを送信
                    String targetClientId = parts[1];
                    String messageToSend = parts[2];
                    PrintWriter targetOut = clientMap.get(targetClientId);
                    if (targetOut != null) {
                        targetOut.println("Message: " + messageToSend);
                    } else {
                        System.out.println("Client ID " + targetClientId + " not found.");
                    }
                }
            }
        } else if (commandLine.equals("/kill")) {
            // サーバーをシャットダウン
            System.out.println("Server is shutting down...");
            isRunning = false;
            try {
                for (PrintWriter writer : clientMap.values()) {
                    writer.close();
                }
            } catch (Exception e) {
                System.out.println("Error while shutting down: " + e.getMessage());
            }
        }
    }
}


