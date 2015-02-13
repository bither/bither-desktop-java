package net.bither.viewsystem.froms;

import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.HDMKeychain;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.utils.KeyUtil;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.DialogPassword;
import net.bither.xrandom.HDMKeychainHotUEntropyDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.security.SecureRandom;

public class HDMHotPanel extends WizardPanel implements DialogPassword.PasswordGetter.PasswordGetterDelegate {
    private JButton btnHot;
    private JButton btnCold;
    private JButton btnService;
    private DialogPassword.PasswordGetter passwordGetter;


    public HDMHotPanel() {
        super(MessageKey.HDM, AwesomeIcon.FA_RECYCLE, false);
        passwordGetter = new DialogPassword.PasswordGetter(HDMHotPanel.this);
    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][][][][]", // Column constraints
                "[]10[][][][]" // Row constraints

        ));
        btnHot = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addHot();
            }
        }, MessageKey.HDM, AwesomeIcon.FA_RECYCLE);
        btnCold = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCold();

            }
        }, MessageKey.HDM, AwesomeIcon.FA_RECYCLE);
        btnService = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addService();
            }
        }, MessageKey.HDM, AwesomeIcon.FA_RECYCLE);

        panel.add(btnHot, "align center,cell 3 2 ,grow,wrap");
        panel.add(btnCold, "align center,cell 3 3 ,grow,wrap");
        panel.add(btnService, "align center,cell 3 4 ,grow,wrap");
        passwordGetter = new DialogPassword.PasswordGetter();


    }

    private void addHot() {
        HdmKeychainAddHotPanel hdmKeychainAddHotPanel = new HdmKeychainAddHotPanel(new HdmKeychainAddHotPanel.DialogHdmKeychainAddHotDelegate() {
            @Override
            public void addWithXRandom() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SecureCharSequence password = passwordGetter.getPassword();
                        if (password == null) {
                            return;
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                HDMKeychainHotUEntropyDialog hdmKeychainHotUEntropyDialog = new HDMKeychainHotUEntropyDialog(passwordGetter);
                                hdmKeychainHotUEntropyDialog.pack();
                                hdmKeychainHotUEntropyDialog.setVisible(true);
                                if (AddressManager.getInstance().getHdmKeychain() != null) {
                                    finishHot();
                                }

                            }
                        });

                    }
                }).start();

            }

            @Override
            public void addWithoutXRandom() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SecureCharSequence password = passwordGetter.getPassword();
                        if (password == null) {
                            return;
                        }
                        HDMKeychain keychain = new HDMKeychain(new SecureRandom(), password);
                        KeyUtil.setHDKeyChain(keychain);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (AddressManager.getInstance().getHdmKeychain() != null) {
                                    finishHot();
                                }
                            }
                        });

                    }
                }).start();


            }
        });
        hdmKeychainAddHotPanel.showPanel();

    }

    private void addCold() {

    }

    private void addService() {

    }

    private void finishHot() {

    }

    private void finishCold() {

    }

    private void finishService() {

    }

    @Override
    public void beforePasswordDialogShow() {

    }

    @Override
    public void afterPasswordDialogDismiss() {

    }
}
