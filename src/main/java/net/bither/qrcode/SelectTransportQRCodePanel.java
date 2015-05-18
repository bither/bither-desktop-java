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
import java.io.File;
import java.util.ArrayList;


public class SelectTransportQRCodePanel extends SelectQRCodePanel {

    private int totalPage = 1;
    private ArrayList<QRCodeTransportPage> pages;
    private String lastResult;

    public SelectTransportQRCodePanel(IScanQRCode scanQRCode) {
        super(scanQRCode);
        pages = new ArrayList<QRCodeTransportPage>();

    }

    @Override
    public void initialiseContent(JPanel panel) {
        super.initialiseContent(panel);
        setMsg(LocaliserUtils.getString("scan_qr_transport_init_label"));
    }

    @Override
    protected void fromFile() {

        startFileChooser(new IFileChooser() {
            @Override
            public void selectFile(File file) {
                if (file != null) {
                    String str = QRCodeEncoderDecoder.decode(file);
                    handleResult(str);

                }

            }
        });
    }

    @Override
    protected void fromScan() {
        close();
        ScanTransportQRCodeDialog scanQRCodeDialog = new ScanTransportQRCodeDialog(this.scanQRCode);
        scanQRCodeDialog.pack();
        scanQRCodeDialog.setVisible(true);
    }

    public void handleResult(String result) {
        if (Utils.isEmpty(result)) {
            setMsg(LocaliserUtils.getString("no_format_qr_code"));
        } else {
            if (resultValid(result)) {
                QRCodeTransportPage page = QRCodeTransportPage
                        .formatQrCodeTransport(result);
                pages.add(page);
                totalPage = page.getSumPage();
                if (page.getCurrentPage() < totalPage - 1) {
                    String str = String.format(LocaliserUtils.getString("scan_qr_transport_page_label"),
                            pages.size() + 1, totalPage);
                    setMsg(str);

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
        System.out.println("shake");
    }

    private void complete() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    String string = QRCodeTransportPage.qrCodeTransportToString(pages);
                    scanQRCode.handleResult(string, SelectTransportQRCodePanel.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }


}
