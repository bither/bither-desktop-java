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
