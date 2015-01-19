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

package net.bither.implbitherj;


import net.bither.ApplicationDataDirectoryLocator;
import net.bither.bitherj.AbstractApp;
import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.ISetting;
import net.bither.bitherj.NotificationService;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.preference.PersistentCookieStore;
import net.bither.preference.UserPreference;
import org.apache.http.client.CookieStore;

import java.io.File;

public class DesktopImplAbstractApp extends AbstractApp {


    @Override
    public ISetting initSetting() {
        return new ISetting() {
            @Override
            public BitherjSettings.AppMode getAppMode() {
                return UserPreference.getInstance().getAppMode();
            }

            @Override
            public boolean getBitherjDoneSyncFromSpv() {
                return UserPreference.getInstance().getBitherjDoneSyncFromSpv();
            }

            @Override
            public void setBitherjDoneSyncFromSpv(boolean isDone) {
                UserPreference.getInstance().setBitherjDoneSyncFromSpv(isDone);
            }

            @Override
            public boolean getDownloadSpvFinish() {
                return UserPreference.getInstance().getDownloadSpvFinish();
            }

            @Override
            public void setDownloadSpvFinish(boolean finish) {
                UserPreference.getInstance().setDownloadSpvFinish(finish);

            }

            @Override
            public BitherjSettings.TransactionFeeMode getTransactionFeeMode() {
                return UserPreference.getInstance().getTransactionFeeMode();
            }

            @Override
            public File getPrivateDir(String dirName) {
                File file = new File(new ApplicationDataDirectoryLocator().getApplicationDataDirectory() + File.separator + dirName);
                if (!file.exists()) {
                    file.mkdirs();
                }
                return file;
            }

            @Override
            public boolean isApplicationRunInForeground() {

                return true;
            }

            @Override
            public CookieStore getCookieStore() {
                return PersistentCookieStore.getInstance();
            }

            @Override
            public QRCodeUtil.QRQuality getQRQuality() {
                return UserPreference.getInstance().getQRQuality();
            }
        };
    }

    @Override
    public NotificationService initNotification() {
        return new NotificationDesktopImpl();
    }
}
