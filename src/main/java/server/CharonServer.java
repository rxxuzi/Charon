package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CharonServer {
    public static final int PORT = 12345;
    public static boolean isRunning = true;
    public static final Map<String, PrintWriter> clientMap = new HashMap<>();
    public static Map<String, Boolean> muteMap = new ConcurrentHashMap<>();

    public static ServerSocket svrSocket;
    public static final long serverStartTime = System.currentTimeMillis();

    public static final List<String> msgList = new ArrayList<>();

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

        svrSocket = new ServerSocket(PORT);

        try {
            while (isRunning) {
                Socket clientSocket = svrSocket.accept(); // This doesn't need to be in the try-with-resources
                new ClientHandler(clientSocket).start(); // Start a new thread for each client
            }
            svrSocket.close(); // サーバーソケットを閉じる
            System.exit(0); // プログラムを終了
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port " + PORT + " or listening for a connection");
            System.out.println(e.getMessage());
        } finally {
            svrSocket.close();
        }
        System.exit(0); // プログラムを終了
    }

    private static void processCommand(String commandLine) {
        if(commandLine.startsWith("/")){
            SvrCommand.execute(commandLine);
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
                    Boolean isMuted = CharonServer.muteMap.getOrDefault(clientId, false);
                    if (!isMuted) {
                        System.out.println("Received from " + clientId + ": " + inputLine);
                        msgList.add(clientId + " : " + inputLine);
                    } else {
                        //　ミュートしたアカウントの処理
//                        System.out.println("Muted message from " + clientId + ": " + inputLine);
                    }

                }
            } catch (IOException e) {
                if (isRunning) {
                    System.out.println("Exception in ClientHandler for client " + clientId + ": " + e.getMessage());
                }
            } finally {
                try {
                    if (out != null) out.close();
                    if (in != null) in.close();
                    clientSocket.close();
                } catch (IOException ignored) {}
                clientMap.remove(clientId);
            }
        }
    }
}


