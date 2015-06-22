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
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class DesktopQRCodReceive {
    private int sendCode;
    private int sumPage;
    private int currentPage;

    private List<QRCodeTransportPage> qrCodeTransportPageList = new ArrayList<QRCodeTransportPage>();


    public String getShowMsg() {
        String[] headers = new String[]{Integer.toString(sendCode),
                Integer.toString(sumPage), Integer.toString(currentPage)};
        String sendHeader = Utils.joinString(headers, QRCodeUtil.QR_CODE_SPLIT);
        return sendHeader;
    }

    public boolean receiveComplete() {
        return currentPage == sumPage;
    }

    public String getReceiveResult() {
        if (currentPage == sumPage) {
            return QRCodeTransportPage.qrCodeTransportToString(qrCodeTransportPageList);
        } else {
            return null;
        }
    }

    public void receiveMsg(String msg) {
        if (QRCodeUtil.verifyBitherQRCode(msg)) {
            String[] strings = QRCodeUtil.splitString(msg);
            sendCode = Integer.valueOf(strings[0]);
            sumPage = Integer.valueOf(strings[1]);
            currentPage = Integer.valueOf(strings[2]);
            String qrCodeTransport = msg.substring(strings[0].length() +strings[1].length()+strings[2].length()+ 3);
            qrCodeTransportPageList.add(QRCodeTransportPage.formatQrCodeTransport(qrCodeTransport));

        }

    }


}
