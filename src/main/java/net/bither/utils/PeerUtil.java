package net.bither.utils;

import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.BitherjSettings;
import net.bither.bitherj.core.PeerManager;
import net.bither.preference.UserPreference;

public class PeerUtil {
    public static synchronized void startPeer() {
        try {

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
                System.out.println(" PeerManager.instance().start()");
            }
        }

    }

    public static void stopPeer() {
        PeerManager.instance().stop();
    }
}
