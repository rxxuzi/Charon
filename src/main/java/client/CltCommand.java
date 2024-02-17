package client;

import com.google.gson.Gson;
import data.Chat;
import global.Cmd;
import global.Fast;

import java.io.IOException;
import java.io.PrintWriter;

import static client.CharonClient.cltNotes;
import static client.CharonClient.level;
import static client.CltMessages.*;

public class CltCommand {

    public static void execute(String commandLine, PrintWriter out) {
        String[] cmd = commandLine.split(" ", 3);
        String exec = cmd[0];
        try {
            switch (exec) {
                case "/exit" -> exit(out);
                case "/level" -> level();
                case "/say" -> say(commandLine, out);
                case "/list" -> out.println(Fast.st[1] + "list"); // サーバーに list コマンドを送信
                case "/ls" -> Cmd.ls();
                case "/cat" -> Cmd.cat(cmd[1]);
                case "/rm" -> Cmd.rm(cmd[1]);
                case "/note" -> note(cmd);
                case "/help" -> System.out.println(CMD_HELP);
                default -> unknown(cmd[0]);
            }
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            System.out.println("Invalid command: " + commandLine);
            unknown();
        }
    }

    /**
     * <h2>note</h2>
     * <pre>
     * --del [index]   : メモを削除
     * --view          : メモを表示
     * --clear         : メモをクリア
     * --pop           : メモをポップ（削除して表示）
     * --save          : メモを保存
     * --help          : ヘルプを表示
     * </pre>
     * @param opt オプション
     * @param note メモ内容
     */
    public static void note(String opt, String note){
        debug("note" + " " + opt + " " + note);
        switch (opt) {
            case "--del", "-d" -> {
                if (note != null){
                    try {
                        int index = Integer.parseInt(note);
                        cltNotes.remove(index);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid index: " + note);
                    }
                }
            }
            case "--view", "-v" -> {
                for (int i = 0; i < cltNotes.size(); i++) System.out.printf("%3d : %s \n" ,i , cltNotes.get(i));
            }
            case "--clear", "-c" -> cltNotes.clear();
            case "--pop" , "-p" -> {
                if (!cltNotes.isEmpty()) {
                    System.out.println(cltNotes.remove(cltNotes.size() -1));
                }
            }
            case "--save" , "-s" -> CltMessages.saveNote();
            case "--help" , "-h" -> System.out.println(CltMessages.NOTE_HELP);
            default -> System.out.println("Invalid option: " + opt + "\n Type /note --help for help");
        }
    }

    /**
     * <h2>exit</h2>
     * ソケットを閉じてプログラムを終了させる
     * @param out ソケットからの出力ストリーム
     */
    private static void exit(PrintWriter out) {
        try {
            CharonClient.socket.close();
            out.close();
        } catch (IOException e) {
            warning(e.getMessage());
            System.exit(1); // エラーの場合は終了する
            return; // このreturnは必要ないが、コードの完全性を高めるためにある。
        }
        success(EXITING);
        System.exit(0);
    }

    /**
     * <h2>level</h2>
     * クライアントのアクセスレベルを表示する
     */
    private static void level(){
        info("Your current access level is: " + level);
    }

    /**
     * <h2>say</h2>
     * クライアントからサーバーにメッセージを送る
     * @param cmd {@code /say <Client-Id> <message>}
     * @param out ソケットからの出力ストリーム
     */
    private static void say(String cmd, PrintWriter out){
        if (level >= 1) {
            String[] cmdx = cmd.split(" ", 3);
            if (cmdx.length == 3) {
                String to = cmdx[1];
                String message = cmdx[2];
                Chat chat = new Chat(cmd, to, message);
                String chatJson = new Gson().toJson(chat);
                out.println(Fast.st[2] + chatJson);
            } else {
                System.out.println("Usage: /say <Client-Id> <message>");
            }
        } else {
            warning("You do not have permission to use this command.");
        }
    }

    /**
     * <h2>note</h2>
     * メモを管理するコマンド
     * @param cmd {@code /note <option> [note]}
     */
    private static void note(String[] cmd){
        if (cmd.length == 2) {
            CltCommand.note(cmd[1], "");
        } else if (cmd.length == 3) {
            CltCommand.note(cmd[1], cmd[2]);
        } else {
            CltCommand.note("--view", "");
        }
    }

    /**
     * <h2>unknown</h2>
     * 不明なコマンドを処理する
     * @param cmd 不明なコマンド
     */
    private static void unknown(String cmd){
        warning("Unknown or Wrong command: " + cmd + " Type /help for help.");
    }

    private static void unknown(){
        warning("Unknown or Wrong command. Type /help for help.");
    }
}
