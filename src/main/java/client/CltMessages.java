package client;

import global.Message;

public class CltMessages extends Message {
    public static final String KICKED_OUT = "You have been kicked out by the server.";
    public static final String SERVER_SHUTDOWN = "Server is shutting down. You have been disconnected.";
    public static final String DISCONNECTED = "Disconnected from the server.";
    public static final String EXITING = "Exiting...";

    public static int updateAccessLevel(String fromServer) {
        int level = Integer.parseInt(fromServer.split(":")[1]);
        notice("Your access level has been updated to: " + level);
        return level; // 返り値は、ユーザーのアクセスレベルを返す。
    }
}
