package server;

import data.Chat;
import net.Network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static server.SvrMessage.*;

public class CharonServer {
    public static final int PORT = 12345;
    public static boolean isRunning = true;
    // 接続しているクライアントのマップ
    public static final Map<String, PrintWriter> clientMap = new ConcurrentHashMap<>();
    // ミュートしているクライアントのマップ
    public static final Map<String, Boolean> muteMap = new ConcurrentHashMap<>();
    // クライアントの権限レベルを保持するマップ
    public static final Map<String, Integer> permissionMap = new ConcurrentHashMap<>();

    public static ServerSocket svrSocket;
    public static final long serverStartTime = System.currentTimeMillis();

    public static final List<String> msgList = new ArrayList<>(); // メッセージの履歴
    public static final List<String> cmdList = new ArrayList<>(); // コマンド履歴
    public static final List<String> muteMsgList = new ArrayList<>(); //　ミュートされているメッセージの履歴
    public static final List<Chat> chatList = new ArrayList<>();  // チャットのリスト

    public static void main(String[] args) throws Exception {
        important("Server is running on port " + PORT + "...");

        // コマンドリッスンスレッドの立ち上げ
        new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (isRunning) {
                    if (!scanner.hasNextLine()) continue;
                    String commandLine = scanner.nextLine();
                    processCommand(commandLine);
                }
            } catch (Exception e) {
                warning("Error handling server commands: " + e.getMessage());
            }
        }).start();

        svrSocket = new Network(PORT).createLocalhostServerSocket();

        try {
            while (isRunning) {
                Socket clientSocket = svrSocket.accept(); // This doesn't need to be in the try-with-resources
                new ClientHandler(clientSocket).start(); // Start a new thread for each client
            }
            svrSocket.close(); // サーバーソケットを閉じる
            System.exit(0); // プログラムを終了
        } catch (IOException e) {
            important("Exception caught when trying to listen on port " + PORT + " or listening for a connection");
            warning(e.getMessage());
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

                permissionMap.put(clientId, 1);

                success("Client ID: " + clientId + " has connected.");

                String inputLine;
                MessageProcessor processor = new MessageProcessor(); // MessageProcessor のインスタンスを作成

                while ((inputLine = in.readLine()) != null && isRunning) {
                    /*
                        先頭に$が付いていたらJson形式のストリーム
                        先頭に!が付いていたらコマンド
                     */
                    if (inputLine.startsWith("$")) {
                        // MessageProcessor を使用してメッセージを処理 (sayコマンド)
                        processor.sendJson(inputLine, clientMap);
                    } else if  (inputLine.startsWith("!")) {
                        String commandLine = inputLine.substring(1); // 先頭の ! を取り除く
                        String[] cmd = commandLine.split(" ", 3);
                        if (cmd[0].equals("/list")) {
                            processor.showList(out, clientMap);
                        }
                        processor.showList(out, clientMap);
                    } else {
                        Boolean isMuted = CharonServer.muteMap.getOrDefault(clientId, false);
                        if (!isMuted) {
                            // 通常のテキストメッセージの処理
                            notice(clientId + ": " + inputLine);
                            msgList.add(clientId + ": " + inputLine);
                        } else {
                            muteMsgList.add(clientId + ": " + inputLine);
                        }
                    }
                }
                // readLine() が null を返した場合、クライアントが接続を閉じたことを示す
                info("Client " + clientId + " disconnected.");
            } catch (IOException e) {
                if (isRunning) {
                    info("Exception in ClientHandler for client " + clientId + ": " + e.getMessage());
                }
            } finally {
                try {
                    if (out != null) out.close();
                    if (in != null) in.close();
                    clientSocket.close();
                } catch (IOException ignored) {}
                clientMap.remove(clientId);
                permissionMap.remove(clientId);
            }
        }
    }
}


