package server;

import data.Chat;
import data.User;
import global.Fast;
import net.Spider;
import opium.Opioid;
import opium.Opium;
import opium.OpiumException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import static server.SvrMessages.*;

public class CharonServer {
    public static final int PORT = 12345;
    public static boolean isRunning = true;
    // 接続しているクライアントのマップ
    public static final Map<String, PrintWriter> clientMap = new ConcurrentHashMap<>();
    // クライアントのストリームのマップ
    public static final Map<String, OutputStream> streamMap = new ConcurrentHashMap<>();
    // ミュートしているクライアントのマップ
    public static final Map<String, Boolean> muteMap = new ConcurrentHashMap<>();
    // クライアントの権限レベルを保持するマップ
    public static final Map<String, Integer> permissionMap = new ConcurrentHashMap<>();
    //　ユーザーマップ
    public static final Map<String, User> idMap = new ConcurrentHashMap<>();

    public static final long serverStartTime = System.currentTimeMillis();

    public static ServerSocket svrSocket;

    public static final List<User> userList = new ArrayList<>();
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

        Spider spider = new Spider(PORT);
        svrSocket = spider.createLocalhostServerSocket();

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
        private OutputStream os;
        private String name;
        private String id;
        private User user;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                os = clientSocket.getOutputStream();

                user = User.json2User(in.readLine());
                name = user.name;
                id = user.id;

                idMap.put(id, user);
                userList.add(user);

                clientMap.put(name, out); // メッセージ用のストリーム
                streamMap.put(name, os);  // ファイル送信用のストリーム

                permissionMap.put(name, 1);

                success("Client : " + name + " has connected." + " ID : " + user.id);

                String line;
                MessageProcessor processor = new MessageProcessor(); // MessageProcessor のインスタンスを作成

                while ((line = in.readLine()) != null && isRunning) {
                    String type = line.split(":")[0];
                    line = line.substring(type.length() + 1);
                    type += ":";

                    if(type.equals(Fast.st[0])){
                        // !t:  テキスト
                        Boolean isMuted = CharonServer.muteMap.getOrDefault(name, false);
                        if (!isMuted) {
                            // 通常のテキストメッセージの処理
                            notice(name + ": " + line);
                            msgList.add(name + ": " + line);
                        } else {
                            muteMsgList.add(name + ": " + line);
                        }
                    } else if (type.equals(Fast.st[1])){
                        // !c:  コマンド
                        String[] cmd = line.split(" ", 3);
                        if (cmd[0].equals("/list")) {
                            processor.showList(out, clientMap);
                        }
                        processor.showList(out, clientMap);
                    } else if (type.equals(Fast.st[2])){
                        // !j:   JSON
                        // MessageProcessor を使用してメッセージを処理 (sayコマンド)
                        processor.sendJson(line, clientMap);
                    } else if(type.equals(Fast.st[3])){
                        // !o:   opium
                        try {
                            // Opiumインスタンスを受信する
                            Opium receivedOpium = Opium.toOpium(clientSocket);
                            Opioid.list.add(receivedOpium); // リストに追加
                            debug("Received Opium instance: " + receivedOpium.toString());
                        } catch (OpiumException e) {
                            throw new RuntimeException(e);
                        }
                    } else if(type.equals(Fast.st[5])) {
                        // !u : User
                        user = User.json2User(line);
                    }
                }
                // readLine() が null を返した場合、クライアントが接続を閉じたことを示す
                info("Client " + name + " disconnected.");
            } catch (IOException e) {
                if (isRunning) {
                    info("Exception in ClientHandler for client " + name + ": " + e.getMessage());
                }
            } finally {
                try {
                    if (out != null) out.close();
                    if (in != null) in.close();
                    clientSocket.close();
                } catch (IOException ignored) {}

                //　それぞれのMapからクライアントを削除する
                clientMap.remove(name);
                streamMap.remove(name);
                permissionMap.remove(name);
                userList.remove(user);
            }
        }

    }
}


