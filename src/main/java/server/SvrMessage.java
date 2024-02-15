package server;

import global.Message;

public class SvrMessage extends Message {
    public static final String CMD_HELP = """
            Available commands:
            /help - Display this help message
            /send -a <message> - Send a message to all clients
            /send -r <message> - Send a message to a random client
            /send <client-id> <message> - Send a message to a specific client
            /kick <client-id> - Kick a client out of the server
            /kick -a - Kick all clients out of the server
            /list - View connected clients
            /stats - Display server statistics
            /mute <client-id> - Mute a client
            /mute -a - Mute all clients
            /unmute <client-id> - Unmute a client
            /unmute -a - Unmute all clients
            /log - View the message log
            /log [--msg|--cmd|--mute|--chat] - View message, command, mute, or chat logs respectively
            /log [--msg|--mute] <client-id> - View messages or mute log for a specific client
            /kill - Shutdown the server
            /give <client-id> [0~3] - Give clients access privileges from 0~3
            /give -a <client-id> [0~3] - Give all clients access privileges from 0~3
            """;

}
