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
    public static void main(String[] args) throws IOException {
        Gson gson = new Gson();
        String clientId = UUID.randomUUID().toString();
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
                            level = Integer.parseInt(fromServer.split(":")[1]);
                            System.out.println("Your access level has been updated to: " + level);
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
                if(fromUser.startsWith("/")){
                    if (fromUser.equalsIgnoreCase("/exit")) {
                        success(EXITING);
                        System.exit(0);
                    } else if (fromUser.equalsIgnoreCase("/level")) {
                        System.out.println("Your current access level is: " + level);
                    } else if (fromUser.startsWith("/say")) {
                        if(level >= 1){
                            String[] command = fromUser.split(" ", 3);
                            if (command.length == 3) {
                                String to = command[1];
                                String message = command[2];
                                // Chat インスタンスを作成
                                Chat chat = new Chat(clientId, to, message);
                                // Chat インスタンスを JSON 文字列に変換
                                String chatJson = gson.toJson(chat);
                                out.println("$" + chatJson); // $ をつけてメッセージを送信するとJson形式の文字列という意味となる
                            } else {
                                System.out.println("Usage: /say <Client-Id> <message>");
                            }
                        } else {
                            warning("You do not have permission to use this command.");
                        }
                    } else if (fromUser.startsWith("/list")){
                        out.println("!list"); // サーバーに !list コマンドを送信
                    } else {
                        System.out.println("Invalid command. Please try again.");
                    }
                }else{
                    out.println(fromUser);
                }
            }
        } catch (IOException e) {
            important("Exception caught when trying to connect to " + HOST + " on port " + PORT + "\n" + e.getMessage());
        } finally {
            socket.close();
        }
    }
}


