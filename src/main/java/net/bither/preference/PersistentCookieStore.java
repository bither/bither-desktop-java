/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.bither.preference;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PersistentCookieStore implements CookieStore {
    private static final String COOKIE_PREFS = "CookiePrefsFile";
    private static final String COOKIE_NAME_STORE = "names";
    private static final String COOKIE_NAME_PREFIX = "cookie_";

    private Properties userPreferences = UserPreference.getInstance().getUserPreferences();

    private Map<String, Cookie> cookies;
    private static PersistentCookieStore persistentCookieStore = new PersistentCookieStore();

    public static PersistentCookieStore getInstance() {
        return persistentCookieStore;
    }

    /**
     * Construct a persistent cookie store.
     */
    private PersistentCookieStore() {
        reload();
    }

    @Override
    public void addCookie(Cookie cookie) {
        String name = cookie.getName();

        // Save cookie into local store, or remove if expired
        if (!cookie.isExpired(new Date())) {
            cookies.put(name, cookie);
        } else {
            cookies.remove(name);
        }

        // Save cookie into persistent store

        Set<String> keySet = cookies.keySet();
        userPreferences.setProperty(COOKIE_NAME_STORE,
                join(",", keySet));
        userPreferences.setProperty(COOKIE_NAME_PREFIX + name,
                encodeCookie(new SerializableCookie(cookie)));
        UserPreference.getInstance().saveUserPreferences();
    }

    @Override
    public void clear() {
        // Clear cookies from local store
        cookies.clear();
        // Clear cookies from persistent store
        Set<String> keySet = cookies.keySet();
        for (String name : keySet) {
            userPreferences.remove(COOKIE_NAME_PREFIX + name);
        }
        userPreferences.remove(COOKIE_NAME_STORE);
        UserPreference.getInstance().saveUserPreferences();
    }

    @Override
    public boolean clearExpired(Date date) {
        boolean clearedAny = false;


        for (ConcurrentHashMap.Entry<String, Cookie> entry : cookies.entrySet()) {
            String name = entry.getKey();
            Cookie cookie = entry.getValue();
            if (cookie.isExpired(date)) {
                // Clear cookies from local store
                cookies.remove(name);

                // Clear cookies from persistent store
                userPreferences.remove(COOKIE_NAME_PREFIX + name);

                // We've cleared at least one
                clearedAny = true;
            }
        }
        // Update names in persistent store
        if (clearedAny) {
            Set<String> keySet = cookies.keySet();
            userPreferences.setProperty(COOKIE_NAME_STORE,
                    join(",", keySet));
            UserPreference.getInstance().saveUserPreferences();
        }

        return clearedAny;
    }

    @Override
    public List<Cookie> getCookies() {
        return new ArrayList<Cookie>(cookies.values());
    }

    //
    // Cookie serialization/deserialization
    //

    protected String encodeCookie(SerializableCookie cookie) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(os);
            outputStream.writeObject(cookie);
        } catch (Exception e) {
            return null;
        }

        return byteArrayToHexString(os.toByteArray());
    }

    protected Cookie decodeCookie(String cookieStr) {
        byte[] bytes = hexStringToByteArray(cookieStr);
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        Cookie cookie = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(is);
            cookie = ((SerializableCookie) ois.readObject()).getCookie();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cookie;
    }

    // Using some super basic byte array <-> hex conversions so we don't have
    // to rely on any large Base64 libraries. Can be overridden if you like!
    protected String byteArrayToHexString(byte[] b) {
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (byte element : b) {
            int v = element & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase(Locale.US);
    }

    protected byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return data;
    }


    public void reload() {
        //todo dir
        // Load any previously stored cookies into the store

        cookies = new ConcurrentHashMap<String, Cookie>();

        String storedCookieNames = userPreferences.getProperty(
                COOKIE_NAME_STORE, null);
        if (storedCookieNames != null) {
            String[] cookieNames = split(storedCookieNames, ",");
            for (String name : cookieNames) {
                String encodedCookie = userPreferences.getProperty(
                        COOKIE_NAME_PREFIX + name, null);
                if (encodedCookie != null) {
                    Cookie decodedCookie = decodeCookie(encodedCookie);
                    if (decodedCookie != null) {
                        cookies.put(name, decodedCookie);
                    }
                }
            }

            // Clear out expired cookies
            clearExpired(new Date());
        }
    }

    private String[] split(String text, String expression) {
        if (text.length() == 0) {
            return new String[]{};
        } else {
            return text.split(expression, -1);
        }
    }

    private String join(CharSequence delimiter, Iterable tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb.toString();
    }

}