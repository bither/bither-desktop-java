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
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.qrcode.DisplayBitherQRCodePanel;
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

public class EnterpriseColdPanel extends WizardPanel implements IPasswordGetterDelegate {

    private PasswordPanel.PasswordGetter passwordGetter;
    private JButton btnFirstMasterPub;
    private JButton btnSecondMasterPub;

    public EnterpriseColdPanel() {
        super(MessageKey.HDM, AwesomeIcon.FA_RECYCLE);

        passwordGetter = new PasswordPanel.PasswordGetter(EnterpriseColdPanel.this);
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
                                password.wipe();
                                Bither.refreshFrame();


                                //   }
                            }
                        });

                    }
                }).start();

            }
        });


    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][][][][]", // Column constraints
                "[][][][][][]" // Row constraints
        ));
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
        }, MessageKey.EXTENDED_PUBLIC_KEY, AwesomeIcon.HEADER);
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
        }, MessageKey.EXTENDED_PUBLIC_KEY, AwesomeIcon.HEADER);
        if (AddressManager.getInstance().hasDesktopHDMKeychain()) {
            panel.add(btnFirstMasterPub, "align center,cell 3 0 ,grow ,shrink,wrap");
            panel.add(btnSecondMasterPub, "align center,cell 3 1 ,grow ,shrink,wrap");
        }

    }

    @Override
    public void beforePasswordDialogShow() {

    }

    @Override
    public void afterPasswordDialogDismiss() {

    }

}
