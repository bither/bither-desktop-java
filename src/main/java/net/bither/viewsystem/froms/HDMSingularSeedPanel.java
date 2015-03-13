package net.bither.viewsystem.froms;

import net.bither.Bither;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.qrcode.QRCodeGenerator;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Created by nn on 15/3/11.
 */
public class HDMSingularSeedPanel extends WizardPanel {

    private JTextArea taPrivateText;
    private String worldString;

    private String qrcodeString;

    public HDMSingularSeedPanel(List<String> worldList, String qrcodeString) {
        super(MessageKey.HDM_COLD_SEED_WORD_LIST, AwesomeIcon.BITBUCKET, true);
        worldString = "";
        this.qrcodeString = qrcodeString;
        for (int i = 0; i < worldList.size(); i++) {
            if (i == worldList.size() - 1) {
                worldString += worldList.get(i);
            } else if ((i + 1) % 3 == 0) {
                worldString += worldList.get(i) + "-" + "\n";

            } else {
                worldString += worldList.get(i) + "-";
            }
        }


    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[]", // Column constraints
                "[][]" // Row constraints
        ));

        BufferedImage qrCodeImage = null;
        Dimension mainFrameSize = Bither.getMainFrame().getSize();
        int scaleWidth = (int) (mainFrameSize.getWidth() / 2);
        int scaleHeight = (int) (mainFrameSize.getHeight() / 2);
        Image image = QRCodeGenerator.generateQRcode(qrcodeString, null, null, 1);
        if (image != null) {
            int scaleFactor = (int) (Math.floor(Math.min(scaleHeight / image.getHeight(null),
                    scaleWidth / image.getWidth(null))));
            qrCodeImage = QRCodeGenerator.generateQRcode(qrcodeString, null, null, scaleFactor);
        }

        JLabel imageLabel = Labels.newImageLabel(qrCodeImage);
        panel.add(imageLabel, "shrink,");

        taPrivateText = new JTextArea();
        taPrivateText.setBorder(null);
        taPrivateText.setEditable(false);
        taPrivateText.setText(worldString);
        taPrivateText.setBackground(panel.getBackground());
        taPrivateText.setFont(taPrivateText.getFont().deriveFont(20));
        panel.add(taPrivateText, "shrink ,wrap,span");
        panel.add(Labels.newNoteLabel(new String[]{LocaliserUtils.getString("hdm_singular_cold_seed_remember_warn")}), "grow");

    }
}
