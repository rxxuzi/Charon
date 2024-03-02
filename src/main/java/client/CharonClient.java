package client;

import com.google.gson.Gson;
import data.Chat;
import global.Fast;
import net.Network;
import opium.Opioid;
import opium.Opium;
import opium.OpiumException;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import static client.CltMessages.*;

public class CharonClient {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    public static int level = 1;
    public static long joinTime;
    public static Socket socket;
    public static String clientId;
    public static final List<String> cltNotes = new ArrayList<>();
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
        clientId = UUID.randomUUID().toString();
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter client ID : ");
        if (sc.hasNextLine()) {
            clientId = sc.nextLine();
        }

        important("Client ID: " + clientId);
        socket = new Network(PORT).createSocket(HOST);

        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            joinTime = System.currentTimeMillis();

            out.println(clientId);

            new Thread(() -> {
                try {
                    String fromServer;
                    // 受信
                    while ((fromServer = in.readLine()) != null) {
                        if (fromServer.startsWith("levelUpdate:")) {
                            level = updateAccessLevel(fromServer);
                            continue;
                        } else if (fromServer.startsWith(Fast.st[3])) {
                            Opium opium = Opium.toOpium(socket);
                            Opioid.list.add(opium);
                            debug("Received Opium instance:" + opium.toString());
                            continue;
                        } else {
                            notice(fromServer); // サーバーからの受信したメッセージを出力
                        }
                        if (fromServer.equals(KICKED_OUT) || fromServer.equals(SERVER_SHUTDOWN)) {
                            important(DISCONNECTED);
                            System.exit(1);
                        }
                    }
                } catch (IOException e) {
                    important("Error reading from server: " + e.getMessage());
                } catch (OpiumException e) {
                    warning(e.getMessage());
                }
            }).start();

            String fromUser;

            // 送信
            while ((fromUser = stdIn.readLine()) != null && !socket.isClosed()) {
                if (fromUser.startsWith("@")){
                    chat(fromUser, out);
                } else if (fromUser.startsWith("/")){
                    CltCommand.execute(fromUser, out);
                } else if (fromUser.startsWith("#")){
                    cltNotes.add(fromUser.substring(1));
                } else {
                    if (!fromUser.isEmpty()) out.println(Fast.st[0] + fromUser);
                }
            }
        } catch (IOException e) {
            important("Exception caught when trying to connect to " + HOST + " on port " + PORT + "\n" + e.getMessage());
        } finally {
            socket.close();
        }
    }

    private static void chat(String cmd, PrintWriter out){
        String[] command = cmd.split(" ", 2);
        if (command.length == 2) {
            String to = command[0].substring(1);
            String message = command[1];
            Chat chat = new Chat(clientId, to, message);
            String chatJson = gson.toJson(chat);
            out.println(Fast.st[2] + chatJson);
        } else {
            System.out.println("Usage: @<Client-Id> <message>");
        }
    }
}


