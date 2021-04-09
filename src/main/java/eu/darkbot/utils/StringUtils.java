package eu.darkbot.utils;

public class StringUtils {

    public static String replaceLastOccurrence(String string, char toReplace, char replacement) {
        int index = string.lastIndexOf(toReplace);
        if (index == -1) return string;

        return string.substring(0, index) + replacement + string.substring(index + 1);
    }
}
