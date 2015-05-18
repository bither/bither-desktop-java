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

import net.bither.Bither;
import net.bither.bitherj.core.HDAccount;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.utils.UnitUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.dialogs.MessageDialog;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class TxNotificationCenter {
    public interface ITxListener {
        public void notificatTx(String address, Tx tx,
                                Tx.TxNotificationType txNotificationType, long deltaBalance);
    }

    private static List<ITxListener> txListenerList = new ArrayList<ITxListener>();

    public static void notificatTx(final String address, final Tx tx,
                                   final Tx.TxNotificationType txNotificationType, final long deltaBalance) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (Utils.compareString(address, HDAccount.HDAccountPlaceHolder)) {
                    Bither.refreshFrame();
                }
                for (ITxListener txListener : txListenerList) {
                    txListener.notificatTx(address, tx, txNotificationType, deltaBalance);
                }
                if (txNotificationType == Tx.TxNotificationType.txReceive) {
                    boolean isReceived = deltaBalance > 0;
                    long balance = Math.abs(deltaBalance);
                    notifyCoins(address, balance, isReceived);
                }
            }
        });


    }

    public static void addTxListener(ITxListener txListener) {
        if (!txListenerList.contains(txListener)) {
            txListenerList.add(txListener);
        }
    }

    public static void removeTxListener(ITxListener txListener) {
        txListenerList.add(txListener);
    }

    private static void notifyCoins(String address, final long amount,
                                    boolean isReceived) {
        String contentText = address;
        if (Utils.compareString(address, HDAccount.HDAccountPlaceHolder)) {
            contentText = LocaliserUtils.getString("add_hd_account_tab_hd");
        }
        String title = UnitUtil.formatValue(amount, UnitUtil.BitcoinUnit.BTC) + " " + UnitUtil.BitcoinUnit.BTC.name();
        if (isReceived) {
            title = LocaliserUtils.getString("feed_received_btc") + " " + title;
        } else {
            title = LocaliserUtils.getString("feed_send_btc") + " " + title;
        }
        final String msg = contentText + " \n" + title;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MessageDialog(msg).showMsg();
            }
        });

    }
}
