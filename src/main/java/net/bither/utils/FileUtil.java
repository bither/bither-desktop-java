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

import net.bither.Bither;
import net.bither.BitherSetting;

import java.io.*;
import java.util.*;

/**
 * Created by nn on 14-11-10.
 */
public class FileUtil {

    public static final String USER_PROPERTIES_FILE_NAME = "bither.properties";

    // Nonsense bytes to fill up deleted files - these have no meaning.
    private static byte[] NONSENSE_BYTES = new byte[]{(byte) 0xF0, (byte) 0xA6, (byte) 0x55, (byte) 0xAA, (byte) 0x33,
            (byte) 0x77, (byte) 0x33, (byte) 0x37, (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0xC2, (byte) 0xB3,
            (byte) 0xA4, (byte) 0x9A, (byte) 0x30, (byte) 0x7F, (byte) 0xE5, (byte) 0x5A, (byte) 0x23, (byte) 0x47, (byte) 0x13,
            (byte) 0x17, (byte) 0x15, (byte) 0x32, (byte) 0x5C, (byte) 0x77, (byte) 0xC9, (byte) 0x73, (byte) 0x04, (byte) 0x2D,
            (byte) 0x40, (byte) 0x0F, (byte) 0xA5, (byte) 0xA6, (byte) 0x43, (byte) 0x77, (byte) 0x33, (byte) 0x3B, (byte) 0x62,
            (byte) 0x34, (byte) 0xB6, (byte) 0x72, (byte) 0x32, (byte) 0xB3, (byte) 0xA4, (byte) 0x4B, (byte) 0x80, (byte) 0x7F,
            (byte) 0xC5, (byte) 0x43, (byte) 0x23, (byte) 0x47, (byte) 0x13, (byte) 0xB7, (byte) 0xA5, (byte) 0x32, (byte) 0xDC,
            (byte) 0x79, (byte) 0x19, (byte) 0xB1, (byte) 0x03, (byte) 0x9D};

    private static int BULKING_UP_FACTOR = 16;
    private static byte[] SECURE_DELETE_FILL_BYTES = new byte[NONSENSE_BYTES.length * BULKING_UP_FACTOR];

    static {
        // Make some SECURE_DELETE_FILL_BYTES bytes = x BULKING_UP_FACTOR the
        // NONSENSE just to save write time.
        for (int i = 0; i < BULKING_UP_FACTOR; i++) {
            System.arraycopy(NONSENSE_BYTES, 0, SECURE_DELETE_FILL_BYTES, NONSENSE_BYTES.length * i, NONSENSE_BYTES.length);
        }
    }


    private static final String EXCHANGERATE = "exchangerate";
    private static final String CURRENCIES_RATE = "currencies_rate";
    private static final String MARKET_CAHER = "mark";
    private static final String EXCAHNGE_TICKER_NAME = "exchange.ticker";


    private static final String BITHER_BACKUP_SDCARD_DIR = "BitherBackup";
    private static final String BITHER_BACKUP_ROM_DIR = "backup";

    private static final String BITHER_BACKUP_HOT_FILE_NAME = "keys";

    public static File getExchangeRateFile() {
        File file = getDir("");
        return new File(file, EXCHANGERATE);
    }

    public static File getCurrenciesRateFile() {
        File file = getDir("");
        return new File(file, CURRENCIES_RATE);
    }

    public static File getTickerFile() {
        File file = getMarketCache();
        file = new File(file, EXCAHNGE_TICKER_NAME);
        return file;

    }

    private static File getMarketCache() {
        return getDir(MARKET_CAHER);

    }

    private static File getDir(String name) {
        File file = new File(Bither.getApplicationDataDirectoryLocator().getApplicationDataDirectory() + File.separator + name);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }


    public static Object deserialize(File file) {
        Object object = new Object();
        FileInputStream fos = null;
        try {
            if (!file.exists()) {
                return null;
            }
            fos = new FileInputStream(file);
            ObjectInputStream ois;
            ois = new ObjectInputStream(fos);
            object = ois.readObject();

        } catch (Exception e) {
            e.printStackTrace();
            if (file.exists()) {
                file.delete();
            }
            return null;

        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return object;
    }

    public static void serializeObject(File file, Object object) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    public static File getBackupDir() {
        File backupDir = getDir(BITHER_BACKUP_SDCARD_DIR);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        return backupDir;
    }

    public static File getBackupFile() {
        File file = new File(getBackupDir(),
                DateUtils.getNameForFile(System.currentTimeMillis())
                        + ".bak"
        );
        return file;
    }

    public static List<File> getBackupFileListOfCold() {
        File dir = getBackupDir();
        List<File> fileList = new ArrayList<File>();
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            files = orderByDateDesc(files);
            for (File file : files) {
                if (StringUtil.checkBackupFileOfCold(file.getName())) {
                    fileList.add(file);
                }
            }
        }
        return fileList;
    }


    public static File[] orderByDateDesc(File[] fs) {
        Arrays.sort(fs, new Comparator<File>() {
            public int compare(File f1, File f2) {
                long diff = f1.lastModified() - f2.lastModified();
                if (diff > 0) {
                    return -1;//-1 f1 before f2
                } else if (diff == 0) {
                    return 0;
                } else {
                    return 1;
                }
            }

            public boolean equals(Object obj) {
                return true;
            }

        });
        return fs;
    }

    public static Properties loadUserPreferences(String fileName) {
        Properties userPreferences = new Properties();
        try {
            String userPropertiesFilename;
            if ("".equals(Bither.getApplicationDataDirectoryLocator().getApplicationDataDirectory())) {
                userPropertiesFilename = fileName;
            } else {
                userPropertiesFilename = Bither.getApplicationDataDirectoryLocator().getApplicationDataDirectory() + File.separator
                        + fileName;
            }
            InputStream inputStream = new FileInputStream(userPropertiesFilename);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF8");
            userPreferences.load(inputStreamReader);
        } catch (FileNotFoundException e) {
            // Ok - may not have been created yet.
        } catch (IOException e) {
            // Ok may not be written yet.
        }

        return userPreferences;
    }

    public static void saveUserPreferences(Properties userPreferences, String fileName) {
        // Write the user preference properties.
        OutputStream outputStream = null;
        try {
            String userPropertiesFilename;
            if ("".equals(Bither.getApplicationDataDirectoryLocator().getApplicationDataDirectory())) {
                userPropertiesFilename = fileName;
            } else {
                userPropertiesFilename = Bither.getApplicationDataDirectoryLocator().getApplicationDataDirectory()
                        + File.separator + fileName;
            }

            outputStream = new FileOutputStream(userPropertiesFilename);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(bufferedOutputStream, "UTF8");
            userPreferences.store(outputStreamWriter, BitherSetting.USER_PROPERTIES_HEADER_TEXT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    outputStream = null;
                }
            }
        }
    }


}
