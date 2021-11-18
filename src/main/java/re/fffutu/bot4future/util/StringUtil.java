package re.fffutu.bot4future.util;

import java.util.Arrays;

public class StringUtil {
    public static String padStart(String string, int length, char charToFill) {
        if(string.length() >= length) return string;
        char[] array = new char[length - string.length()];
        Arrays.fill(array, charToFill);
        return new String(array) + string;
    }

    public static String padEnd(String string, int length, char charToFill) {
        if(string.length() >= length) return string;
        char[] array = new char[length - string.length()];
        Arrays.fill(array, charToFill);
        return string + new String(array);
    }
}
