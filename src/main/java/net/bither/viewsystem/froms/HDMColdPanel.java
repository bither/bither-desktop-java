package net.bither.viewsystem.froms;

import net.bither.Bither;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.HDMKeychain;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.utils.KeyUtil;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.base.RadioButtons;
import net.bither.viewsystem.dialogs.PasswordDialog;
import net.bither.viewsystem.listener.IDialogPasswordListener;
import net.bither.xrandom.HDMKeychainColdUEntropyDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.SecureRandom;

public class HDMColdPanel extends WizardPanel implements IDialogPasswordListener {
    private JRadioButton radioButton;

    public HDMColdPanel() {
        super(MessageKey.HDM, AwesomeIcon.FA_RECYCLE, false);
        setOkAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PasswordDialog passwordDialog = new PasswordDialog(HDMColdPanel.this);
                passwordDialog.pack();
                passwordDialog.setVisible(true);
            }
        });

        setOkEnabled(AddressManager.getInstance().getHdmKeychain() == null);
    }

    @Override
    public void initialiseContent(final JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][][][][]", // Column constraints
                "[]10[][][][][]80[]20[][][]" // Row constraints
        ));

        JTextArea textArea = new JTextArea();
        textArea.setBorder(null);
        textArea.setEditable(false);
        textArea.setText(LocaliserUtils.getString("hdm_seed_generation_notice"));
        textArea.setBackground(panel.getBackground());
        textArea.setFont(textArea.getFont().deriveFont(20));
        panel.add(textArea, "push,align center,wrap");
        radioButton = RadioButtons.newRadioButton(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        }, MessageKey.xrandom, null);
        radioButton.setFocusPainted(false);
        radioButton.setSelected(true);
        panel.add(radioButton, "push,align center,wrap");

    }

    @Override
    public void onPasswordEntered(SecureCharSequence password) {
        onCancel();
        if (radioButton.isSelected()) {
            HDMKeychainColdUEntropyDialog hdmKeychainColdUEntropyDialog = new HDMKeychainColdUEntropyDialog(password);
            hdmKeychainColdUEntropyDialog.pack();
            hdmKeychainColdUEntropyDialog.setVisible(true);

        } else {
            HDMKeychain chain = new HDMKeychain(new SecureRandom(), password);
            KeyUtil.setHDKeyChain(chain);

            password.wipe();
            Bither.refreshFrame();


        }

    }
}