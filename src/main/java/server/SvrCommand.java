package server;

import client.CltMessages;
import data.Chat;
import data.Fcx;
import data.FcxManager;
import global.Cmd;
import global.Fast;

import java.io.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static server.SvrMessages.*;
import static server.CharonServer.clientMap;

public final class SvrCommand {
    public static void execute(String commandLine) {
        String[] parts = commandLine.split(" ", 3);
        switch (parts[0]) {
            case "/send" -> send(commandLine);
            case "/say" -> say(parts);
            case "/kick" -> kick(parts);
            case "/kill" -> kill();
            case "/list" -> list();
            case "/help" -> help();
            case "/stats" -> stats();
            case "/mute" -> mute(parts);
            case "/unmute" -> unmute(parts);
            case "/log" -> log(parts);
            case "/give" -> give(parts);
            case "/file" -> file(parts);
            case "/ls" -> Cmd.ls(parts[1]);
            default -> unknown(parts); // 上記以外のコマンドの処理
        }
    }

    /**
     * <h2>file</h2>
     * fcxの保存・削除・一覧する
     * @param parts
     */
    private static void file(String[] parts){
        int size = CharonServer.fcxList.size();
        if (size == 0) {
            warning("No FCX");
            return;
        }
        if (parts.length == 1) {
            System.out.println("Usage : /file [--list|--save|--del]");
        } else if (parts.length >= 2){
            String opt = parts[1];

            switch(opt){
                case "--list" -> {
                    for (int i = 0; i < size; i++) {
                        Fcx fcx = CharonServer.fcxList.get(i);
                        System.out.printf("%3d %s \n" ,i , " : " + fcx.toString());
                    }
                }
                case "--save" -> {
                    if(FcxManager.saveAllFcx(CharonServer.fcxList)){
                        notice("Save All File");
                    }
                }
            }
        }
        debug("size : " + size);
    }

    private static void unknown(String[] parts) {
        warning("Unknown command: " + parts[0] + " Type /help for help.");
    }

    /**
     * <h2>send</h2>
     * クライアントへファイルを送信する
     * @see OutputStream
     * @param commandLine {@code /send <client-id> <file-path>}
     */
    public static void send(String commandLine) {
        String[] cmd = commandLine.split(" ");
        if (cmd.length >= 3){
            String targetClientId = cmd[1];
            String filePath = cmd[2];

            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                try {
                    long fileSize = file.length();
                    PrintWriter msg = clientMap.get(targetClientId);
                    OutputStream os = CharonServer.streamMap.get(targetClientId);
                    DataOutputStream dos = new DataOutputStream(os);
                    debug("Sending file " + file.getName() + " to @" + targetClientId);

                    msg.println(Fast.st[4] + file.getName() + ":" + fileSize); //　ヘッダーを送る
                    FileInputStream fis = new FileInputStream(file); // ファイルをストリームに変換
                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = fis.read(buffer)) > 0) {
                        dos.write(buffer, 0, read);
                    }
                    fis.close();
                } catch (IOException e) {
                    System.out.println("Error sending file: " + e.getMessage());
                }
            } else {
                warning("Please enter a valid file path");
            }
        } else {
            System.out.println("Usage: /send <client-id> <file-path>");
        }
    }

    /**
     * <h2>say</h2>
     * クライアントへメッセージを送信する
     * @param parts {@code /say <client-id | -a | -r> <message>}
     */
    private static void say(String[] parts) {
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

    /**
     * <h2>kick</h2>
     * クライアントをキックする
     * @param parts {@code /kick <client-id | -a>}
     */
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

    /**
     * <h2>kill</h2>
     * サーバーをシャットダウンする
     * @see CharonServer#isRunning
     *
     */
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

    /**
     * <h2>list</h2>
     * 接続しているクライアントのIDを表示する
     */
    private static void list() {
        // 接続しているクライアントのIDを表示
        if (clientMap.isEmpty()){
            warning("No clients connected.");
        } else {
            System.out.println("Connected Client ID: " + clientMap.keySet());
        }
    }

    private static void help() {
        System.out.println(CMD_HELP);
    }

    /**
     * <h2>stats</h2>
     * サーバーの状態を出力する
     * @see #formatDuration(long)
     * @see CharonServer#serverStartTime
     */
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

    /**
     * <h2>mute</h2>
     * クライアントからのメッセージをミュートする
     * @param parts {@code /mute <client-id> }
     */
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

    /**
     * <h2>unmute</h2>
     * クライアントからのメッセージをミュート解除する
     * @param parts {@code /unmute <client-id> }
     */
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
        if (parts.length == 2 && parts[1].equals("-x")) {
            // クライアント全ての権限を0にする。
            for (String clientId : CharonServer.clientMap.keySet()) {
                CharonServer.permissionMap.put(clientId, 0);
                System.out.println("Given permission level 0 to client ID: " + clientId);
                CharonServer.clientMap.get(clientId).println("levelUpdate:0"); // クライアントに権限レベルの更新を通知
            }
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


