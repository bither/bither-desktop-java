package net.bither.viewsystem.froms;

import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.HDMKeychain;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.qrcode.DisplayQRCodePanle;
import net.bither.qrcode.IReadQRCode;
import net.bither.qrcode.IScanQRCode;
import net.bither.qrcode.SelectTransportQRCodePanel;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.dialogs.PasswordDialog;
import net.bither.viewsystem.listener.IDialogPasswordListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class HDMColdDetailPanel extends WizardPanel {

    private JButton btnColdQRCode;
    private JButton btnScanServiceQRCode;
    private JButton btnColdSeed;
    private JButton btnPhras;
    private HDMKeychain keychain;

    public HDMColdDetailPanel() {
        super(MessageKey.HDM, AwesomeIcon.FA_RECYCLE, false);
        keychain = AddressManager.getInstance().getHdmKeychain();
    }

    @Override
    public void initialiseContent(final JPanel panel) {

        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][][][][]", // Column constraints
                "[]20[][][][][]80[]40[][]" // Row constraints
        ));
        btnColdQRCode = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PasswordDialog passwordDialog = new PasswordDialog(new IDialogPasswordListener() {
                    @Override
                    public void onPasswordEntered(SecureCharSequence password) {
                        showPublicKeyQrCode(password);
                    }
                });
                passwordDialog.pack();
                passwordDialog.setVisible(true);

            }
        }, MessageKey.HDM_COLD_PUB_KEY_QR_CODE, AwesomeIcon.QRCODE);
        btnScanServiceQRCode = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                SelectTransportQRCodePanel selectQRCodeDialog = new SelectTransportQRCodePanel(new IScanQRCode() {
                    public void handleResult(final String result, IReadQRCode readQRCode) {
                        readQRCode.close();

                        if (Utils.isEmpty(result)) {
                            new MessageDialog(LocaliserUtils.getString("scan_for_all_addresses_in_bither_cold_failed")).showMsg();

                        } else {
                            PasswordDialog passwordDialog = new PasswordDialog(new IDialogPasswordListener() {
                                @Override
                                public void onPasswordEntered(SecureCharSequence password) {
                                    signMessageOfHDMKeychain(result, password);
                                }
                            });
                            passwordDialog.pack();
                            passwordDialog.setVisible(true);
                        }
                    }
                }, true);
                selectQRCodeDialog.showPanel();

            }
        }, MessageKey.HDM_SERVER_QR_CODE, AwesomeIcon.CAMERA);
        btnColdSeed = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PasswordDialog passwordDialog = new PasswordDialog(new IDialogPasswordListener() {
                    @Override
                    public void onPasswordEntered(SecureCharSequence password) {
                        showHDMSeedQRCode(password);
                    }
                });
                passwordDialog.pack();
                passwordDialog.setVisible(true);

            }
        }, MessageKey.HDM_COLD_SEED_QR_CODE, AwesomeIcon.QRCODE);
        btnPhras = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PasswordDialog passwordDialog = new PasswordDialog(new IDialogPasswordListener() {
                    @Override
                    public void onPasswordEntered(SecureCharSequence password) {
                        showHDMSeedPhras(password);
                    }
                });
                passwordDialog.pack();
                passwordDialog.setVisible(true);

            }
        }, MessageKey.HDM_COLD_SEED_WORD_LIST, AwesomeIcon.BITBUCKET);
        panel.add(btnColdQRCode, "align center,cell 3 2 ,grow,wrap");
        panel.add(btnScanServiceQRCode, "align center,cell 3 3,grow,wrap");
        panel.add(btnColdSeed, "align center,cell 3 4,grow,wrap");
        panel.add(btnPhras, "align center,cell 3 5,grow,wrap");


    }

    private void signMessageOfHDMKeychain(final String result, final SecureCharSequence password) {

        new Thread() {
            @Override
            public void run() {
                try {
                    final String signed = AddressManager.getInstance().getHdmKeychain
                            ().signHDMBId(result, password);
                    password.wipe();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            DisplayQRCodePanle displayQRCodePanle = new DisplayQRCodePanle(signed);
                            displayQRCodePanle.updateTitle(LocaliserUtils.getString("hdm_keychain_add_signed_server_qr_code_title"));
                            displayQRCodePanle.showPanel();

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            new MessageDialog(LocaliserUtils.getString("hdm_keychain_add_sign_server_qr_code_error")).showMsg();
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

    private void showPublicKeyQrCode(final SecureCharSequence password) {

        new Thread() {
            @Override
            public void run() {
                try {
                    final String pub = keychain.getExternalChainRootPubExtendedAsHex(password);
                    password.wipe();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            DisplayQRCodePanle displayQRCodePanle = new DisplayQRCodePanle(pub);
                            displayQRCodePanle.showPanel();
                            displayQRCodePanle.updateTitle(LocaliserUtils.getString("hdm_cold_pub_key_qr_code_name"));
                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
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
}
