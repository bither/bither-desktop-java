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

package net.bither.factory;

import net.bither.Bither;
import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.qrcode.QRCodeEnodeUtil;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.qrcode.IReadQRCode;
import net.bither.qrcode.IScanQRCode;
import net.bither.qrcode.SelectTransportQRCodePanel;
import net.bither.utils.KeyUtil;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.dialogs.DialogProgress;
import net.bither.viewsystem.dialogs.MessageDialog;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class MonitorAddress {

    private ArrayList<String> addresses = new ArrayList<String>();
    private DialogProgress progressDialog;

    public void monitorAddress() {
        SelectTransportQRCodePanel selectQRCodeDialog = new SelectTransportQRCodePanel(new IScanQRCode() {
            public void handleResult(final String result, IReadQRCode readQRCode) {
                readQRCode.close();

                if (Utils.isEmpty(result) || !QRCodeEnodeUtil.checkPubkeysQRCodeContent(result)) {
                    new MessageDialog(LocaliserUtils.getString("scan_for_all_addresses_in_bither_cold_failed")).showMsg();

                } else {
                    progressDialog = new DialogProgress();
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            processQrCodeContent(result);
                        }


                    };
                    thread.start();
                    progressDialog.pack();
                    progressDialog.setVisible(true);
                }
            }
        });
        selectQRCodeDialog.showPanel();

    }

    private void processQrCodeContent(String content) {
        try {
            addresses.clear();
            java.util.List<Address> wallets = QRCodeEnodeUtil.formatPublicString(content);
            for (Address address : wallets) {
                if (!AddressManager.getInstance().getAllAddresses().contains(address)) {
                    addresses.add(address.getAddress());
                }
            }
            addAddress(wallets);
        } catch (Exception e) {
            new MessageDialog(LocaliserUtils.getString("scan_for_all_addresses_in_bither_cold_failed")).showMsg();

        }
    }


    private void addAddress(
            final List<Address> wallets) {
        try {
            List<String> addressList = new ArrayList<String>();
            for (Address bitherAddress : wallets) {
                addressList.add(bitherAddress.getAddress());
            }
            KeyUtil.addAddressListByDesc(wallets);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dispose();
                    Bither.refreshFrame();

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dispose();
                    new MessageDialog(LocaliserUtils.getString("network_or_connection_error")).showMsg();
                }
            });

        }
    }

}
