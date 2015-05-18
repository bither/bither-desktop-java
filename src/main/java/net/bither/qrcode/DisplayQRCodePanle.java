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

import net.bither.BitherUI;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.froms.WizardPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class DisplayQRCodePanle extends WizardPanel {

    private String qrCodeString;


    public DisplayQRCodePanle(String qrCodeString) {
        super(MessageKey.QR_CODE, AwesomeIcon.QRCODE);
        this.qrCodeString = qrCodeString;
    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[]", // Column constraints
                "10[][][][]" // Row constraints
        ));
        BufferedImage qrCodeImage = null;
        panel.getMaximumSize();
        int scaleWidth = BitherUI.POPOVER_MIN_WIDTH;
        int scaleHeight = BitherUI.POPOVER_MIN_WIDTH;
        Image image = QRCodeGenerator.generateQRcode(qrCodeString, null, null, 1);
        if (image != null) {
            int scaleFactor = (int) (Math.floor(Math.min(scaleHeight / image.getHeight(null),
                    scaleWidth / image.getWidth(null))));
            qrCodeImage = QRCodeGenerator.generateQRcode(qrCodeString, null, null, scaleFactor);
        }

        JLabel imageLabel = Labels.newImageLabel(qrCodeImage);
        panel.add(imageLabel, "align center,push,wrap");


    }


}
