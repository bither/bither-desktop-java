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
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.DesktopHDMKeychain;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.delegate.IPasswordGetterDelegate;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.qrcode.IReadQRCode;
import net.bither.qrcode.IScanQRCode;
import net.bither.qrcode.SelectQRCodePanel;
import net.bither.utils.KeyUtil;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.DialogProgress;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class EnterpriseHotPanel extends WizardPanel implements IPasswordGetterDelegate {

    private PasswordPanel.PasswordGetter passwordGetter;

    private JButton btnImportFirstMasterPub;
    private JButton btnImportSecondMasterPub;

    private JButton btnAddKeychain;

    private byte[] bytesFirst = null;
    private byte[] bytesSecond = null;

    //todo get coin address, banlance
    public EnterpriseHotPanel() {
        super(MessageKey.HDM, AwesomeIcon.FA_RECYCLE);
        passwordGetter = new PasswordPanel.PasswordGetter(EnterpriseHotPanel.this);

        ininPubKeyUI();
        initAddKeychain();
        initAddress();

    }

    private void initAddKeychain() {
        btnAddKeychain = Buttons.newNormalButton(new AbstractAction() {
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
//                                if (xrandomCheckBox.isSelected()) {
//                                    HDMKeychainColdUEntropyDialog hdmKeychainColdUEntropyDialog = new HDMKeychainColdUEntropyDialog(passwordGetter);
//                                    hdmKeychainColdUEntropyDialog.pack();
//                                    hdmKeychainColdUEntropyDialog.setVisible(true);
//                                } else {
                                DesktopHDMKeychain chain = new DesktopHDMKeychain(new SecureRandom(), password);
                                List<DesktopHDMKeychain> desktopHDMKeychainList = new ArrayList<DesktopHDMKeychain>();
                                desktopHDMKeychainList.add(chain);
                                KeyUtil.setDesktopHMDKeychains(desktopHDMKeychainList);
                                password.wipe();
                              //  Bither.refreshFrame();


                                //   }
                            }
                        });

                    }
                }).start();


            }
        }, MessageKey.EXTENDED_PUBLIC_KEY, AwesomeIcon.HEADER);

    }

    private void initAddress() {

    }

    private void ininPubKeyUI() {
        btnImportFirstMasterPub = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SelectQRCodePanel selectQRCodePanel = new SelectQRCodePanel(new IScanQRCode() {
                    @Override
                    public void handleResult(String result, IReadQRCode readQRCode) {
                        readQRCode.close();
                        if (QRCodeUtil.verifyBitherQRCode(result)) {
                            bytesFirst = Utils.hexStringToByteArray(result);
                            btnImportFirstMasterPub.setEnabled(true);
                            addOtherPubkey();
                        } else {
                            readQRCode.reTry("");
                        }

                    }
                });
                selectQRCodePanel.showPanel();

            }
        }, MessageKey.EXTENDED_PUBLIC_KEY, AwesomeIcon.HEADER);
        btnImportSecondMasterPub = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SelectQRCodePanel selectQRCodePanel = new SelectQRCodePanel(new IScanQRCode() {
                    @Override
                    public void handleResult(String result, IReadQRCode readQRCode) {
                        readQRCode.close();
                        if (QRCodeUtil.verifyBitherQRCode(result)) {
                            bytesSecond = Utils.hexStringToByteArray(result);
                            btnImportSecondMasterPub.setEnabled(true);
                            addOtherPubkey();
                        } else {
                            readQRCode.reTry("");
                        }

                    }
                });
                selectQRCodePanel.showPanel();

            }
        }, MessageKey.EXTENDED_PUBLIC_KEY, AwesomeIcon.HEADER);
    }

    private void addOtherPubkey() {
        if (bytesSecond == null || bytesFirst == null || !AddressManager.getInstance().hasDesktopHDMKeychain()) {
            return;
        }
        final DialogProgress dialogProgress = new DialogProgress();

        new Thread(new Runnable() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dialogProgress.pack();
                        dialogProgress.setVisible(true);
                    }
                });
                DesktopHDMKeychain desktopHDMKeychain =
                        AddressManager.getInstance().getDesktopHDMKeychains().get(0);
                desktopHDMKeychain.addAccountKey(bytesFirst, bytesSecond);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dialogProgress.dispose();
                    }
                });

            }
        }).start();


    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][][][][]", // Column constraints
                "[][][][][][]" // Row constraints
        ));

        if (AddressManager.getInstance().hasDesktopHDMKeychain()) {
            panel.add(btnImportFirstMasterPub, "align center,cell 3 0 ,grow ,shrink,wrap");
            panel.add(btnImportSecondMasterPub, "align center,cell 3 1 ,grow ,shrink,wrap");
        } else {
            panel.add(btnAddKeychain, "align center,cell 3 0 ,grow ,shrink,wrap");
        }


    }

    @Override
    public void beforePasswordDialogShow() {

    }

    @Override
    public void afterPasswordDialogDismiss() {

    }
}
