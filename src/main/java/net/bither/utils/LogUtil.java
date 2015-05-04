package net.bither.utils;

import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.utils.Utils;

/**
 * Created by nn on 15/5/4.
 */
public class LogUtil {
    public static void printlnOut(String str) {
        if (!BitherjSettings.LOG_DEBUG) {
            return;
        }
        if (Utils.isEmpty(str)) {
            return;
        }
        System.out.println(str);
    }

    public static void printlnError(String str) {
        if (!BitherjSettings.LOG_DEBUG) {
            return;
        }
        if (Utils.isEmpty(str)) {
            return;
        }
        System.err.println(str);
    }
}
