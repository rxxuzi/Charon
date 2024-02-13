package server;

import client.Messages;

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
    public static final Map<String, PrintWriter> clientMap = new HashMap<>();
    public static ServerSocket serverSocket;

    public static void main(String[] args) throws Exception {
        System.out.println("Server is running on port " + PORT + "...");

        // コマンドリッスンスレッドの立ち上げ
        new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (isRunning) {
                    if (!scanner.hasNextLine()) continue;
                    String commandLine = scanner.nextLine();
                    processCommand(commandLine);
                }
            } catch (Exception e) {
                System.out.println("Error handling server commands: " + e.getMessage());
            }
        }).start();

        serverSocket = new ServerSocket(PORT);

        try {
            while (isRunning) {
                Socket clientSocket = serverSocket.accept(); // This doesn't need to be in the try-with-resources
                new ClientHandler(clientSocket).start(); // Start a new thread for each client
            }
            serverSocket.close(); // サーバーソケットを閉じる
            System.exit(0); // プログラムを終了
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port " + PORT + " or listening for a connection");
            System.out.println(e.getMessage());
        } finally {
            serverSocket.close();
        }
        System.exit(0); // プログラムを終了
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
        } else if (commandLine.startsWith("/kick")) {
            String[] parts = commandLine.split(" ");
            if (parts.length == 2) {
                String targetId = parts[1];
                if (clientMap.containsKey(targetId)) {
                    PrintWriter targetOut = clientMap.get(targetId);
                    if (targetOut != null) {
                        targetOut.println(Messages.KICKED_OUT);
                        targetOut.close(); // ターゲットの出力ストリームを閉じる
                    }
                    clientMap.remove(targetId); // ターゲットをクライアントマップから削除
                    System.out.println("Client ID " + targetId + " has been kicked out.");
                } else {
                    System.out.println("Client ID " + targetId + " not found.");
                }
            }
        } else if (commandLine.equals("/kill")) {
            System.out.println("Server is shutting down...");
            isRunning = false;
            try {
                for (PrintWriter writer : clientMap.values()) {
                    writer.println(Messages.SERVER_SHUTDOWN);
                    writer.close();
                }
                clientMap.clear();   // クライアントマップを空にする
                serverSocket.close(); // サーバーソケットを閉じる
            } catch (Exception e) {
                System.out.println("Error while shutting down: " + e.getMessage());
            }
            System.exit(0); // プログラムを終了
        } else if (commandLine.equals("/view")) {
            // 接続しているクライアントのIDを表示
            for(int i = 0; i < clientMap.size(); i++) {
                System.out.println("Connected Client ID: " + clientMap.keySet());
            }
        } else if (commandLine.equals("/help")) {
            System.out.println("Available commands:");
            System.out.println("/send -a <message> - Send a message to all clients");
            System.out.println("/send <client-id> <message> - Send a message to a specific client");
            System.out.println("/kick <client-id> - Kick a client out of the server");
            System.out.println("/view - View connected clients");
            System.out.println("/kill - Shutdown the server");
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientId;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                clientId = in.readLine();
                clientMap.put(clientId, out);
                System.out.println("Client ID: " + clientId + " has connected.");

                String inputLine;
                while ((inputLine = in.readLine()) != null && isRunning) {
                    System.out.println("Received from " + clientId + ": " + inputLine);
                }
            } catch (IOException e) {
                if (isRunning) { // isRunning が true の場合のみエラーメッセージを表示
                    System.out.println("Exception in ClientHandler for client " + clientId + ": " + e.getMessage());
                }
            } finally {
                try {
                    if (out != null) out.close();
                    if (in != null) in.close();
                    clientSocket.close();
                } catch (IOException e) {
                    // ソケットクローズ時の例外は通常無視しても安全
                }
                clientMap.remove(clientId);
            }
        }
    }
}


