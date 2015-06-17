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

import net.bither.bitherj.core.Tx;
import net.bither.bitherj.utils.Utils;

public class DesktopQRCodSend {

    public static int QRCodeSendCode = 1;
    private Tx tx;
    private String message;
    private int sendCode;
    private int sumPage;
    private int currentPage;
    private String receiveMsg;


    public boolean sendComplete() {
        return false;
    }

    public boolean signComplete() {
        return false;
    }

    public String getShowMessage() {
        return "";
    }

    public void setReceiveMsg(String msg) {
        this.receiveMsg = msg;
    }

    public boolean canNextPage() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DesktopQRCodSend) {
            DesktopQRCodSend other = (DesktopQRCodSend) obj;
            return sendCode == other.sendCode && Utils.compareString(message, other.message);

        }
        return false;
    }
}
