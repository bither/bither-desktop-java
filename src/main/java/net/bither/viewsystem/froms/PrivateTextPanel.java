package net.bither.viewsystem.froms;

import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.utils.WalletUtils;
import net.bither.viewsystem.base.Panels;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class PrivateTextPanel extends WizardPanel {
    private JTextArea taPrivateText;
    private SecureCharSequence secureCharSequence;

    public PrivateTextPanel(SecureCharSequence secureCharSequence) {
        super(MessageKey.PRIVATE_KEY_TEXT, AwesomeIcon.FA_FILE_TEXT);
        this.secureCharSequence = WalletUtils.formatHashFromCharSequence(secureCharSequence, 4, 16);
        secureCharSequence.wipe();

    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][][]", // Column constraints
                "[]20[][][][][]80[]40[][]" // Row constraints
        ));

        taPrivateText = new JTextArea();
        taPrivateText.setBorder(null);
        taPrivateText.setEditable(false);
        taPrivateText.setFont(new Font("Monospaced", taPrivateText.getFont().getStyle(), taPrivateText.getFont().getSize()));
        taPrivateText.setText(this.secureCharSequence.toString());
        taPrivateText.setBackground(panel.getBackground());

        panel.add(taPrivateText, "align center,cell 2 2 ,grow");


    }
}
