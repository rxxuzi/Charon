package client;

import global.Message;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static client.CharonClient.cltNotes;

public class CltMessages extends Message {
    public static final String KICKED_OUT = "You have been kicked out by the server.";
    public static final String SERVER_SHUTDOWN = "Server is shutting down. You have been disconnected.";
    public static final String DISCONNECTED = "Disconnected from the server.";
    public static final String EXITING = "Exiting...";
    public static final String INVALID_COMMAND = "Invalid command.";
    public static final String FILE_RECEIVE_FAILED = "File receive failed.";

    public static final String CMD_HELP = """
            /help - Display this help message
            /note [option] [note] - Manage notes
            /exit - Exit the program
            /level - Check access level
            /list - List all online users
            /say <client-id> <message> - Send a message to a specific client
            /ls - List all files in the current directory
            /cat <filename> - Display the contents of a file
            /rm  <filename> - Delete a file                
            """;
    public static final String NOTE_HELP = """
            <--del | -d> [index]   : Delete note
            <--view | -v>           : View notes
            <--clear | -c>          : Clear notes
            <--pop | -p>            : Pop note
            <--save | -s>           : Save notes
            <--help | -h>           : Show this help
            """;
    public static int updateAccessLevel(String fromServer) {
        int level = Integer.parseInt(fromServer.split(":")[1]);
        notice("Your access level has been updated to: " + level);
        return level; // 返り値は、ユーザーのアクセスレベルを返す。
    }

    public static void saveNote(){
        notice("Note file saved.");
        File file = new File("note.txt");

        try (FileWriter fw = new FileWriter(file)){
            for (String memo : cltNotes) {
                fw.write(memo + "\n");
            }
        } catch (IOException e) {
            warning("Error: " + e.getMessage());
        }
    }

}
