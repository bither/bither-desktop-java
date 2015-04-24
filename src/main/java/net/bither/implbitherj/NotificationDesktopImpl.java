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

import net.bither.bitherj.AbstractApp;
import net.bither.bitherj.NotificationService;
import net.bither.bitherj.core.Tx;
import net.bither.utils.PeerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationDesktopImpl implements NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);


    @Override
    public void sendLastBlockChange() {
        BlockNotificationCenter.notificationBlockChange();

    }

    @Override
    public void notificatTx(String address, Tx tx, Tx.TxNotificationType txNotificationType, long deltaBalance) {
        TxNotificationCenter.notificatTx(address, tx, txNotificationType, deltaBalance);

    }

    @Override
    public void sendBroadcastAddressLoadCompleteState() {
        AddressNotificationCenter.notificationAddressLoadComplete();

    }

    @Override
    public void sendBroadcastPeerState(final int numPeers) {
        PeerNotificationCenter.sendBroadcastPeerState(numPeers);

    }

    @Override
    public void sendConnectedChangeBroadcast(String connectedChangeBroadcast, boolean isConnected) {
        PeerNotificationCenter.sendConnectedChangeBroadcast(connectedChangeBroadcast, isConnected);
    }

    @Override
    public void sendBroadcastProgressState(double value) {
        PeerNotificationCenter.sendBroadcastProgressState(value);

    }

    @Override
    public void removeProgressState() {
        PeerNotificationCenter.removeProgressState();
    }


    @Override
    public void sendBroadcastSyncSPVFinished(boolean isFinished) {
        if (isFinished) {
            AbstractApp.bitherjSetting.setBitherjDoneSyncFromSpv(isFinished);
            PeerUtil.startPeer();
        }
    }

    @Override
    public void removeBroadcastSyncSPVFinished() {

    }

    @Override
    public void sendBroadcastGetSpvBlockComplete(boolean isComplete) {

    }

    @Override
    public void removeBroadcastPeerState() {

    }

    @Override
    public void removeAddressLoadCompleteState() {

    }


}
