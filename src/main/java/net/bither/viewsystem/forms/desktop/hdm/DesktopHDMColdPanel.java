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

package net.bither.viewsystem.forms.desktop.hdm;

import com.github.sarxos.webcam.Webcam;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.DesktopHDMKeychain;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.delegate.IPasswordGetterDelegate;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.qrcode.DisplayBitherQRCodePanel;
import net.bither.utils.KeyUtil;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.DialogProgress;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.forms.PasswordPanel;
import net.bither.viewsystem.forms.SelectWebcamPanel;
import net.bither.viewsystem.forms.WizardPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class DesktopHDMColdPanel extends WizardPanel implements IPasswordGetterDelegate, SelectWebcamPanel.ISelectWencamListener {

    private PasswordPanel.PasswordGetter passwordGetter;
    private JButton btnAddHDMKeychain;
    private JButton btnFirstMasterPub;
    private JButton btnSecondMasterPub;
    private JButton btnSignTransaction;
    private JPanel panel;
    private SecureCharSequence password;

    public DesktopHDMColdPanel() {
        super(MessageKey.HDM, AwesomeIcon.FA_RECYCLE);
        passwordGetter = new PasswordPanel.PasswordGetter(DesktopHDMColdPanel.this);
        initUI();


    }

    private void initUI() {

        btnFirstMasterPub = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final DialogProgress dp = new DialogProgress();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final SecureCharSequence password = passwordGetter.getPassword();
                        if (password == null) {
                            return;
                        }
                        List<DesktopHDMKeychain> desktopHDMKeychains =
                                AddressManager.getInstance().getDesktopHDMKeychains();
                        if (desktopHDMKeychains == null || desktopHDMKeychains.size() == 0) {
                            return;
                        }
                        DesktopHDMKeychain desktopHDMKeychain = desktopHDMKeychains.get(0);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                dp.pack();
                                dp.setVisible(true);
                            }
                        });
                        final String extendPubkey = desktopHDMKeychain.getMasterPubKeyExtendedStr(password);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                dp.dispose();
                                DisplayBitherQRCodePanel bitherQRCodePanel =
                                        new DisplayBitherQRCodePanel(extendPubkey);
                                bitherQRCodePanel.showPanel();
                            }
                        });


                    }
                }).start();

            }
        }, MessageKey.desktop_hdm_first_account, AwesomeIcon.HEADER);
        btnSecondMasterPub = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final DialogProgress dp = new DialogProgress();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final SecureCharSequence password = passwordGetter.getPassword();
                        if (password == null) {
                            return;
                        }
                        List<DesktopHDMKeychain> desktopHDMKeychains =
                                AddressManager.getInstance().getDesktopHDMKeychains();
                        if (desktopHDMKeychains == null || desktopHDMKeychains.size() == 0) {
                            return;
                        }
                        DesktopHDMKeychain desktopHDMKeychain = desktopHDMKeychains.get(1);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                dp.pack();
                                dp.setVisible(true);
                            }
                        });
                        final String extendPubkey = desktopHDMKeychain.getMasterPubKeyExtendedStr(password);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                dp.dispose();
                                DisplayBitherQRCodePanel bitherQRCodePanel =
                                        new DisplayBitherQRCodePanel(extendPubkey);
                                bitherQRCodePanel.showPanel();
                            }
                        });


                    }
                }).start();


            }
        }, MessageKey.desktop_hdm_second_account, AwesomeIcon.HEADER);
        btnSignTransaction = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (AddressManager.getInstance().getPrivKeyAddresses().size() == 0 && AddressManager.getInstance().getHdmKeychain() == null && !AddressManager.getInstance().hasDesktopHDMKeychain()) {
                    new MessageDialog(LocaliserUtils.getString("private_key_is_empty")).showMsg();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final SecureCharSequence secureCharSequence = passwordGetter.getPassword();
                            if (secureCharSequence == null) {
                                return;
                            }
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    password = secureCharSequence;
                                    SelectWebcamPanel selectWebcamPanel = new SelectWebcamPanel(DesktopHDMColdPanel.this);
                                    selectWebcamPanel.showPanel();
                                }
                            });


                        }
                    }).start();
                }
            }
        }, MessageKey.SIGN_TX, AwesomeIcon.PENCIL);
        btnAddHDMKeychain = Buttons.newNormalButton(new AbstractAction() {
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
                                // closePanel();
//                                if (xrandomCheckBox.isSelected()) {
//                                    HDMKeychainColdUEntropyDialog hdmKeychainColdUEntropyDialog = new HDMKeychainColdUEntropyDialog(passwordGetter);
//                                    hdmKeychainColdUEntropyDialog.pack();
//                                    hdmKeychainColdUEntropyDialog.setVisible(true);
//                                } else {
                                List<DesktopHDMKeychain> desktopHDMKeychainList = new ArrayList<DesktopHDMKeychain>();
                                DesktopHDMKeychain chain1 = new DesktopHDMKeychain(new SecureRandom(), password);
                                desktopHDMKeychainList.add(chain1);
                                DesktopHDMKeychain chain2 = new DesktopHDMKeychain(new SecureRandom(), password);
                                desktopHDMKeychainList.add(chain2);
                                KeyUtil.setDesktopHMDKeychains(desktopHDMKeychainList);
                                passwordGetter.wipe();
                                refreshPanel();


                                //   }
                            }
                        });

                    }
                }).start();

            }
        }, MessageKey.add_desktop_hdm_cold_keychain, AwesomeIcon.PLUS);

    }

    @Override
    public void initialiseContent(JPanel panel) {
        this.panel = panel;
        refreshPanel();
    }

    @Override
    public void closePanel() {
        super.closePanel();
        if (passwordGetter != null) {
            passwordGetter.wipe();
        }
    }

    private void refreshPanel() {
        panel.removeAll();
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][][][][]", // Column constraints
                "[][][][][][]" // Row constraints
        ));


        if (AddressManager.getInstance().hasDesktopHDMKeychain()) {
            panel.add(btnFirstMasterPub, "align center,cell 3 0 ,shrink,wrap");
            panel.add(btnSecondMasterPub, "align center,cell 3 1 ,shrink,wrap");
            panel.add(btnSignTransaction, "align center,cell 3 2 ,shrink,wrap");
        } else {
            panel.add(btnAddHDMKeychain, "align center,cell 3 0 ,shrink,wrap");
        }

    }

    @Override
    public void beforePasswordDialogShow() {

    }

    @Override
    public void afterPasswordDialogDismiss() {

    }

    @Override
    public void onSelect(Webcam webcam) {
        if (webcam != null) {
            DesktopHDMColdMsgPanel desktopHDMColdMsgPanel = new DesktopHDMColdMsgPanel(password, webcam);
            desktopHDMColdMsgPanel.pack();
            desktopHDMColdMsgPanel.setVisible(true);
        }

    }
}
