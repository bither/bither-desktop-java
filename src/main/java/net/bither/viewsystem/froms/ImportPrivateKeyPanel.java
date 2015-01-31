package net.bither.viewsystem.froms;

import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.crypto.ECKey;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.crypto.bip38.Bip38;
import net.bither.bitherj.exception.AddressFormatException;
import net.bither.bitherj.factory.ImportPrivateKey;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.PrivateKeyUtil;
import net.bither.factory.ImportPrivateKeyDesktop;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.qrcode.IReadQRCode;
import net.bither.qrcode.IScanQRCode;
import net.bither.qrcode.SelectQRCodePanel;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.ImportBIP38PrivateTextDialog;
import net.bither.viewsystem.dialogs.ImportPrivateTextDialog;
import net.bither.viewsystem.dialogs.PasswordDialog;
import net.bither.viewsystem.listener.ICheckPasswordListener;
import net.bither.viewsystem.listener.IDialogPasswordListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ImportPrivateKeyPanel extends WizardPanel {

    private JButton btnQRCode;
    private JButton btnBIP38QRCode;

    private JButton btnPrivateKey;
    private JButton btnBIP38;

    private JButton btnHDMColdSeed;
    private JButton btnHDMCOLDPhrase;
    private String bip38DecodeString;

    public ImportPrivateKeyPanel() {
        super(MessageKey.IMPORT, AwesomeIcon.CLOUD_DOWNLOAD, false);
    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][][]", // Column constraints
                "[][][][][][][][]80[]20[][]" // Row constraints
        ));
        btnQRCode = Buttons.newQRCodeButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
                onQRCode();
            }
        }, MessageKey.IMPORT_PRIVATE_KEY_QRCODE);

        btnPrivateKey = Buttons.newFileTextButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
                importPrivateText();

            }
        }, MessageKey.IMPORT_PRIVATE_KEY_TEXT);
        btnBIP38QRCode = Buttons.newQRCodeButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
                onBIP38QRCode();
            }
        }, MessageKey.IMPORT_BIP38_PRIVATE_KEY_QRCODE);
        btnBIP38 = Buttons.newFileTextButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
                ImportBIP38PrivateTextDialog importBIP38PrivateTextDialog = new ImportBIP38PrivateTextDialog();
                importBIP38PrivateTextDialog.pack();
                importBIP38PrivateTextDialog.setVisible(true);

            }
        }, MessageKey.IMPORT_BIP38_PRIVATE_KEY_TEXT);
        btnHDMColdSeed = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        }, MessageKey.HDM, AwesomeIcon.HDD_O);
        btnHDMCOLDPhrase = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        }, MessageKey.HDM, AwesomeIcon.HDD_O);


        panel.add(btnQRCode, "align center,cell 2 2 ,grow,wrap");
        panel.add(btnPrivateKey, "align center,cell 2 3,grow,wrap");
        panel.add(btnBIP38QRCode, "align center,cell 2 4,grow,wrap");
        panel.add(btnBIP38, "align center,cell 2 5,grow,wrap");
        if (!AddressManager.getInstance().hasHDMKeychain()) {
            panel.add(btnHDMColdSeed, "align center,cell 2 6,grow,wrap");
            panel.add(btnHDMCOLDPhrase, "align center,cell 2 7,grow,wrap");
        }
    }

    private void onQRCode() {
        SelectQRCodePanel qrCodePanel = new SelectQRCodePanel(new IScanQRCode() {
            public void handleResult(final String result, IReadQRCode readQRCode) {
                if (QRCodeUtil.verifyBitherQRCode(result)) {
                    readQRCode.close();
                    PasswordDialog passwordDialog = new PasswordDialog(new ImportPrivateKeyPasswordListenerI(result, false));

                    passwordDialog.setCheckPre(false);

                    passwordDialog.setCheckPasswordListener(new ICheckPasswordListener() {
                        @Override
                        public boolean checkPassword(SecureCharSequence password) {
                            ECKey ecKey = PrivateKeyUtil.getECKeyFromSingleString(result, password);
                            boolean result = ecKey != null;
                            return result;
                        }
                    });
                    passwordDialog.pack();
                    passwordDialog.setVisible(true);

                } else {
                    readQRCode.reTry("");
                }

            }
        });
        qrCodePanel.showPanel();

    }

    private class ImportPrivateKeyPasswordListenerI implements IDialogPasswordListener {
        private String content;
        private boolean isFromBip38;

        public ImportPrivateKeyPasswordListenerI(String content, boolean isFromBip38) {
            this.content = content;
            this.isFromBip38 = isFromBip38;
        }

        @Override
        public void onPasswordEntered(SecureCharSequence password) {

            if (isFromBip38) {
                PasswordDialog dialogPassword = new PasswordDialog(walletIDialogPasswordListener);
                dialogPassword.pack();
                dialogPassword.setVisible(true);

            } else {

                ImportPrivateKeyDesktop importPrivateKey = new ImportPrivateKeyDesktop(
                        ImportPrivateKey.ImportPrivateKeyType.BitherQrcode, content, password);
                importPrivateKey.importPrivateKey();

            }

        }
    }

    private IDialogPasswordListener walletIDialogPasswordListener = new IDialogPasswordListener() {
        @Override
        public void onPasswordEntered(SecureCharSequence password) {
            ImportPrivateKeyDesktop importPrivateKey = new ImportPrivateKeyDesktop(
                    ImportPrivateKey.ImportPrivateKeyType.Bip38, bip38DecodeString, password);
            importPrivateKey.importPrivateKey();
        }
    };


    private void onBIP38QRCode() {
        SelectQRCodePanel qrCodePanel = new SelectQRCodePanel(new IScanQRCode() {
            public void handleResult(final String result, IReadQRCode readQRCode) {
                boolean isBIP38Key = false;
                try {
                    isBIP38Key = Bip38.isBip38PrivateKey(result);
                } catch (AddressFormatException e) {
                    e.printStackTrace();
                }
                if (isBIP38Key) {
                    readQRCode.close();
                    PasswordDialog passwordDialog = new PasswordDialog(new ImportPrivateKeyPasswordListenerI(result, true));

                    passwordDialog.setCheckPre(false);

                    passwordDialog.setCheckPasswordListener(new ICheckPasswordListener() {
                        @Override
                        public boolean checkPassword(SecureCharSequence password) {
                            try {
                                bip38DecodeString = Bip38.decrypt(result, password).toString();
                                return bip38DecodeString != null;
                            } catch (Exception e) {
                                e.printStackTrace();
                                return false;
                            }
                        }
                    });
                    passwordDialog.pack();
                    passwordDialog.setVisible(true);

                } else {
                    readQRCode.reTry("");
                }

            }
        });
        qrCodePanel.showPanel();


    }


    private void importPrivateText() {
        ImportPrivateTextDialog importPrivateTextDialog = new ImportPrivateTextDialog();
        importPrivateTextDialog.pack();
        importPrivateTextDialog.setVisible(true);


    }


}
