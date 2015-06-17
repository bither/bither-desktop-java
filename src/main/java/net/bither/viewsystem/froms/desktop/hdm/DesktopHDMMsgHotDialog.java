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

package net.bither.viewsystem.froms.desktop.hdm;

import net.bither.bitherj.core.Tx;
import net.bither.viewsystem.dialogs.AbstractDesktopHDMMsgDialog;

import javax.swing.*;

/**
 * Created by nn on 15/6/17.
 */
public class DesktopHDMMsgHotDialog extends AbstractDesktopHDMMsgDialog {

    private static final long CHECK_TX_INTERVAL = 3 * 1000;

    private Tx tx;

    @Override
    public void handleScanResult(final String result) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("scan :" + result);
                if (desktopQRCodSend != null) {
                    desktopQRCodSend.setReceiveMsg(result);
                }
                if (desktopQRCodSend.signComplete()) {
                    publishTx();
                }

                if (desktopQRCodSend.canNextPage()) {
                    showQRCode(desktopQRCodReceive.getShowMsg());
                    return;
                }


            }
        });

    }

    public void publishTx() {

    }

    @Override
    protected void inited() {
        refreshTx();
    }

    private void refreshTx() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        if (desktopQRCodSend == null || desktopQRCodSend.signComplete()) {
                            getTx();
                        } else {
                            if (desktopQRCodSend.canNextPage()) {
                                showQRCode(desktopQRCodSend.getShowMessage());
                            }

                        }

                        Thread.sleep(CHECK_TX_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

    //todo get tx
    private void getTx() {
        //todo init DesktopQRCodSend
    }

}
