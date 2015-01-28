package net.bither.viewsystem.froms;

import net.bither.Bither;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.utils.PrivateKeyUtil;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.qrcode.DisplayBitherQRCodePanel;
import net.bither.qrcode.DisplayQRCodePanle;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.dialogs.PasswordDialog;
import net.bither.viewsystem.dialogs.PrivateTextDialog;
import net.bither.viewsystem.listener.IDialogPasswordListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExportPrivateKeyPanel extends WizardPanel implements IDialogPasswordListener {

    private JButton btnEncryptQRCode;
    private JButton btnPrivateText;
    private JButton btnPrivateKeyQRCode;
    private int btnCurrent = 0;

    public ExportPrivateKeyPanel() {
        super(MessageKey.EXPORT, AwesomeIcon.CLOUD_UPLOAD, false);
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
                    new MessageDialog(LocaliserUtils.getString("private.key.is.empty")).showMsg();
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
                    new MessageDialog(LocaliserUtils.getString("private.key.is.empty")).showMsg();
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
                    new MessageDialog(LocaliserUtils.getString("private.key.is.empty")).showMsg();
                }

            }
        }, MessageKey.PRIVATE_KEY_TEXT);

        panel.add(btnEncryptQRCode, "align center,cell 3 2 ,grow,wrap");
        panel.add(btnPrivateKeyQRCode, "align center,cell 3 3,grow,wrap");
        panel.add(btnPrivateText, "align center,cell 3 4,grow,wrap");


    }


    private void callPasswordDialog() {
        PasswordDialog passwordDialog = new PasswordDialog(this);
        passwordDialog.pack();
        passwordDialog.setVisible(true);

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
        onCancel();
        DisplayBitherQRCodePanel qrCodeDialog = new DisplayBitherQRCodePanel(text);
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
        PrivateTextDialog privateTextDialog = new PrivateTextDialog(str);
        privateTextDialog.pack();
        privateTextDialog.setVisible(true);

    }
}
