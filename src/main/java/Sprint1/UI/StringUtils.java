package Sprint1.UI;

/**
 * Utility class for string operations.
 * Provides Java 8 compatible alternatives to Java 11+ methods.
 */
public class StringUtils {
    
    /**
     * Returns a string whose value is the concatenation of this string repeated count times.
     * This is a Java 8 compatible alternative to String.repeat(int) which was introduced in Java 11.
     *
     * @param str   the string to repeat
     * @param count the number of times to repeat
     * @return a string consisting of this string repeated count times
     */
    public static String repeat(String str, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative: " + count);
        }
        if (count == 0) {
            return "";
        }
        if (str == null || str.isEmpty()) {
            return str;
        }
        
        StringBuilder sb = new StringBuilder(str.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}

