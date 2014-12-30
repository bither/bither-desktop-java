package net.bither.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    public static boolean checkBackupFileOfCold(String fileName) {
        Pattern pattern = Pattern.compile("[^-]{6,6}_[^-]{6,6}.bak");
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

}
