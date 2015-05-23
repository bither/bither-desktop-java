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

import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.PeerManager;
import net.bither.bitherj.utils.BlockUtil;
import net.bither.bitherj.utils.TransactionsUtil;
import net.bither.preference.UserPreference;

import javax.swing.*;

public class PeerUtil {

    private static boolean peerCanRun = true;

    public static void startPeer() {
        peerCanRun = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                startPeerInBackground();
            }
        }).start();

    }

    private static synchronized void startPeerInBackground() {
        try {

            if (!peerCanRun) {
                return;
            }
            if (!UserPreference.getInstance().getDownloadSpvFinish()) {
                BlockUtil.dowloadSpvBlock();
            }
            if (UserPreference.getInstance().getAppMode() != BitherjSettings.AppMode.COLD) {
                if (!UserPreference.getInstance().getBitherjDoneSyncFromSpv()) {
                    if (!PeerManager.instance().isConnected()) {
                        PeerManager.instance().start();
                    }
                } else {
                    if (!AddressManager.getInstance().addressIsSyncComplete()) {
                        TransactionsUtil.getMyTxFromBither();
                    }
                    startPeerManager();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startPeerManager() {
        if (AddressManager.getInstance().addressIsSyncComplete()
                && UserPreference.getInstance().getBitherjDoneSyncFromSpv()
                && UserPreference.getInstance().getDownloadSpvFinish()) {
            if (!PeerManager.instance().isConnected()) {
                PeerManager.instance().start();

            }
        }

    }

    public static void stopPeer() {
        peerCanRun = false;
        PeerManager.instance().stop();
    }
}
