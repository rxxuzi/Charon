package data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Chat {
    public String from;
    public String to;
    public String msg;
    public String time;
    public String date;
    public boolean success = false;
    public boolean access = false;

    public Chat(String from, String to, String message) {
        this.from = from;
        this.to = to;
        this.msg = message;
        this.time = String.valueOf(System.currentTimeMillis());
        // YYYY/MM/DD HH:MM:SS
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.date = now.format(formatter);
    }

    public String toString() {
        if (access){
            if (success){
                return "@" + from + " -> @" + to + ": \"" + msg + "\"";
            } else {
                return "@" + from + " ->X @" + to + ": \"" + msg + "\" (failed)";
            }
        } else {
            // Sender does not have permission
            return "@" + from + " ->X @" + to + ": \"" + msg + "\" (No transmission authority)";
        }

    }
}
