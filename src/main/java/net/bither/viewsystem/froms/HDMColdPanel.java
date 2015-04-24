package net.bither.viewsystem.froms;

import net.bither.Bither;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.HDMKeychain;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.delegate.IPasswordGetterDelegate;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.utils.KeyUtil;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.base.RadioButtons;
import net.bither.xrandom.HDMKeychainColdUEntropyDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.SecureRandom;

public class HDMColdPanel extends WizardPanel implements IPasswordGetterDelegate {
    private JCheckBox xrandomCheckBox;
    private PasswordPanel.PasswordGetter passwordGetter;

    public HDMColdPanel() {
        super(MessageKey.HDM, AwesomeIcon.FA_RECYCLE);
        passwordGetter = new PasswordPanel.PasswordGetter(HDMColdPanel.this);
        setOkAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final SecureCharSequence password = passwordGetter.getPassword();
                        if (password == null) {
                            return;
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                closePanel();
                                if (xrandomCheckBox.isSelected()) {
                                    HDMKeychainColdUEntropyDialog hdmKeychainColdUEntropyDialog = new HDMKeychainColdUEntropyDialog(passwordGetter);
                                    hdmKeychainColdUEntropyDialog.pack();
                                    hdmKeychainColdUEntropyDialog.setVisible(true);
                                } else {
                                    HDMKeychain chain = new HDMKeychain(new SecureRandom(), password);
                                    KeyUtil.setHDKeyChain(chain);
                                    password.wipe();
                                    Bither.refreshFrame();


                                }
                            }
                        });

                    }
                }).start();

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


        panel.add(Labels.newNoteLabel(new String[]{LocaliserUtils.getString("hdm_seed_generation_notice")}), "push,align center,wrap");
        xrandomCheckBox = new JCheckBox();
        xrandomCheckBox.setSelected(true);
        xrandomCheckBox.setText(LocaliserUtils.getString("xrandom"));
        panel.add(xrandomCheckBox, "push,align center,wrap");
    }

    @Override
    public void beforePasswordDialogShow() {

    }

    @Override
    public void afterPasswordDialogDismiss() {

    }


}