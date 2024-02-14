package server;

import com.google.gson.Gson;
import data.Chat;

import java.io.PrintWriter;
import java.util.Map;

import static server.CharonServer.permissionMap;
import static server.SvrMessage.*;

public class MessageProcessor {
    private final Gson gson = new Gson();

    public void sendJson(String message, Map<String, PrintWriter> clientMap) {
        if (message.startsWith("$")) {
            // JSON 文字列を識別
            String json = message.substring(1); // 先頭の $ を取り除く
            Chat chat = gson.fromJson(json, Chat.class); // JSON を Chat オブジェクトにデシリアライズ

            // 送信者の権限を確認
            Integer senderLevel = permissionMap.get(chat.from);
            if (senderLevel != null && senderLevel >= 1) {
                // 受信者にメッセージを送信
                PrintWriter toClient = clientMap.get(chat.to);
                if (toClient != null) {
                    toClient.println(chat.from + ": " + chat.msg); // 受信者にメッセージを送信
                    chat.success = true;
                } else {
                    System.out.println("Recipient not found: " + chat.to);
                }
                chat.access = true;
            } else {
                System.out.println("Sender does not have permission: " + chat.from);
                System.out.println("Permission level: " + senderLevel);
            }
            debug(chat);
            CharonServer.chatList.add(chat);
        }
    }

    public void showList(PrintWriter out, Map<String, PrintWriter> clientMap) {
        StringBuilder userList = new StringBuilder("Currently connected users:\n");
        for (String clientId : clientMap.keySet()) {
            userList.append("- ").append(clientId).append("\n");
        }
        out.println(userList); // 接続中のユーザーリストをクライアントに送信
    }

    public void unknown(PrintWriter out, String cmd){
        out.println("Unknown command. " + cmd );
    }
}
