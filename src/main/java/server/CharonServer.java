package server;

import data.Chat;
import data.Fcx;
import global.Fast;
import net.Network;

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

    public static final long serverStartTime = System.currentTimeMillis();

    public static ServerSocket svrSocket;

    // クライアントからのファイルを管理する
    public static final List<Fcx> fcxList = new ArrayList<>();

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
        private OutputStream os;
        private String clientId;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                os = clientSocket.getOutputStream();

                clientId = in.readLine();

                clientMap.put(clientId, out); // メッセージ用のストリーム
                streamMap.put(clientId, os);  // ファイル送信用のストリーム

                permissionMap.put(clientId, 1);

                success("Client ID: " + clientId + " has connected.");

                String line;
                MessageProcessor processor = new MessageProcessor(); // MessageProcessor のインスタンスを作成

                while ((line = in.readLine()) != null && isRunning) {
                    String type = line.split(":")[0];
                    line = line.substring(type.length() + 1);
                    type += ":";

                    if(type.equals(Fast.st[0])){
                        // !t:  テキスト
                        Boolean isMuted = CharonServer.muteMap.getOrDefault(clientId, false);
                        if (!isMuted) {
                            // 通常のテキストメッセージの処理
                            notice(clientId + ": " + line);
                            msgList.add(clientId + ": " + line);
                        } else {
                            muteMsgList.add(clientId + ": " + line);
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
                        // !f:   ファイル
                        try {
                            // Fcxインスタンスを受信する
                            Fcx receivedFcx = getFcx();
                            fcxList.add(receivedFcx); // リストに追加
                            debug("Received Fcx instance: " + receivedFcx.toString());
                        } catch (IOException e) {
                            warning(e.getMessage());
                        } catch (ClassNotFoundException e) {
                            warning("Class Not Found (FCX)" + e.getMessage());
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

                //　それぞれのMapからクライアントを削除する
                clientMap.remove(clientId);
                streamMap.remove(clientId);
                permissionMap.remove(clientId);
            }
        }

        private Fcx getFcx() throws IOException, ClassNotFoundException {
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            int objectSize = dis.readInt(); // オブジェクトサイズを読み取る
            byte[] objectBytes = new byte[objectSize];
            dis.readFully(objectBytes); // オブジェクトデータを全て読み取る
            ByteArrayInputStream bais = new ByteArrayInputStream(objectBytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (Fcx) ois.readObject();
        }
    }
}


