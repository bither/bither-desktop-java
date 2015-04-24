package net.bither.implbitherj;

import net.bither.bitherj.core.Tx;
import net.bither.bitherj.utils.UnitUtil;
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
        final String contentText = address;
        String title = UnitUtil.formatValue(amount, UnitUtil.BitcoinUnit.BTC) + " " + UnitUtil.BitcoinUnit.BTC.name();
        if (isReceived) {
            title = LocaliserUtils.getString("feed_received_btc") + " " + title;
        } else {
            title = LocaliserUtils.getString("feed_send_btc") + " " + title;
        }
        final String msg = contentText + "\n" + title;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MessageDialog(msg).showMsg();
            }
        });

    }
}
