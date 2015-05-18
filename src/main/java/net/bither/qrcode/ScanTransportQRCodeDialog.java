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

package net.bither.qrcode;

import net.bither.bitherj.qrcode.QRCodeTransportPage;
import net.bither.bitherj.utils.Utils;
import net.bither.utils.LocaliserUtils;

import javax.swing.*;
import java.util.ArrayList;

public class ScanTransportQRCodeDialog extends ScanQRCodeDialog {

    private int totalPage = 1;
    private ArrayList<QRCodeTransportPage> pages;
    private String lastResult;


    public ScanTransportQRCodeDialog(IScanQRCode scanQRCode) {
        super(scanQRCode);
        pages = new ArrayList<QRCodeTransportPage>();
    }

    public void handleResult(String result) {

        if (Utils.isEmpty(result)) {
            setMessage("No QR code");
        } else {
            if (resultValid(result)) {
                QRCodeTransportPage page = QRCodeTransportPage
                        .formatQrCodeTransport(result);
                pages.add(page);
                totalPage = page.getSumPage();
                if (page.getCurrentPage() < totalPage - 1) {
                    String str = String.format(LocaliserUtils.getString("scan_qr_transport_page_label"),
                            pages.size() + 1, totalPage);
                    setMessage(str);
                    //        startScan();
                } else {
                    complete();
                }
            } else {
                shake();
            }

        }

    }


    public boolean resultValid(String result) {
        if (!Utils.compareString(result, lastResult)) {
            shake();
        }
        lastResult = result;
        QRCodeTransportPage page = QRCodeTransportPage
                .formatQrCodeTransport(result);
        if (page == null) {
            return false;
        }
        if (page.getCurrentPage() == pages.size()) {
            return true;
        }
        return false;
    }

    private void shake() {
        //todo shake

    }


    private void complete() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    String string = QRCodeTransportPage.qrCodeTransportToString(pages);
                    scanQRCode.handleResult(string, ScanTransportQRCodeDialog.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }
}
