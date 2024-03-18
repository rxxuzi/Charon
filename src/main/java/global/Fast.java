package global;

import java.util.List;
import java.util.function.Predicate;

public class Fast {
    public static final String INVALID_ID_CHAR = "!@#$%^&*()+-=[]{}|;':\",./<>?";

    // Stream type
    public static final String[] st = {
            "!t:", // 0 text
            "!c:", // 1 command
            "!j:", // 2 json
            "!o:", // 3 opium
            "!f:", // 4 file
            "!u:", // 5 user
            "!b:", // 6 binary
            "!s:", // 7 spider
            "!h:", // 8 html
    };

    public static <T> boolean anyMatch(List<T> list, Predicate<T> predicate) {
        for (T element : list) {
            if (predicate.test(element)) {
                return true;
            }
        }
        return false;
    }


}
