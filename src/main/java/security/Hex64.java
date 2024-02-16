package security;

public final class Hex64 {
    private static final String CHAR_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-";

    // ipv6をエンコードする
    public static String enc(String address){
        boolean isLLA = address.contains("%"); // address内に%が入っていたら LLAフラグを立てる
        String ipv6 = normalize(address);
        if (isLLA) return encLLA(ipv6);
        else return encGUA(ipv6);
    }

    // ipv6をデコードする
    public static String dec(String address){
        boolean isLLA = address.contains("%"); // address内に%が入っていたら LLAフラグを立てる
        if (isLLA) return encLLA(address);
        else return encGUA(address);
    }

    public static String encLLA(String ipv6){
        // %の前後で分ける
        String[] lla = ipv6.split("%" , 2);
        String[] ips = lla[0].split(":");
        String[] enc = new String[ips.length];
        for (int i = 0; i < ips.length; i++) {
            enc[i] = encode(ips[i]);
        }
        return String.join("", enc) + "%" + lla[1];
    }

    public static String decLLA(String ipv6){
        String[] lla = ipv6.split("%", 2);
        String[] ips = lla[0].split("(?<=\\G.{3})");
        String[] dec = new String[ips.length];
        for (int i = 0; i < ips.length; i++) {
            dec[i] = decode(ips[i]);
        }
        return String.join(":", dec) + "%" + lla[1];
    }

    public static String encGUA(String ipv6){
        String[] ips = ipv6.split(":");
        String[] enc = new String[ips.length];
        for (int i = 0; i < ips.length; i++) {
            enc[i] = encode(ips[i]);
        }
        return String.join("", enc);
    }

    public static String decGUA(String ipv6){
        String[] ips = ipv6.split("(?<=\\G.{3})");
        String[] dec = new String[ips.length];
        for (int i = 0; i < ips.length; i++) {
            dec[i] = decode(ips[i]);
        }
        return String.join(":", dec);
    }

    private static String encode(String hex) {
        // 先頭の16進数の文字をそのまま残す
        String firstChar = hex.substring(0, 1);
        // 残りを数値に変換
        int decimal = Integer.parseInt(hex.substring(1), 16);

        // 64進数のインデックスを計算してエンコード
        char encodedChar1 = CHAR_SET.charAt((decimal / 64) % 64);
        char encodedChar2 = CHAR_SET.charAt(decimal % 64);

        return firstChar + encodedChar1 + encodedChar2;
    }

    private static String decode(String encoded) {
        // 先頭の文字はそのまま
        String firstChar = encoded.substring(0, 1);
        // 残りのエンコードされた文字から64進数のインデックスを逆算
        int index1 = CHAR_SET.indexOf(encoded.charAt(1));
        int index2 = CHAR_SET.indexOf(encoded.charAt(2));

        // 元の16進数の数値に戻す
        int decimal = (index1 * 64) + index2;

        // 16進数の文字列に変換し、必要に応じて先頭にゼロを追加して桁数を調整
        StringBuilder hexPart = new StringBuilder(Integer.toHexString(decimal));
        while (hexPart.length() < 3) {
            hexPart.insert(0, "0");
        }
        return firstChar + hexPart;
    }

    private static String normalize(String address) {
        if (!address.contains("::")) {
            return padZeroes(address);
        }
        String[] parts = address.split("::", -1);
        String[] leftParts = parts[0].isEmpty() ? new String[0] : parts[0].split(":", -1);
        String[] rightParts = parts.length > 1 && !parts[1].isEmpty() ? parts[1].split(":", -1) : new String[0];
        int missingSections = 8 - (leftParts.length + rightParts.length);

        StringBuilder builder = new StringBuilder();
        for (String part : leftParts) builder.append(part).append(":");
        for (int i = 0; i < missingSections; i++) builder.append("0").append(":");
        for (int i = 0; i < rightParts.length; i++) {
            builder.append(rightParts[i]);
            if (i < rightParts.length - 1) {
                builder.append(":");
            }
        }

        // 最後のコロンを取り除く場合があるため、チェックして修正
        String normalizedAddress = builder.toString();
        if (normalizedAddress.endsWith(":") && !normalizedAddress.endsWith("::")) {
            normalizedAddress = normalizedAddress.substring(0, normalizedAddress.length() - 1);
        }

        return padZeroes(normalizedAddress);
    }

    private static String padZeroes(String ipv6) {
        // 分割して各セグメントを処理
        String[] segments = ipv6.split(":", -1);
        for (int i = 0; i < segments.length; i++) {
            // セグメントが空、または0でない数字で始まるが4文字未満の場合、先頭に0を追加
            if (segments[i].isEmpty() || (segments[i].length() < 4 && !segments[i].startsWith("0"))) {
                segments[i] = String.format("%4s", segments[i]).replace(' ', '0');
            } else if (segments[i].equals("0")) {
                segments[i] = "0000";
            }
        }
        // 処理したセグメントを":"で結合して返す
        return String.join(":", segments);
    }
}

