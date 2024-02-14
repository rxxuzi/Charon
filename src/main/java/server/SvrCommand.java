package server;

import client.CltMessages;

import java.io.PrintWriter;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static server.SvrMessage.*;
import static server.CharonServer.clientMap;

public final class SvrCommand {
    public static void execute(String commandLine) {
        String[] parts = commandLine.split(" ", 3);
        switch (parts[0]) {
            case "/send" -> send(parts);
            case "/kick" -> kick(parts);
            case "/kill" -> kill();
            case "/list" -> list();
            case "/help" -> help();
            case "/stats" -> stats();
            case "/mute" -> mute(parts);
            case "/unmute" -> unmute(parts);
            case "/log" -> log();
            default -> unknown(parts); // 上記以外のコマンドの処理
        }
    }

    private static void unknown(String[] parts) {
        warning("Unknown command: " + parts[0] + " Type /help for help.");
    }

    private static void send(String[] parts) {
        if (clientMap.isEmpty()){
            warning("No clients connected.");
            return;
        }
        if (parts.length >= 3) {
            if (parts[1].equals("-a")) {
                // 全クライアントにメッセージを送信
                String messageToSend = parts[2];
                for (PrintWriter writer : clientMap.values()) {
                    writer.println(messageToSend);
                }
                CharonServer.msgList.add("server [a] : " + messageToSend);
            } else if (parts[1].equals("-r")) {
                //ランダムなクライアントにメッセージを送信
                String messageToSend = parts[2];
                Random random = new Random();
                int randomIndex = random.nextInt(clientMap.size());
                int i = 0;
                for (String clientId : clientMap.keySet()) {
                    if (i == randomIndex) {
                        PrintWriter targetOut = clientMap.get(clientId);
                        if (targetOut != null) {
                            targetOut.println(messageToSend);
                        }
                        break;
                    }
                    i++;
                }
                CharonServer.msgList.add("server [r] : " + messageToSend);
            } else {
                // 特定のクライアントにメッセージを送信
                String targetClientId = parts[1];
                String messageToSend = parts[2];
                PrintWriter targetOut = clientMap.get(targetClientId);
                if (targetOut != null) {
                    targetOut.println(messageToSend);
                } else {
                    warning("Client ID " + targetClientId + " not found.");
                }
                CharonServer.msgList.add("server ["+targetClientId+"] : " + messageToSend);
            }
        }
    }

    private static void kick(String[] parts) {
        if (clientMap.isEmpty()){
            System.out.println("No clients connected.");
            return;
        }
        if (parts.length == 2) {
            String targetId = parts[1];
            if (clientMap.containsKey(targetId)) {
                PrintWriter targetOut = clientMap.get(targetId);
                if (targetOut != null) {
                    targetOut.println(CltMessages.KICKED_OUT);
                    targetOut.close(); // ターゲットの出力ストリームを閉じる
                }
                clientMap.remove(targetId); // ターゲットをクライアントマップから削除
                System.out.println("Client ID " + targetId + " has been kicked out.");
            } else {
                System.out.println("Client ID " + targetId + " not found.");
            }
        }
    }

    private static void kill() {
        // プロセスの終了
        CharonServer.isRunning = false;
        important("Server is shutting down...");

        try {
            for (PrintWriter writer : clientMap.values()) {
                writer.println(CltMessages.SERVER_SHUTDOWN);
                writer.close();
            }
            clientMap.clear();   // クライアントマップを空にする
            CharonServer.svrSocket.close(); // サーバーソケットを閉じる
        } catch (Exception e) {
            System.out.println("Error while shutting down: " + e.getMessage());
        }

        System.exit(0); // プログラムを終了
    }

    private static void list() {
        // 接続しているクライアントのIDを表示
        System.out.println("Connected Client ID: " + clientMap.keySet());
    }

    private static void help() {
        System.out.println("Available commands:");
        System.out.println("/send -a <message> - Send a message to all clients");
        System.out.println("/send -r <message> - Send a message to a random client");
        System.out.println("/send <client-id> <message> - Send a message to a specific client");
        System.out.println("/kick <client-id> - Kick a client out of the server");
        System.out.println("/list - View connected clients");
        System.out.println("/help - Display this help message");
        System.out.println("/stats - Display server statistics");
        System.out.println("/mute <client-id> - Mute a client");
        System.out.println("/mute -a - Mute all clients");
        System.out.println("/unmute <client-id> - Unmute a client");
        System.out.println("/unmute -a - Unmute all clients");
        System.out.println("/log - View the message log");
        System.out.println("/kill - Shutdown the server");
    }

    public static void stats() {
        long currentTime = System.currentTimeMillis();
        long uptimeMillis = currentTime - CharonServer.serverStartTime;
        String uptime = formatDuration(uptimeMillis);

        System.out.println("Current connected clients: " + CharonServer.clientMap.size());
        System.out.println("Server uptime: " + uptime);
    }

    private static String formatDuration(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minutes);

        return String.format("%02d h, %02d min, %02d s", hours, minutes, seconds);
    }

    private static void mute(String[] parts) {
        if (parts.length == 2) {
            if (parts[1].equals("-a")){
                //全員をミュートする
                for (String clientId : CharonServer.clientMap.keySet()) {
                    CharonServer.muteMap.put(clientId, true);
                    System.out.println("Client ID " + clientId + " has been muted.");
                }
            } else {
                String targetId = parts[1];
                CharonServer.muteMap.put(targetId, true);
                System.out.println("Client ID " + targetId + " has been muted.");
            }
        } else {
            System.out.println("Usage: /mute <client-id>");
            System.out.println("Usage: /mute -a");
        }
    }

    private static void unmute(String[] parts) {
        if (parts.length == 2) {
            if (parts[1].equals("-a")){
                //全員をミュートする
                for (String clientId : CharonServer.clientMap.keySet()) {
                    CharonServer.muteMap.put(clientId, false);
                    System.out.println("Client ID " + clientId + " has been unmuted.");
                }
            } else {
                String targetId = parts[1];
                CharonServer.muteMap.put(targetId, false);
                System.out.println("Client ID " + targetId + " has been unmuted.");
            }
        } else {
            System.out.println("Usage: /unmute <client-id>");
            System.out.println("Usage: /unmute -a");
        }
    }

    public static void log() {
        for(String msgs : CharonServer.msgList){
            System.out.println(msgs);
        }
    }
}


