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

package net.bither.runnable;

import net.bither.bitherj.core.*;
import net.bither.bitherj.utils.TransactionsUtil;

import javax.swing.*;


public class CommitTransactionThread extends Thread {
    public static interface CommitTransactionListener {
        public void onCommitTransactionSuccess(Tx tx);

        public void onCommitTransactionFailed();
    }

    private Address wallet;
    private Tx tx;
    private CommitTransactionListener listener;

    public CommitTransactionThread(Address address, Tx tx,
                                   boolean withPrivateKey, CommitTransactionListener listener)
            throws Exception {
        wallet = address;

        this.listener = listener;
        if (address instanceof HDMAddress) {
        } else {
            if (withPrivateKey) {
                if (address.hasPrivKey()) {
                } else {
                    throw new Exception("address not with private key");
                }
            }
        }
        this.tx = tx;
    }

    @Override
    public void run() {
        super.run();
        boolean success = false;
        try {
            PeerManager.instance().publishTransaction(tx);
            TransactionsUtil.removeSignTx(new UnSignTransaction(tx, wallet.getAddress()));
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {
            final boolean s = success;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        if (s) {
                            listener.onCommitTransactionSuccess(tx);
                        } else {
                            listener.onCommitTransactionFailed();
                        }
                    }
                }
            });


        }
    }


}
