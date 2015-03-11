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

    private String qrCodeString;

    public DisplayQRCodePanle(String qrCodestring) {
        this(qrCodestring, true);
    }

    public DisplayQRCodePanle(String qrCodeString, boolean isPopover) {
        super(MessageKey.QR_CODE, AwesomeIcon.QRCODE, isPopover);
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
        Dimension mainFrameSize = Bither.getMainFrame().getSize();
        int scaleWidth = (int) (mainFrameSize.getWidth() / 2);
        int scaleHeight = (int) (mainFrameSize.getHeight() / 2);
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
