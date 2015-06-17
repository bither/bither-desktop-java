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

import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.DesktopHDMKeychain;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.delegate.IPasswordGetterDelegate;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.UnitUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.fonts.AwesomeIcon;
import net.bither.implbitherj.TxNotificationCenter;
import net.bither.languages.MessageKey;
import net.bither.qrcode.DisplayQRCodePanle;
import net.bither.qrcode.IReadQRCode;
import net.bither.qrcode.IScanQRCode;
import net.bither.qrcode.SelectQRCodePanel;
import net.bither.utils.KeyUtil;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.DialogProgress;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class EnterpriseHotPanel extends WizardPanel implements IPasswordGetterDelegate, TxNotificationCenter.ITxListener {

    private PasswordPanel.PasswordGetter passwordGetter;

    private JButton btnImportFirstMasterPub;
    private JButton btnImportSecondMasterPub;

    private JButton btnAddKeychain;

    private JButton btnAddress;
    private JButton btnSignTx;

    private JLabel labelBanlance;

    private byte[] bytesFirst = null;
    private byte[] bytesSecond = null;
    private JPanel panel;


    public EnterpriseHotPanel() {
        super(MessageKey.HDM, AwesomeIcon.FA_RECYCLE);
        passwordGetter = new PasswordPanel.PasswordGetter(EnterpriseHotPanel.this);
        TxNotificationCenter.addTxListener(EnterpriseHotPanel.this);

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
                                // closePanel();
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
                                refreshPanel();
                                //  Bither.refreshFrame();


                                //   }
                            }
                        });

                    }
                }).start();


            }
        }, MessageKey.add_desktop_hdm_hot_keychain, AwesomeIcon.PLUS);

    }

    private void initAddress() {
        labelBanlance = Labels.newValueLabel("");
        btnAddress = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!AddressManager.getInstance().hasDesktopHDMKeychain()) {
                    return;
                }
                DesktopHDMKeychain hdmKeychain = AddressManager.getInstance().getDesktopHDMKeychains().get(0);
                DisplayQRCodePanle displayQRCodePanle = new DisplayQRCodePanle(hdmKeychain.externalAddress());
                displayQRCodePanle.showPanel();

            }
        }, MessageKey.address, AwesomeIcon.QRCODE);
        btnSignTx = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        }, MessageKey.SIGN_TX, AwesomeIcon.PENCIL);

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
        }, MessageKey.import_desktop_hdm_first_account, AwesomeIcon.FA_SIGN_IN);
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
        }, MessageKey.import_desktop_hdm_second_account, AwesomeIcon.FA_SIGN_IN);
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
                        refreshPanel();
                        dialogProgress.dispose();
                    }
                });

            }
        }).start();


    }


    private void refreshPanel() {
        panel.removeAll();
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][][][][]", // Column constraints
                "[][][][][][]" // Row constraints
        ));

        if (AddressManager.getInstance().hasDesktopHDMKeychain()) {
            DesktopHDMKeychain desktopHDMKeychain = AddressManager.getInstance().getDesktopHDMKeychains().get(0);
            if (desktopHDMKeychain.hasDesktopHDMAddress()) {
                panel.add(labelBanlance, "align center,cell 3 0 ,shrink,wrap");
                panel.add(btnAddress, "align center,cell 3 1 ,shrink,wrap");
                panel.add(btnSignTx, "align center,cell 3 2  ,shrink,wrap");
                refreshBanlance();
            } else {
                panel.add(btnImportFirstMasterPub, "align center,cell 3 0 ,shrink,wrap");
                panel.add(btnImportSecondMasterPub, "align center,cell 3 1 ,shrink,wrap");
            }
        } else {
            panel.add(btnAddKeychain, "align center,cell 3 0  ,shrink,wrap");
        }

    }

    private void refreshBanlance() {
        if (AddressManager.getInstance().hasDesktopHDMKeychain()) {
            DesktopHDMKeychain desktopHDMKeychain = AddressManager.getInstance().getDesktopHDMKeychains().get(0);
            labelBanlance.setText(LocaliserUtils.getString("send_confirm_amount") + UnitUtil.formatValue(desktopHDMKeychain.getBalance(), UnitUtil.BitcoinUnit.BTC));
        }

    }

    @Override
    public void initialiseContent(JPanel panel) {
        this.panel = panel;
        refreshPanel();


    }

    @Override
    public void beforePasswordDialogShow() {

    }

    @Override
    public void afterPasswordDialogDismiss() {

    }

    @Override
    public void notificatTx(String address, Tx tx, Tx.TxNotificationType txNotificationType, long deltaBalance) {
        refreshBanlance();
    }

    @Override
    public void closePanel() {
        super.closePanel();
        TxNotificationCenter.removeTxListener(EnterpriseHotPanel.this);
    }
}
