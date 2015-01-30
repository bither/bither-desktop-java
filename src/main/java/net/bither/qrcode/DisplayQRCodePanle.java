package net.bither.qrcode;

import net.bither.Bither;
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


    private BufferedImage qrCodeImage;
    private String codeString;

    public DisplayQRCodePanle(String codeString) {
        super(MessageKey.QR_CODE, AwesomeIcon.QRCODE,true);
        this.codeString = codeString;
    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[]", // Column constraints
                "10[][][][]" // Row constraints
        ));

        Dimension mainFrameSize = Bither.getMainFrame().getSize();
        int scaleWidth = (int) (mainFrameSize.getWidth() / 2);
        int scaleHeight = (int) (mainFrameSize.getHeight() / 2);
        Image image = QRCodeGenerator.generateQRcode(codeString, null, null, 1);
        if (image != null) {
            int scaleFactor = (int) (Math.floor(Math.min(scaleHeight / image.getHeight(null),
                    scaleWidth / image.getWidth(null))));
            qrCodeImage = QRCodeGenerator.generateQRcode(codeString, null, null, scaleFactor);
        }

        JLabel imageLabel = Labels.newImageLabel(qrCodeImage);
        panel.add(imageLabel, "align center,push,wrap");


    }


}
