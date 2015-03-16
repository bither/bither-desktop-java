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

import net.bither.bitherj.utils.UpgradeAddressUtil;
import net.bither.preference.UserPreference;
import net.bither.runnable.BaseRunnable;
import net.bither.runnable.RunnableListener;

public class UpgradeUtil {
    public static final int UPGRADE_ADDRESS_TO_DB = 131;

    private UpgradeUtil() {

    }

    public static boolean needUpgrade() {
        int verionCode = UserPreference.getInstance().getVerionCode();
        return verionCode < UPGRADE_ADDRESS_TO_DB;
    }

    public static void upgradeNewVerion(final RunnableListener handler) {
        BaseRunnable baseRunnable = new BaseRunnable() {
            @Override
            public void run() {
                handler.prepare();
                try {
                    int verionCode = UserPreference.getInstance().getVerionCode();
                    if (verionCode < UPGRADE_ADDRESS_TO_DB) {
                        UpgradeAddressUtil.upgradeAddress();
                    }
                    handler.success(null);
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.error(0, null);
                }

            }
        };
        new Thread(baseRunnable).start();

    }


}
