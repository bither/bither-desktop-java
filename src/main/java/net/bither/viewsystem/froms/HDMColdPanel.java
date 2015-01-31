package net.bither.viewsystem.froms;

import net.bither.Bither;
import net.bither.BitherSetting;
import net.bither.bitherj.core.HDMKeychain;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.utils.KeyUtil;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.base.RadioButtons;
import net.bither.viewsystem.dialogs.PasswordDialog;
import net.bither.viewsystem.listener.IDialogPasswordListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.SecureRandom;

public class HDMColdPanel extends WizardPanel implements IDialogPasswordListener {
    private JRadioButton radioButton;

    public HDMColdPanel() {
        super(MessageKey.HDM, AwesomeIcon.FA_RECYCLE, false);
    }

    @Override
    public void initialiseContent(final JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][][][][]", // Column constraints
                "[]10[][][][][]80[]20[][][]" // Row constraints
        ));

        panel.add(Labels.newValueLabel(LocaliserUtils.getString("version") + ": " + BitherSetting.VERSION), "push,align center,wrap");
        radioButton = RadioButtons.newRadioButton(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {



            }
        }, MessageKey.CHECK_PRIVATE_KEY, null);
        panel.add(radioButton, "push,align center,wrap");
        JButton button = Buttons.newButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PasswordDialog passwordDialog = new PasswordDialog(HDMColdPanel.this);
                passwordDialog.pack();
                passwordDialog.setVisible(true);
            }
        }, MessageKey.CHECK_PRIVATE_KEY);
        panel.add(button, "push,align center,wrap");

    }

    @Override
    public void onPasswordEntered(SecureCharSequence password) {
        if (radioButton.isSelected()) {

        } else {
            HDMKeychain chain = new HDMKeychain(new SecureRandom(), password);
            KeyUtil.setHDKeyChain(chain);
            password.wipe();


        }

    }
}