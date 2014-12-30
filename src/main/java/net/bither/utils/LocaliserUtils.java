package net.bither.utils;

import net.bither.bitherj.utils.Utils;

import java.util.Locale;

public class LocaliserUtils {
    private static Localiser mLocaliser = new Localiser(Locale.ENGLISH);

    private LocaliserUtils() {

    }

    public static String getString(String key) {
        return mLocaliser.getString(key);

    }

    public static String getString(String key, String[] errorMessage) {
        return mLocaliser.getString(key, errorMessage);

    }

    public static String getString(String key, Object[] message) {
        return mLocaliser.getString(key, message);

    }

    public static Locale getLocale() {
        return mLocaliser.getLocale();
    }


    public static void setLocale(Locale locale) {
        mLocaliser = new Localiser(locale);
    }

    public static void setLocaliser(Localiser localiser) {
        mLocaliser = localiser;
    }


    public static Localiser getLocaliser() {
        return mLocaliser;
    }

    public static boolean isChina() {
        String defaultCountry = Locale.getDefault().getCountry();
        if (Utils.compareString(defaultCountry, "CN") || Utils.compareString
                (defaultCountry, "cn")) {
            return true;
        } else {
            return false;
        }
    }
}
