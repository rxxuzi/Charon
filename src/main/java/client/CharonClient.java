package client;

import com.google.gson.Gson;
import data.Chat;
import net.Network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.UUID;

import static client.CltMessages.*;

public class CharonClient {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    public static int level = 1;
    public static long joinTime;
    public static Socket socket;
    private static String clientId;
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
        clientId = UUID.randomUUID().toString();
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter client ID : ");
        if (sc.hasNextLine()) {
            clientId = sc.nextLine();
        }

        System.out.println("Client ID: " + clientId);
        socket = new Network(PORT).createSocket(HOST);

        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            joinTime = System.currentTimeMillis();

            out.println(clientId);

            new Thread(() -> {
                try {
                    String fromServer;
                    while ((fromServer = in.readLine()) != null) {
                        if (fromServer.startsWith("levelUpdate:")) {
                            level = CltMessages.updateAccessLevel(fromServer);
                            continue;
                        }

                        notice(fromServer); // サーバーからの受信

                        if (fromServer.equals(CltMessages.KICKED_OUT) || fromServer.equals(CltMessages.SERVER_SHUTDOWN)) {
                            important(CltMessages.DISCONNECTED);
                            System.exit(1);
                        }
                    }
                } catch (IOException e) {
                    important("Error reading from server: " + e.getMessage());
                }
            }).start();

            String fromUser;
            while ((fromUser = stdIn.readLine()) != null && !socket.isClosed()) {
                if (fromUser.startsWith("@")){
                    chat(fromUser, out);
                } else if (fromUser.startsWith("/")){
                    processCommand(fromUser, out);
                } else {
                    out.println(fromUser);
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
            out.println("$" + chatJson);
        } else {
            System.out.println("Usage: @<Client-Id> <message>");
        }
    }

    public static void processCommand(String commandLine, PrintWriter out) {
        String[] cmd = commandLine.split(" ", 3);
        if (cmd[0].equals("/exit")) {
            success(EXITING);
            System.exit(0);
        } else if (cmd[0].equals("/level")) {
            System.out.println("Your current access level is: " + level);
        } else if (cmd[0].equals("/say")) {
            if(level >= 1){
                String[] cmdx = commandLine.split(" ", 3);
                if (cmdx.length == 3) {
                    String to = cmdx[1];
                    String message = cmdx[2];
                    Chat chat = new Chat(clientId, to, message);
                    String chatJson = gson.toJson(chat);
                    out.println("$" + chatJson); // $ をつけてメッセージを送信するとJson形式の文字列という意味となる
                } else {
                    System.out.println("Usage: /say <Client-Id> <message>");
                }
            } else {
                warning("You do not have permission to use this command.");
            }
        } else if (cmd[0].equals("/list")){
            out.println("!list"); // サーバーに !list コマンドを送信
        } else {
            System.out.println("Invalid command. Please try again.");
        }
    }
}


