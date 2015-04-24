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

package net.bither.utils;


import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.crypto.ECKey;
import net.bither.bitherj.crypto.PasswordSeed;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.PrivateKeyUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.preference.UserPreference;
import net.bither.runnable.BaseRunnable;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

// TODO : backup hot wallet's encrypted private keys & public keys in the rom
public class BackupUtil {

    public interface BackupListener {
        void backupSuccess();

        void backupError();
    }


    private static long ONE_WEEK_TIME = 7 * 24 * 60 * 60 * 1000;


    private BackupUtil() {

    }


    public static ECKey getEckeyFromBackup(String address, CharSequence password) {
        if (UserPreference.getInstance().getAppMode() == BitherjSettings.AppMode.COLD) {
            return getEckeyFormBackupCold(address, password);
        } else {
            return getEckeyFormBackupHot(address, password);
        }

    }

    private static ECKey getEckeyFormBackupHot(String address, CharSequence password) {
        File file = FileUtil.getBackupFile();
        String str = Utils.readFile(file);
        if (str.contains(address)) {
            String[] backupStrArray = str.split(PrivateKeyUtil.BACKUP_KEY_SPLIT_MUTILKEY_STRING);
            for (String backupStr : backupStrArray) {
                if (backupStr.contains(address)) {
                    String[] strArray = QRCodeUtil.splitString(backupStr);
                    if (strArray.length > 3) {
                        String keyString = backupStr.substring(strArray[0]
                                .length() + 1);
                        return PrivateKeyUtil.getECKeyFromSingleString(
                                keyString, password);
                    }
                }
            }
        }
        return null;
    }

    private static ECKey getEckeyFormBackupCold(String address, CharSequence password) {

        try {
            File[] files = FileUtil.getBackupDir().listFiles();
            if (files == null) {
                return null;
            }
            files = FileUtil.orderByDateDesc(files);
            for (int i = files.length - 1;
                 i >= 0;
                 i++) {
                File file = files[i];
                String str = Utils.readFile(file);
                if (str.contains(address)) {
                    String[] backupStrArray = str.split(PrivateKeyUtil.BACKUP_KEY_SPLIT_MUTILKEY_STRING);
                    for (String backupStr : backupStrArray) {
                        if (backupStr.contains(address)) {
                            String[] strArray = QRCodeUtil.splitString(backupStr);
                            if (strArray.length > 3) {
                                String keyString = backupStr
                                        .substring(strArray[0].length() + 1);
                                return PrivateKeyUtil.getECKeyFromSingleString(
                                        keyString, password);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static void backupColdKey(boolean checkTime) {
        backupColdKey(checkTime, null);

    }

    public static void backupColdKey(boolean checkTime,
                                     BackupListener backupListener) {
        if (UserPreference.getInstance().getAppMode() != BitherjSettings.AppMode.COLD
                ) {
            if (backupListener != null) {
                backupListener.backupError();
            }
            return;
        }

        boolean isBackup = false;
        if (checkTime) {
            Date lastBackupTime = UserPreference.getInstance()
                    .getLastBackupkeyTime();
            List<File> files = FileUtil.getBackupFileListOfCold();
            if (lastBackupTime == null
                    || ((lastBackupTime.getTime() + ONE_WEEK_TIME) < System
                    .currentTimeMillis()) || files.size() == 0) {
                isBackup = true;
            }
        } else {
            isBackup = true;
        }
        if (isBackup) {
            BackupPrivateKeyRunnable backupColdPrivateKeyRunnable = new
                    BackupPrivateKeyRunnable(
                    backupListener);
            new Thread(backupColdPrivateKeyRunnable).start();

        } else {
            if (backupListener != null) {
                backupListener.backupError();
            }
        }
    }

    public static void backupHotKey() {
        if (UserPreference.getInstance().getAppMode() == BitherjSettings.AppMode.HOT) {
            BackupPrivateKeyRunnable backupColdPrivateKeyRunnable = new BackupPrivateKeyRunnable(
                    null);
            new Thread(backupColdPrivateKeyRunnable).start();
        }
    }

    public static String[] getBackupKeyStrList(File file) {
        String keyStrs = Utils.readFile(file);
        String[] result = null;
        if (!Utils.isEmpty(keyStrs)) {
            result = keyStrs.split(PrivateKeyUtil.BACKUP_KEY_SPLIT_MUTILKEY_STRING);
        }
        return result;
    }


    private static class BackupPrivateKeyRunnable extends BaseRunnable {

        private BackupListener mBackupListener;

        public BackupPrivateKeyRunnable(BackupListener backupListener) {
            this.mBackupListener = backupListener;

        }

        @Override
        public void run() {
            prepare();
            backupPrivateKey();
            if (this.mBackupListener != null) {

                mBackupListener.backupSuccess();

            }
            success(null);

        }

        private void backupPrivateKey() {
            File file = FileUtil.getBackupFile();

            String backupString = PrivateKeyUtil.getBackupPrivateKeyStr();


            if (!Utils.isEmpty(backupString)) {

                try {
                    Utils.writeFile(backupString.getBytes(), file);
                    UserPreference.getInstance().setLastBackupKeyTime(
                            new Date(System.currentTimeMillis()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

}
