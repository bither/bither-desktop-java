package net.bither.utils;

import net.bither.bitherj.utils.Base58;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
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

    public static String validBicoinAddressBegin(String input) {
        final Pattern PATTERN_BITCOIN_ADDRESS = Pattern.compile("[^"
                + new String(Base58.ALPHABET) + "]{1,30}");
        Matcher matcher = PATTERN_BITCOIN_ADDRESS.matcher(input);
        if (!matcher.find()) {
            return null;
        } else {
            return matcher.toMatchResult().group();
        }


    }


}
