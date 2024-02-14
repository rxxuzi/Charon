package server;

import client.CltMessages;
import data.Chat;

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
            case "/log" -> log(parts);
            case "/give" -> give(parts);
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
                    writer.println("Server: " +messageToSend);
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
                            targetOut.println("Server: " +messageToSend);
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
                    targetOut.println("Server: " + messageToSend);
                } else {
                    warning("Client ID " + targetClientId + " not found.");
                }
                CharonServer.msgList.add("server ["+targetClientId+"] : " + messageToSend);
            }
        }
    }

    private static void kick(String[] parts) {
        if (clientMap.isEmpty()){
            warning("No clients connected.");
            return;
        }
        if (parts.length == 2) {
            if (parts[1].equals("-a")) {
                // 全クライアントをキック
                for (PrintWriter writer : clientMap.values()) {
                    writer.println(CltMessages.KICKED_OUT);
                    writer.close();
                }
                clientMap.clear();
                debug("All clients have been kicked out.");
            } else {
                String targetId = parts[1];
                if (clientMap.containsKey(targetId)) {
                    PrintWriter targetOut = clientMap.get(targetId);
                    if (targetOut != null) {
                        targetOut.println(CltMessages.KICKED_OUT);
                        targetOut.close(); // ターゲットの出力ストリームを閉じる
                    }
                    clientMap.remove(targetId); // ターゲットをクライアントマップから削除
                    debug("Client ID " + targetId + " has been kicked out.");
                } else {
                    warning("Client ID " + targetId + " not found.");
                }
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
            warning("Error while shutting down: " + e.getMessage());
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
        System.out.println("/log [--msg|--cmd|--mute|--chat] - View message, command, mute, or chat logs respectively");
        System.out.println("/log [--msg|--mute] <client-id> - View messages or mute log for a specific client");
        System.out.println("/kill - Shutdown the server");
        System.out.println("/give <client-id> [0~3] - Give clients access privileges from 0~3");
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

    /**
     * <h2>log</h2>
     * メッセージログを表示する
     * @see CharonServer#msgList
     * @param parts {@code /log [--msg|--cmd|--mute] <client-id> }
     */
    public static void log(String[] parts) {
        if(parts.length == 1){
            for(String msgs : CharonServer.msgList){
                System.out.println(msgs);
            }
        } else if (parts.length == 2) {
            switch (parts[1]) {
                case "--msg" -> {
                    for (String msgs : CharonServer.msgList) {
                        System.out.println(msgs);
                    }
                }
                case "--cmd" -> {
                    for (String cmds : CharonServer.cmdList) {
                        System.out.println(cmds);
                    }
                }
                case "--mute" -> {
                    for (String mute : CharonServer.muteMsgList) {
                        System.out.println(mute);
                    }
                }
                case "--chat" -> {
                    for (Chat chat : CharonServer.chatList) {
                        System.out.println(chat);
                    }
                }
                default -> {
                    System.out.println("Usage: /log [--msg|--cmd|--mute|--chat] [client-id]");
                    System.out.println("Usage: /log");
                }
            }
        } else if (parts.length >= 3) {
            String targetId = parts[2];
            if (clientMap.containsKey(targetId)){
                if (parts[1].equals("--msg")) {
                    for(String msgs : CharonServer.msgList){
                        if(msgs.startsWith(targetId)){
                            System.out.println(msgs);
                        }
                    }
                } else if (parts[1].equals("--mute")) {
                    for (String msg : CharonServer.muteMsgList) {
                        if (msg.startsWith(targetId)) {
                            System.out.println(msg);
                        }
                    }
                } else {
                    System.out.println("Usage: /log [--msg|--mute] [client-id]");
                    System.out.println("Usage: /log");
                }
            } else {
                System.out.println("Client ID " + targetId + " not found.");
            }
        } else {
            System.out.println("Usage: /log");
            System.out.println("Usage: /log [--msg|--cmd|--mute|--chat]");
            System.out.println("Usage: /log [--msg|--mute] [client-id]");
        }
    }

    /**
     * <h2>give</h2>
     * 権限レベルを更新する
     * @param parts {@code /give <client-id> [0~3] }
     */
    public static void give(String[] parts) {
        if (clientMap.isEmpty()) {
            warning("No clients connected.");
            return;
        }
        if (parts.length == 3) {
            String clientId = parts[1];
            try {
                int permissionLevel = Integer.parseInt(parts[2]);
                if (permissionLevel >= 0 && permissionLevel <= 3) {
                    // 権限レベルを更新
                    CharonServer.permissionMap.put(clientId, permissionLevel);
                    System.out.println("Given permission level " + permissionLevel + " to client ID: " + clientId);

                    // 対象クライアントに権限レベルの更新を通知
                    if (CharonServer.clientMap.containsKey(clientId)) {
                        PrintWriter out = CharonServer.clientMap.get(clientId);
                        out.println("levelUpdate:" + permissionLevel); // クライアントに権限レベルの更新を通知
                    } else {
                        System.out.println("Client ID not found: " + clientId);
                    }
                } else {
                    System.out.println("Invalid permission level. Please use 0 (Guest), 1 (Member), 2 (Operator), or 3(Admin)");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid permission level format.");
            }
        } else {
            System.out.println("Usage: /give <client-Id> [0~3]");
        }
    }

}


