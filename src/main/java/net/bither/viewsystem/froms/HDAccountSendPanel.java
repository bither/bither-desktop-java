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

package net.bither.viewsystem.froms;

import net.bither.Bither;
import net.bither.bitherj.core.HDAccount;
import net.bither.bitherj.core.PeerManager;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.crypto.KeyCrypterException;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.crypto.mnemonic.MnemonicException;
import net.bither.bitherj.exception.TxBuilderException;
import net.bither.bitherj.utils.GenericUtils;
import net.bither.bitherj.utils.Utils;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.WalletUtils;
import net.bither.viewsystem.dialogs.DialogProgress;
import net.bither.viewsystem.dialogs.MessageDialog;

import javax.swing.*;

public class HDAccountSendPanel extends SendBitcoinPanel implements SendBitcoinConfirmPanel.SendConfirmListener {

    static {
        WalletUtils.initTxBuilderException();
    }


    private long btcAmount;
    private String toAddress;
    private Tx tx;

    public HDAccountSendPanel() {
        super();
    }

    public HDAccountSendPanel(String doateAddress) {
        super(doateAddress);
    }

    private DialogProgress dp = new DialogProgress();

    @Override
    public void initialiseContent(JPanel panel) {
        super.initialiseContent(panel);
        btnChangeAddress.setVisible(false);
    }

    @Override
    protected void onSend() {
        String amtString = tfAmt.getText().trim();
        long btc = GenericUtils.toNanoCoins(amtString, 0).longValue();

        if (btc > 0) {
            btcAmount = btc;
            tx = null;
            if (Utils.validBicoinAddress(tfAddress.getText().toString().trim())) {
                toAddress = tfAddress.getText().toString().trim();

                new Thread() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                dp.pack();
                                dp.setVisible(true);
                            }
                        });
                        send();
                    }
                }.start();
            } else {
                new MessageDialog(LocaliserUtils.getString("send_failed")).showMsg();

            }
        }
    }

    private void send() {
        tx = null;
        HDAccount account = (HDAccount) Bither.getActionAddress();
        SecureCharSequence password = new SecureCharSequence(currentPassword.getPassword());
        try {
            tx = account.newTx(toAddress, btcAmount, password);
        } catch (Exception e) {
            e.printStackTrace();
            btcAmount = 0;
            tx = null;
            String msg = LocaliserUtils.getString("send_failed");
            if (e instanceof KeyCrypterException || e instanceof MnemonicException
                    .MnemonicLengthException) {
                msg = LocaliserUtils.getString("password_wrong");
            } else if (e instanceof TxBuilderException) {
                msg = e.getMessage();
            }
            final String m = msg;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    dp.dispose();
                    new MessageDialog(m).showMsg();
                }
            });
        } finally {
            password.wipe();
        }
        if (tx != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    showConfirm();
                }
            });
        }
    }

    private void showConfirm() {
        dp.dispose();
        SendBitcoinConfirmPanel sendBitcoinConfirmPanel =
                new SendBitcoinConfirmPanel(this, toAddress, null, tx);
        sendBitcoinConfirmPanel.showPanel();
    }


    @Override
    public void onConfirm(Tx request) {


        new Thread() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dp.pack();
                        dp.setVisible(true);
                    }
                });
                boolean success = false;
                try {
                    PeerManager.instance().publishTransaction(tx);
                    success = true;
                    tx = null;
                    toAddress = null;
                    btcAmount = 0;
                } catch (PeerManager.PublishUnsignedTxException e) {
                    e.printStackTrace();
                    tx = null;
                    toAddress = null;
                    btcAmount = 0;
                }
                if (success) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dp.dispose();
                            closePanel();
                            if (Utils.isEmpty(doateAddress)) {
                                new MessageDialog(LocaliserUtils.getString("send_success")).showMsg();
                            } else {
                                new MessageDialog(LocaliserUtils.getString("donate_thanks")).showMsg();
                            }
                        }
                    });
                } else {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dp.dispose();
                            new MessageDialog(LocaliserUtils.getString("send_failed")).showMsg();
                        }
                    });

                }
            }
        }.start();

    }

    @Override
    public void onCancel() {
        tx = null;
        toAddress = null;
        btcAmount = 0;
    }
}
