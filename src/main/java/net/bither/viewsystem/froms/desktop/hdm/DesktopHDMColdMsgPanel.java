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

import com.github.sarxos.webcam.Webcam;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.DesktopHDMKeychain;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.qrcode.QRCodeTxTransport;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.qrcode.DesktopQRCodReceive;
import net.bither.qrcode.DesktopQRCodSend;
import net.bither.viewsystem.dialogs.AbstractDesktopHDMMsgDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DesktopHDMColdMsgPanel extends AbstractDesktopHDMMsgDialog {

    private SecureCharSequence password;
    private String lastResult;

    public DesktopHDMColdMsgPanel(SecureCharSequence password, Webcam webcam) {
        super(webcam);
        isSendMode = false;
        this.password = password;
    }

    @Override
    protected void handleScanResult(String result) {
        if (Utils.compareString(result, lastResult)) {
            return;
        }
        lastResult = result;

        if (isSendMode) {
            if (desktopQRCodSend != null) {
                desktopQRCodSend.setReceiveMsg(result);
                showQRCode(desktopQRCodSend.getShowMessage());
                if (desktopQRCodSend.allComplete()) {
                    isSendMode = false;
                }
            }

        } else {
            desktopQRCodReceive.receiveMsg(result);
            showQRCode(desktopQRCodReceive.getShowMsg());
            if (desktopQRCodReceive.receiveComplete()) {
                isSendMode = true;
                desktopQRCodSend = new DesktopQRCodSend(getSignString());
                showQRCode(desktopQRCodSend.getShowMessage());
            }
        }
        if (desktopQRCodSend != null && desktopQRCodSend.allComplete()
                && desktopQRCodReceive != null && desktopQRCodReceive.receiveComplete()) {
            desktopQRCodReceive = new DesktopQRCodReceive();
        }


    }

    @Override
    protected void inited() {
        desktopQRCodReceive = new DesktopQRCodReceive();

    }

    private String getSignString() {
        String string = desktopQRCodReceive.getReceiveResult();
        QRCodeTxTransport qrCodeTransportPage = QRCodeTxTransport.formatQRCodeTransportOfDesktopHDM(string);
        DesktopHDMKeychain desktopHDMKeychain = AddressManager.getInstance().getDesktopHDMKeychains().get(0);
        List<byte[]> unsignHashs = new ArrayList<byte[]>();
        for (String str : qrCodeTransportPage.getHashList()) {
            unsignHashs.add(Utils.hexStringToByteArray(str));
        }
        List<byte[]> signatureList = desktopHDMKeychain.signWithCold(unsignHashs, password, qrCodeTransportPage.getPathTypeIndexes());
        List<String> result = new ArrayList<String>();
        for (byte[] signature : signatureList) {
            result.add(Utils.bytesToHexString(signature).toUpperCase(Locale.US));
        }
        return Utils.joinString(result, QRCodeUtil.QR_CODE_SPLIT);
    }
}
