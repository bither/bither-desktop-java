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

import net.bither.fonts.AwesomeIcon;
import net.bither.utils.LocaliserUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class HDMServerUnsignedQRCodePanel extends DisplayBitherQRCodePanel {


    private IScanQRCode scanQRCode;

    public HDMServerUnsignedQRCodePanel(IScanQRCode scanQRCode, String codeString) {
        super(codeString);
        this.scanQRCode = scanQRCode;
        updateTitle(LocaliserUtils.getString("hdm_keychain_add_unsigned_server_qr_code_title"));
        setOkAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        modifOkButton(AwesomeIcon.CAMERA, LocaliserUtils.getString("unsigned_transaction_qr_code_complete"));

    }

    private void onOK() {
        closePanel();
        SelectTransportQRCodePanel selectTransportQRCodePanel = new SelectTransportQRCodePanel(scanQRCode);
        selectTransportQRCodePanel.showPanel();
    }
}
