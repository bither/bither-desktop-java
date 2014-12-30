package net.bither.implbitherj;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class PeerNotificationCenter {

    public interface IPeerListener {
        public void sendBroadcastPeerState(final int numPeers);

        public void sendConnectedChangeBroadcast(String connectedChangeBroadcast, boolean isConnected);

        public void sendBroadcastProgressState(double value);

        public void removeProgressState();

    }

    private static List<IPeerListener> peerListeners = new ArrayList<IPeerListener>();

    public static void sendBroadcastPeerState(final int numPeers) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (IPeerListener peerListener : peerListeners) {
                    peerListener.sendBroadcastPeerState(numPeers);
                }
            }
        });


    }


    public static void sendConnectedChangeBroadcast(final String connectedChangeBroadcast, final boolean isConnected) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (IPeerListener peerListener : peerListeners) {
                    peerListener.sendConnectedChangeBroadcast(connectedChangeBroadcast, isConnected);
                }
            }
        });
    }


    public static void sendBroadcastProgressState(final double value) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (IPeerListener peerListener : peerListeners) {
                    peerListener.sendBroadcastProgressState(value);
                }
            }
        });

    }


    public static void removeProgressState() {
        for (IPeerListener peerListener : peerListeners) {
            peerListener.removeProgressState();
        }

    }

    public static void addAddressListener(IPeerListener addressListener) {
        if (!peerListeners.contains(addressListener)) {
            peerListeners.add(addressListener);
        }
    }

    public static void removeAddressListener(IPeerListener addressListener) {
        peerListeners.remove(addressListener);
    }
}
