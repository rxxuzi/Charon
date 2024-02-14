package global;

import java.util.Arrays;

public class Message {
    // ANSIエスケープコード
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";

    private static final String[] ANSI_COLOR = {
            ANSI_RED,
            ANSI_GREEN,
            ANSI_YELLOW,
            ANSI_BLUE,
            ANSI_PURPLE,
            ANSI_CYAN
    };

    public static final int COLORS =  ANSI_COLOR.length;

    public static String colorize(int type, Object msg) {
        return ANSI_COLOR[type % ANSI_COLOR.length] + msg.toString() + ANSI_RESET;
    }

    public static void echo(String msg) {
        System.out.println(msg);
    }

    public static void echo(int type, String msg) {
        System.out.println(ANSI_COLOR[type % COLORS] + msg + ANSI_RESET);
    }

    public static void echo(int type, Object... args) {
        String message = Arrays.toString(args);
        System.out.println(ANSI_COLOR[type % COLORS] + message + ANSI_RESET);
    }

    public static void echo(int type, String format, Object... args) {
        String formattedMessage = String.format(format, args);
        System.out.println(ANSI_COLOR[type % COLORS] + formattedMessage + ANSI_RESET);
    }

    public static void important(String msg) {
        echo(0, msg);
    }

    public static void important(Object... args) {
        echo(0, args);
    }
    public static void important(String format, Object... args) {
        echo(0, format, args);
    }

    // 緑色 - 成功や完了メッセージに使用
    public static void success(String msg) {
        echo(1, msg);
    }

    public static void success(Object... args) {
        echo(1, args);
    }

    public static void success(String format, Object... args) {
        echo(1, format, args);
    }

    // 黄色 - 警告メッセージに使用
    public static void warning(String msg) {
        echo(2, msg);
    }

    public static void warning(Object... args) {
        echo(2, args);
    }

    public static void warning(String format, Object... args) {
        echo(2, format, args);
    }

    // 青色 - 情報メッセージに使用
    public static void info(String msg) {
        echo(3, msg);
    }

    public static void info(Object... args) {
        echo(3, args);
    }

    public static void info(String format, Object... args) {
        echo(3, format, args);
    }

    // マゼンタ - デバッグメッセージや特別な注意が必要なメッセージに使用
    public static void debug(String msg) {
        echo(4, msg);
    }

    public static void debug(Object... args) {
        echo(4, args);
    }

    public static void debug(String format, Object... args) {
        echo(4, format, args);
    }

    // シアン - 通知やヒントメッセージに使用
    public static void notice(String msg) {
        echo(5, msg);
    }

    public static void notice(Object... args) {
        echo(5, args);
    }

    public static void notice(String format, Object... args) {
        echo(5, format, args);
    }
}
