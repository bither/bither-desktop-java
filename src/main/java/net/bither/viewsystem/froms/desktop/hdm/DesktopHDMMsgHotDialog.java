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
import net.bither.bitherj.utils.Utils;
import net.bither.qrcode.DesktopQRCodReceive;
import net.bither.qrcode.DesktopQRCodSend;
import net.bither.viewsystem.dialogs.AbstractDesktopHDMMsgDialog;

import javax.swing.*;

/**
 * Created by nn on 15/6/17.
 */
public class DesktopHDMMsgHotDialog extends AbstractDesktopHDMMsgDialog {

    private static final long CHECK_TX_INTERVAL = 3 * 1000;


    public DesktopHDMMsgHotDialog() {
        isSendMode = true;
    }

    private Tx tx;

    @Override
    public void handleScanResult(final String result) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("scan :" + result);
                if (isSendMode) {
                    if (desktopQRCodSend != null) {
                        desktopQRCodSend.setReceiveMsg(result);
                    }

                    if (desktopQRCodSend.sendComplete()) {
                        showQRCode(desktopQRCodReceive.getShowMsg());

                    }
                    if (desktopQRCodSend.allComplete()) {
                        isSendMode = false;
                        desktopQRCodReceive = new DesktopQRCodReceive();
                    }
                } else {
                    desktopQRCodReceive.receiveMsg(result);
                    showQRCode(desktopQRCodReceive.getShowMsg());

                }

                if (desktopQRCodSend.allComplete() && desktopQRCodReceive.receiveComplete()) {
                    publishTx();

                }


            }
        });

    }

    public void publishTx() {
        String signStr = desktopQRCodReceive.getReceiveResult();
        desktopQRCodReceive = null;
        desktopQRCodSend = null;


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
                        if (desktopQRCodSend == null) {
                            getTx();
                        }
                        Thread.sleep(CHECK_TX_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

    //todo get tx,init DesktopQRCodSend
    private void getTx() {
        byte[] rawTx = Utils.hexStringToByteArray("0100000001bdc0141fe3e5c2223a6d26a95acbf791042d93f9d9b8b38f133bf7adb5c1e293010000006a47304402202214770c0f5a9261190337273219a108132a4bc987c745db8dd6daded34b0dcb0220573de1d973166024b8342d6b6fef2a864a06cceee6aee13a910e5d8df465ed2a01210382b259804ad8d88b96a23222e24dd5a130d39588e78960c9e9b48a5b49943649ffffffff02a0860100000000001976a91479a7bf0bba8359561d4dab457042d7b632d5e64188ac605b0300000000001976a914b036c529faeca8040232cc4bd5918e709e90c4ff88ac00000000");
        tx = new Tx(rawTx);
        desktopQRCodSend = new DesktopQRCodSend(tx, "1JJZwUBy6Mr9i62JpGccBprqcte8eYxALg", 1);


    }

}
