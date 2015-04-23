package net.bither.utils;

import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.PeerManager;
import net.bither.bitherj.utils.BlockUtil;
import net.bither.bitherj.utils.TransactionsUtil;
import net.bither.preference.UserPreference;

public class PeerUtil {

    public static void startPeer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                startPeerInBackground();
            }
        }).start();

    }

    private static synchronized void startPeerInBackground() {
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
            }
        }

    }

    public static void stopPeer() {
        PeerManager.instance().stop();
    }
}
