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

    public static long maxUsed = 0;

    public static void maxUsedSize() {
        Runtime runtime = Runtime.getRuntime();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();

        long used = total - free;
        if (used > maxUsed) {
            System.gc();
            maxUsed = used;
            LogUtil.printlnOut("maxUsed:" + Math.round(maxUsed / 1e3));
        }
    }

    public static void callSystemGC() {
        Runtime runtime = Runtime.getRuntime();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long max = runtime.maxMemory();
        long used = total - free;
        LogUtil.printlnOut("   " + Math.round(used / 1e3) + " KB used before GC,total :" + Math.round(total / 1e3) + ",free:" + Math.round(free / 1e3));


        runtime.runFinalization();
        runtime.gc();
        runtime = Runtime.getRuntime();
        total = runtime.totalMemory();
        free = runtime.freeMemory();
        max = runtime.maxMemory();
        used = total - free;
        LogUtil.printlnOut(Math.round(used / 1e3) + " KB used after GC,total :" + Math.round(total / 1e3) + ",free:" + Math.round(free / 1e3));

    }
}
