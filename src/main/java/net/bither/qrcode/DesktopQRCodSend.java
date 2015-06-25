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

import net.bither.bitherj.core.AbstractHD;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.qrcode.QRCodeTxTransport;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.utils.LocaliserUtils;

import java.util.List;

public class DesktopQRCodSend {

    public static int QRCodeSendCode = 1;
    private static byte[] lock = new byte[0];
    private java.util.List<String> contents;
    private int sendCode;
    private int currentPage;
    private String receiveMsg;


    public DesktopQRCodSend(Tx tx, List<AbstractHD.PathTypeIndex> pathTypeIndexList, String changeAddress) {
        synchronized (lock) {
            QRCodeSendCode++;
            this.sendCode = QRCodeSendCode;
            String codeString = QRCodeTxTransport.getDeskpHDMPresignTxString(QRCodeTxTransport.TxTransportType.DesktopHDM,
                    tx, changeAddress,
                    LocaliserUtils.getString("address_cannot_be_parsed"), pathTypeIndexList);
            this.contents = QRCodeUtil.getQrCodeStringList(QRCodeUtil.encodeQrCodeString(codeString));
            this.currentPage = 0;

        }

    }

    public DesktopQRCodSend(String codeString) {
        synchronized (lock) {
            QRCodeSendCode++;
            this.sendCode = QRCodeSendCode;
            this.contents = QRCodeUtil.getQrCodeStringList(QRCodeUtil.encodeQrCodeString(codeString));
            this.currentPage = 0;

        }

    }

    public int getSendCode() {
        return this.sendCode;
    }

    public boolean sendFinish() {
        return currentPage == this.contents.size() - 1;
    }

    public boolean canNextPage() {
        String[] headers = new String[]{Integer.toString(sendCode),
                Integer.toString(contents.size() - 1), Integer.toString(currentPage)};
        String sendHeader = Utils.joinString(headers, QRCodeUtil.QR_CODE_SPLIT);
        return Utils.compareString(sendHeader, receiveMsg);
    }


    public void nextPage() {
        currentPage++;
    }

    public String getShowMessage() {
        String msg = "";
        String[] headers = new String[]{Integer.toString(sendCode),
                Integer.toString(contents.size() - 1), Integer.toString(currentPage)};
        String sendHeader = Utils.joinString(headers, QRCodeUtil.QR_CODE_SPLIT);
        if (this.contents.size() == 1) {
            msg = sendHeader + QRCodeUtil.QR_CODE_SPLIT + this.contents.get(0);
        } else {
            if (currentPage < this.contents.size()) {
                msg = Integer.toString(sendCode) + QRCodeUtil.QR_CODE_SPLIT + this.contents.get(currentPage);
            }

        }
        return msg;
    }

    public void setReceiveMsg(String msg) {

        String[] strings = QRCodeUtil.splitString(msg);
        if (Utils.isInteger(strings[0])) {
            int sendCode = Integer.valueOf(strings[0]);
            if (sendCode > QRCodeSendCode) {
                QRCodeSendCode = sendCode;
            }
        }
        this.receiveMsg = msg;
    }


    public static int getSendCodeFromMsg(String msg) {
        String[] strings = QRCodeUtil.splitString(msg);
        int sendCode = Integer.valueOf(strings[0]);
        return sendCode;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DesktopQRCodSend) {
            DesktopQRCodSend other = (DesktopQRCodSend) obj;
            return sendCode == other.sendCode;

        }
        return false;
    }
}
