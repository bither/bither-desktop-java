package net.bither.viewsystem.froms;

import net.bither.Bither;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.HDMKeychain;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.PrivateKeyUtil;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.qrcode.DisplayBitherQRCodePanel;
import net.bither.qrcode.DisplayQRCodePanle;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.DialogPassword;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.listener.IDialogPasswordListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class ExportPrivateKeyPanel extends WizardPanel implements IDialogPasswordListener {

    private JButton btnEncryptQRCode;
    private JButton btnPrivateText;
    private JButton btnPrivateKeyQRCode;
    private JButton btnColdSeed;
    private JButton btnPhras;
    private HDMKeychain keychain;

    private int btnCurrent = 0;

    public ExportPrivateKeyPanel() {
        super(MessageKey.EXPORT, AwesomeIcon.FA_SIGN_OUT, false);
        keychain = AddressManager.getInstance().getHdmKeychain();
    }

    @Override
    public void initialiseContent(JPanel panel) {

        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][][][][]", // Column constraints
                "[]20[][][][][]80[]40[][]" // Row constraints
        ));
        btnEncryptQRCode = Buttons.newQRCodeButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (AddressManager.getInstance().getPrivKeyAddresses().size() > 0) {
                    btnCurrent = 0;
                    callPasswordDialog();
                } else {
                    new MessageDialog(LocaliserUtils.getString("private_key_is_empty")).showMsg();
                }

            }
        }, MessageKey.PRIVATE_KEY_QRCODE_ENCRYPTED);
        btnPrivateKeyQRCode = Buttons.newQRCodeButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (AddressManager.getInstance().getPrivKeyAddresses().size() > 0) {
                    btnCurrent = 2;
                    callPasswordDialog();
                } else {
                    new MessageDialog(LocaliserUtils.getString("private_key_is_empty")).showMsg();
                }

            }
        }, MessageKey.PRIVATE_KEY_QRCODE_DECRYPTED);

        btnPrivateText = Buttons.newFileTextButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (AddressManager.getInstance().getPrivKeyAddresses().size() > 0) {
                    btnCurrent = 1;
                    callPasswordDialog();
                } else {
                    new MessageDialog(LocaliserUtils.getString("private_key_is_empty")).showMsg();
                }

            }
        }, MessageKey.PRIVATE_KEY_TEXT);


        btnColdSeed = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DialogPassword dialogPassword = new DialogPassword(new IDialogPasswordListener() {
                    @Override
                    public void onPasswordEntered(SecureCharSequence password) {
                        showHDMSeedQRCode(password);
                    }
                });
                dialogPassword.pack();
                dialogPassword.setVisible(true);

            }
        }, MessageKey.HDM_COLD_SEED_QR_CODE, AwesomeIcon.QRCODE);
        btnPhras = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DialogPassword dialogPassword = new DialogPassword(new IDialogPasswordListener() {
                    @Override
                    public void onPasswordEntered(SecureCharSequence password) {
                        showHDMSeedPhras(password);
                    }
                });
                dialogPassword.pack();
                dialogPassword.setVisible(true);

            }
        }, MessageKey.HDM_COLD_SEED_WORD_LIST, AwesomeIcon.BITBUCKET);

        if (Bither.getActionAddress() != null) {
            panel.add(btnEncryptQRCode, "align center,cell 3 2 ,grow,wrap");
            panel.add(btnPrivateKeyQRCode, "align center,cell 3 3,grow,wrap");
            panel.add(btnPrivateText, "align center,cell 3 4,grow,wrap");
        } else {
            panel.add(btnColdSeed, "align center,cell 3 2,grow,wrap");
            panel.add(btnPhras, "align center,cell 3 3,grow,wrap");

        }
    }


    private void callPasswordDialog() {
        DialogPassword dialogPassword = new DialogPassword(this);
        dialogPassword.pack();
        dialogPassword.setVisible(true);

    }

    @Override
    public void onPasswordEntered(SecureCharSequence password) {
        switch (btnCurrent) {
            case 0:
                showEncryptQRCode(Bither.getActionAddress().getFullEncryptPrivKey().toUpperCase());
                break;
            case 1:
                showPrivateText(password);
                break;
            case 2:
                showPrivateKeyQRCode(password);
                break;
        }

    }

    private void showEncryptQRCode(String text) {
        DisplayBitherQRCodePanel qrCodeDialog = new DisplayBitherQRCodePanel(text, true);
        qrCodeDialog.showPanel();


    }

    private void showPrivateKeyQRCode(SecureCharSequence password) {
        final SecureCharSequence str = PrivateKeyUtil.getDecryptPrivateKeyString(Bither.getActionAddress().getFullEncryptPrivKey(), password);
        password.wipe();
        DisplayQRCodePanle displayQRCodePanle = new DisplayQRCodePanle(str.toString());
        displayQRCodePanle.showPanel();


    }

    private void showPrivateText(SecureCharSequence password) {

        final SecureCharSequence str = PrivateKeyUtil.getDecryptPrivateKeyString(Bither.getActionAddress().getFullEncryptPrivKey(), password);
        password.wipe();
        PrivateTextPanel privateTextPanel = new PrivateTextPanel(str);
        privateTextPanel.showPanel();

    }

    private void showHDMSeedPhras(final SecureCharSequence password) {

        new Thread() {
            @Override
            public void run() {
                final List<String> words = new ArrayList<String>();
                try {
                    words.addAll(keychain.getSeedWords(password));
                } catch (Exception e) {
                    e.printStackTrace();

                }
                if (words.size() > 0) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            HDMSeedPhrasPanel hdmSeedPhrasPanel = new HDMSeedPhrasPanel(words);
                            hdmSeedPhrasPanel.showPanel();

                        }
                    });
                }
            }
        }.start();
    }

    private void showHDMSeedQRCode(SecureCharSequence password) {
        password.wipe();
        String content = QRCodeUtil.HDM_QR_CODE_FLAG + keychain.getFullEncryptPrivKey();
        DisplayQRCodePanle displayQRCodePanle = new DisplayQRCodePanle(content);
        displayQRCodePanle.showPanel();
        displayQRCodePanle.updateTitle(LocaliserUtils.getString("hdm_cold_seed_qr_code"));
    }

}
