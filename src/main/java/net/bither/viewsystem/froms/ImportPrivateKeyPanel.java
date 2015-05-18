/*
 *
 *  Copyright 2014 http://Bither.net
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package net.bither.viewsystem.froms;

import net.bither.Bither;
import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.HDMKeychain;
import net.bither.bitherj.crypto.ECKey;
import net.bither.bitherj.crypto.EncryptedData;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.crypto.bip38.Bip38;
import net.bither.bitherj.exception.AddressFormatException;
import net.bither.bitherj.factory.ImportHDSeed;
import net.bither.bitherj.factory.ImportPrivateKey;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.PrivateKeyUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.factory.ImportHDSeedDesktop;
import net.bither.factory.ImportListener;
import net.bither.factory.ImportPrivateKeyDesktop;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.preference.UserPreference;
import net.bither.qrcode.IReadQRCode;
import net.bither.qrcode.IScanQRCode;
import net.bither.qrcode.SelectQRCodePanel;
import net.bither.qrcode.SelectTransportQRCodePanel;
import net.bither.utils.KeyUtil;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.DialogProgress;
import net.bither.viewsystem.dialogs.ImportBIP38PrivateTextDialog;
import net.bither.viewsystem.dialogs.ImportPrivateTextDialog;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.listener.ICheckPasswordListener;
import net.bither.viewsystem.listener.IDialogPasswordListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class ImportPrivateKeyPanel extends WizardPanel {

    private JButton btnQRCode;
    private JButton btnBIP38QRCode;

    private JButton btnPrivateKey;
    private JButton btnBIP38;

    private JButton btnHDMColdSeed;
    private JButton btnHDMColdPhras;

    private JButton btnHDAccountSeed;
    private JButton btnHDAccountPhras;


    private String bip38DecodeString;
    private JButton btnClone;
    private DialogProgress dp;

    public ImportPrivateKeyPanel() {
        super(MessageKey.IMPORT, AwesomeIcon.FA_SIGN_IN);
        dp = new DialogProgress();
    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][][]", // Column constraints
                "[][][][][][][][][][][]" // Row constraints
        ));
        btnQRCode = Buttons.newQRCodeButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closePanel();
                onQRCode();
            }
        }, MessageKey.IMPORT_PRIVATE_KEY_QRCODE);

        btnPrivateKey = Buttons.newFileTextButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closePanel();
                importPrivateText();

            }
        }, MessageKey.IMPORT_PRIVATE_KEY_TEXT);
        btnBIP38QRCode = Buttons.newQRCodeButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closePanel();
                onBIP38QRCode();
            }
        }, MessageKey.IMPORT_BIP38_PRIVATE_KEY_QRCODE);
        btnBIP38 = Buttons.newFileTextButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closePanel();
                ImportBIP38PrivateTextDialog importBIP38PrivateTextDialog = new ImportBIP38PrivateTextDialog();
                importBIP38PrivateTextDialog.pack();
                importBIP38PrivateTextDialog.setVisible(true);

            }
        }, MessageKey.IMPORT_BIP38_PRIVATE_KEY_TEXT);
        btnHDMColdSeed = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closePanel();
                onColdSeedQRCode();

            }
        }, MessageKey.import_hdm_cold_seed_qr_code, AwesomeIcon.QRCODE);
        btnHDMColdPhras = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closePanel();
                RestoreWalletSeedPhrasePanel restoreWalletSeedPhrasePanel = new RestoreWalletSeedPhrasePanel(ImportHDSeed.ImportHDSeedType.HDMColdPhrase);
                restoreWalletSeedPhrasePanel.showPanel();

            }
        }, MessageKey.import_hdm_cold_seed_phrase, AwesomeIcon.BITBUCKET);
        btnHDAccountSeed = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                closePanel();
                onHDAccountSeedQRCode();


            }
        }, MessageKey.import_hd_account_seed_qr_code, AwesomeIcon.QRCODE);
        btnHDAccountPhras = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closePanel();
                RestoreWalletSeedPhrasePanel restoreWalletSeedPhrasePanel = new RestoreWalletSeedPhrasePanel(ImportHDSeed.ImportHDSeedType.HDSeedPhrase);
                restoreWalletSeedPhrasePanel.showPanel();

            }
        }, MessageKey.import_hd_account_seed_phrase, AwesomeIcon.BITBUCKET);
        btnClone = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SelectTransportQRCodePanel selectTransportQRCodePanel = new SelectTransportQRCodePanel(new IScanQRCode() {
                    @Override
                    public void handleResult(String result, IReadQRCode readQRCode) {
                        readQRCode.close();

                        PasswordPanel dialogPassword = new PasswordPanel(
                                new CloneFromPasswordListenerI(result));
                        dialogPassword.setCheckPre(false);
                        dialogPassword.setTitle(LocaliserUtils.getString("clone_from_password"));
                        dialogPassword.showPanel();

                    }
                });
                selectTransportQRCodePanel.showPanel();

            }
        }, MessageKey.CLONE_QRCODE, AwesomeIcon.DOWNLOAD);
        panel.add(btnQRCode, "align center,cell 2 1 ,grow,wrap");
        panel.add(btnPrivateKey, "align center,cell 2 2,grow,wrap");
        panel.add(btnBIP38QRCode, "align center,cell 2 3,grow,wrap");
        panel.add(btnBIP38, "align center,cell 2 4,grow,wrap");
        if (UserPreference.getInstance().getAppMode() == BitherjSettings.AppMode.COLD
                ) {
            if (AddressManager.getInstance().getHdmKeychain() == null) {
                panel.add(btnHDMColdSeed, "align center,cell 2 5,grow,wrap");
                panel.add(btnHDMColdPhras, "align center,cell 2 6,grow,wrap");
                if (AddressManager.getInstance().getPrivKeyAddresses().size() == 0) {
                    panel.add(btnClone, "align center,cell 2 7,grow,wrap");
                }
            }
        } else {
            if (AddressManager.getInstance().getHdAccount() == null) {
                panel.add(btnHDAccountSeed, "align center,cell 2 5,grow,wrap");
                panel.add(btnHDAccountPhras, "align center,cell 2 6,grow,wrap");
            }

        }
    }

    private void onHDAccountSeedQRCode() {
        SelectQRCodePanel qrCodePanel = new SelectQRCodePanel(new IScanQRCode() {
            public void handleResult(final String result, IReadQRCode readQRCode) {

                if (QRCodeUtil.verifyBitherQRCode(result)) {
                    if (result.indexOf(QRCodeUtil.HD_QR_CODE_FLAG) == 0) {
                        readQRCode.close();
                        PasswordPanel dialogPassword = new PasswordPanel(
                                new ImportHDAccountSeedPasswordListener(result));
                        dialogPassword.setCheckPre(false);
                        dialogPassword.setCheckPasswordListener(new ICheckPasswordListener() {
                            @Override
                            public boolean checkPassword(SecureCharSequence password) {
                                String keyString = result.substring(1);
                                String[] passwordSeeds = QRCodeUtil.splitOfPasswordSeed(keyString);
                                String encreyptString = Utils.joinString(new String[]{passwordSeeds[0], passwordSeeds[1], passwordSeeds[2]}, QRCodeUtil.QR_CODE_SPLIT);
                                EncryptedData encryptedData = new EncryptedData(encreyptString);
                                byte[] result = null;
                                try {
                                    result = encryptedData.decrypt(password);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return result != null;
                            }
                        });
                        dialogPassword.setTitle(LocaliserUtils.getString("import_private_key_qr_code_password"));
                        dialogPassword.showPanel();

                    } else {
                        new MessageDialog(LocaliserUtils.getString("import_hdm_cold_seed_format_error")).showMsg();

                    }


                } else {
                    readQRCode.reTry("");
                }

            }
        });
        qrCodePanel.showPanel();
    }


    private void onColdSeedQRCode() {
        SelectQRCodePanel qrCodePanel = new SelectQRCodePanel(new IScanQRCode() {
            public void handleResult(final String result, IReadQRCode readQRCode) {

                if (QRCodeUtil.verifyBitherQRCode(result)) {
                    if (result.indexOf(QRCodeUtil.HDM_QR_CODE_FLAG) == 0) {
                        readQRCode.close();
                        PasswordPanel dialogPassword = new PasswordPanel(
                                new ImportHDSeedPasswordListener(result));
                        dialogPassword.setCheckPre(false);
                        dialogPassword.setCheckPasswordListener(new ICheckPasswordListener() {
                            @Override
                            public boolean checkPassword(SecureCharSequence password) {
                                String keyString = result.substring(1);
                                String[] passwordSeeds = QRCodeUtil.splitOfPasswordSeed(keyString);
                                String encreyptString = Utils.joinString(new String[]{passwordSeeds[0], passwordSeeds[1], passwordSeeds[2]}, QRCodeUtil.QR_CODE_SPLIT);
                                EncryptedData encryptedData = new EncryptedData(encreyptString);
                                byte[] result = null;
                                try {
                                    result = encryptedData.decrypt(password);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return result != null;
                            }
                        });
                        dialogPassword.setTitle(LocaliserUtils.getString("import_private_key_qr_code_password"));
                        dialogPassword.showPanel();

                    } else {
                        new MessageDialog(LocaliserUtils.getString("import_hdm_cold_seed_format_error")).showMsg();

                    }


                } else {
                    readQRCode.reTry("");
                }

            }
        });
        qrCodePanel.showPanel();
    }

    private void onQRCode() {
        SelectQRCodePanel qrCodePanel = new SelectQRCodePanel(new IScanQRCode() {
            public void handleResult(final String result, IReadQRCode readQRCode) {
                if (QRCodeUtil.verifyBitherQRCode(result)) {
                    readQRCode.close();
                    PasswordPanel dialogPassword = new PasswordPanel(new ImportPrivateKeyPasswordListenerI(result, false));

                    dialogPassword.setCheckPre(false);

                    dialogPassword.setCheckPasswordListener(new ICheckPasswordListener() {
                        @Override
                        public boolean checkPassword(SecureCharSequence password) {
                            ECKey ecKey = PrivateKeyUtil.getECKeyFromSingleString(result, password);
                            boolean result = ecKey != null;
                            return result;
                        }
                    });
                    dialogPassword.showPanel();


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
            if (password == null) {
                return;
            }

            if (isFromBip38) {
                PasswordPanel dialogPassword = new PasswordPanel(walletIDialogPasswordListener);
                dialogPassword.showPanel();


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
            if (password == null) {
                return;
            }
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
                    PasswordPanel dialogPassword = new PasswordPanel(new ImportPrivateKeyPasswordListenerI(result, true));

                    dialogPassword.setCheckPre(false);

                    dialogPassword.setCheckPasswordListener(new ICheckPasswordListener() {
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
                    dialogPassword.showPanel();


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

    private class ImportHDAccountSeedPasswordListener implements IDialogPasswordListener {
        private String content;


        public ImportHDAccountSeedPasswordListener(String content) {
            this.content = content;

        }

        @Override
        public void onPasswordEntered(SecureCharSequence password) {
            if (password == null) {
                return;
            }

            ImportHDSeedDesktop importHDSeedAndroid = new ImportHDSeedDesktop
                    (ImportHDSeed.ImportHDSeedType.HDSeedQRCode, content, null, password, new ImportListener() {
                        @Override
                        public void importSuccess() {

                        }
                    });
            importHDSeedAndroid.importHDSeed();

        }

    }

    private class ImportHDSeedPasswordListener implements IDialogPasswordListener {
        private String content;


        public ImportHDSeedPasswordListener(String content) {
            this.content = content;

        }

        @Override
        public void onPasswordEntered(SecureCharSequence password) {
            if (password == null) {
                return;
            }

            ImportHDSeedDesktop importHDSeedAndroid = new ImportHDSeedDesktop
                    (content, password, new ImportListener() {
                        @Override
                        public void importSuccess() {

                        }
                    });
            importHDSeedAndroid.importHDMColdSeed();

        }

    }

    private class CloneFromPasswordListenerI implements IDialogPasswordListener {
        private String content;

        public CloneFromPasswordListenerI(String content) {
            this.content = content;
        }

        @Override
        public void onPasswordEntered(SecureCharSequence password) {
            if (password == null) {
                return;
            }

            CloneThread cloneThread = new CloneThread(content, password);
            cloneThread.start();
        }
    }


    private class CloneThread extends Thread {
        private String content;
        private SecureCharSequence password;

        public CloneThread(String content, SecureCharSequence password) {
            this.content = content;
            this.password = password;
        }

        public void run() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {

                }
            });
            List<Address> addressList = PrivateKeyUtil.getECKeysFromBackupString(content, password);
            HDMKeychain hdmKeychain = PrivateKeyUtil.getHDMKeychain(content, password);

            if ((addressList == null || addressList.size() == 0) && (hdmKeychain == null)) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dp.dispose();
                        new MessageDialog(LocaliserUtils.getString("clone_from_failed")).showMsg();
                    }
                });
                return;
            }

            KeyUtil.addAddressListByDesc(addressList);
            if (hdmKeychain != null) {
                KeyUtil.setHDKeyChain(hdmKeychain);
            }
            password.wipe();

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    dp.dispose();
                    new MessageDialog(LocaliserUtils.getString("clone_from_success")).showMsg();
                    closePanel();
                    Bither.refreshFrame();
                }
            });
        }
    }

}
