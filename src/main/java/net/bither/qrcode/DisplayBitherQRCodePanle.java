package net.bither.qrcode;

import net.bither.Bither;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.froms.WizardPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class DisplayBitherQRCodePanle extends WizardPanel {

    private java.util.List<String> contents;

    private JButton previousPageButton;
    private JLabel iconLabel;
    private JButton nextPageButton;
    private JLabel labPage;
    private int index = 0;

    public DisplayBitherQRCodePanle(String codeString) {
        this(codeString, false);

    }

    public DisplayBitherQRCodePanle(String codeString, boolean isPopover) {
        super(MessageKey.QR_CODE, AwesomeIcon.QRCODE,isPopover);
        this.contents = QRCodeUtil.getQrCodeStringList(QRCodeUtil.encodeQrCodeString(codeString));

    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "20[][][]20", // Column constraints
                "25[][]" // Row constraints
        ));

        iconLabel = Labels.newValueLabel("");
        iconLabel.setOpaque(true);
        panel.add(iconLabel, "align center,cell 1 0,wrap");
        previousPageButton = Buttons.newPreviousButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                index--;
                showQRCode();
            }
        });
        nextPageButton = Buttons.newNextButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                index++;
                showQRCode();
            }
        });
        labPage = Labels.newLabChangeNote();
        panel.add(previousPageButton, "align left,cell 0 1");
        panel.add(labPage, "align center,cell 1 1");
        panel.add(nextPageButton, "align right,cell 2 1");
        showQRCode();


    }

    private void showQRCode() {
        if (index == this.contents.size() - 1) {
            nextPageButton.setEnabled(false);
        } else {
            nextPageButton.setEnabled(true);
        }
        if (index == 0) {
            previousPageButton.setEnabled(false);
        } else {
            previousPageButton.setEnabled(true);
        }
        labPage.setText(Utils.format(LocaliserUtils.getString("qr.code.page"), index + 1, contents.size()));
        String text = contents.get(index);
        Dimension mainFrameSize = Bither.getMainFrame().getSize();
        int scaleWidth = (int) (mainFrameSize.getWidth() / 2);
        int scaleHeight = (int) (mainFrameSize.getHeight() / 2);
        Image image = QRCodeGenerator.generateQRcode(text, null, null, 1);
        if (image != null) {
            int scaleFactor = (int) (Math.floor(Math.min(scaleHeight / image.getHeight(null),
                    scaleWidth / image.getWidth(null))));
            image = QRCodeGenerator.generateQRcode(text, null, null, scaleFactor);
        }
        iconLabel.setIcon(new ImageIcon(image));

    }
}
