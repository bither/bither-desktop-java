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

import net.bither.qrcode.DesktopQRCodReceive;
import net.bither.qrcode.DesktopQRCodSend;
import net.bither.viewsystem.dialogs.AbstractDesktopHDMMsgDialog;

public class DesktopHDMColdMsgPanel extends AbstractDesktopHDMMsgDialog {

    public DesktopHDMColdMsgPanel() {
        isSendMode = false;
    }

    @Override
    protected void handleScanResult(String result) {
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
        return "";
    }
}
