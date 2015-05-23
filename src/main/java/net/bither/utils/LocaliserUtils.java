/*
 *
 *  Copyright 2014 http://Bither.net
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

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
