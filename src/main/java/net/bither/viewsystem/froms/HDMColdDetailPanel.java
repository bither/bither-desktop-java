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
import net.bither.bitherj.core.HDMKeychain;
import net.bither.bitherj.crypto.SecureCharSequence;
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
import net.bither.viewsystem.listener.IDialogPasswordListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class HDMColdDetailPanel extends WizardPanel {

    private JButton btnColdQRCode;
    private JButton btnScanServiceQRCode;
    private HDMKeychain keychain;

    public HDMColdDetailPanel() {
        super(MessageKey.HDM, AwesomeIcon.FA_RECYCLE);
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
                PasswordPanel dialogPassword = new PasswordPanel(new IDialogPasswordListener() {
                    @Override
                    public void onPasswordEntered(SecureCharSequence password) {
                        showPublicKeyQrCode(password);
                    }
                });
                dialogPassword.showPanel();


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
                            PasswordPanel dialogPassword = new PasswordPanel(new IDialogPasswordListener() {
                                @Override
                                public void onPasswordEntered(SecureCharSequence password) {
                                    signMessageOfHDMKeychain(result, password);
                                }
                            });
                            dialogPassword.showPanel();

                        }
                    }
                });
                selectQRCodeDialog.showPanel();

            }
        }, MessageKey.HDM_SERVER_QR_CODE, AwesomeIcon.CAMERA);

        panel.add(btnColdQRCode, "align center,cell 3 2 ,grow,wrap");
        panel.add(btnScanServiceQRCode, "align center,cell 3 3,grow,wrap");


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


}
