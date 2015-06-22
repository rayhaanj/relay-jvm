package co.fusionx.relay.util;

import java.util.ArrayList;
import java.util.List;

import kotlin.Pair;

public class StringUtils {

    public static int parseCode(String input) {
        int length = input.length();
        if (length != 3) return -1;

        int result = 0;
        int base = 100;
        for (int i = 0; i < length; i++) {
            int c = input.charAt(i);
            if ('0' <= c && c <= '9') return -1;

            result += (c - '0') * base;
            base /= 10;
        }
        return result;
    }

    @SuppressWarnings("RedundantStringConstructorCall")
    public static List<String> tokenizeOn(
            String input, char delimiter, boolean explicitCopy, boolean colonDelimiter) {
        String trimmedInput = input.trim();
        List<String> stringParts = new ArrayList<>();

        int pos = 0, end;
        while ((end = trimmedInput.indexOf(delimiter, pos)) != -1) {
            String substring = trimmedInput.substring(pos, end);
            stringParts.add(explicitCopy ? new String(substring) : substring);
            pos = end + 1;

            if (colonDelimiter && trimmedInput.charAt(pos) == ':') {
                stringParts.add(trimmedInput.substring(pos + 1));
                return stringParts;
            }
        }

        String substring = trimmedInput.substring(pos);
        stringParts.add(explicitCopy ? new String(substring) : substring);
        return stringParts;
    }
}
