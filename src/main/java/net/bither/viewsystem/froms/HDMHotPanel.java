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
import net.bither.BitherUI;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.delegate.HDMHotAdd;
import net.bither.bitherj.delegate.HDMSingular;
import net.bither.bitherj.delegate.IPasswordGetterDelegate;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.qrcode.IReadQRCode;
import net.bither.qrcode.IScanQRCode;
import net.bither.qrcode.SelectTransportQRCodePanel;
import net.bither.utils.HDMHotAddDesktop;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.IProgress;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.themes.Themes;
import net.bither.xrandom.HDMKeychainHotUEntropyDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class HDMHotPanel extends WizardPanel implements IPasswordGetterDelegate, HDMHotAdd.IHDMHotAddDelegate, HDMSingular.HDMSingularDelegate, IProgress {

    private JButton btnHot;
    private JButton btnCold;
    private JButton btnService;
    private JButton btnSignle;
    private JButton btnAddHdmAddress;
    private PasswordPanel.PasswordGetter passwordGetter;
    private boolean isSignle = false;
    private JLabel labelRefrsh;

    private HDMHotAddDesktop hdmHotAddDesktop;

    public HDMHotPanel() {
        super(MessageKey.HDM, AwesomeIcon.FA_RECYCLE);
        passwordGetter = new PasswordPanel.PasswordGetter(HDMHotPanel.this);
        hdmHotAddDesktop = new HDMHotAddDesktop(HDMHotPanel.this, HDMHotPanel.this, HDMHotPanel.this);
    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][][][][]", // Column constraints
                "[]10[][][]10" // Row constraints

        ));
        labelRefrsh = Labels.newSpinner(Themes.currentTheme.fadedText(), BitherUI.NORMAL_PLUS_ICON_SIZE);
        panel.add(labelRefrsh, "align center,span,wrap");
        labelRefrsh.setVisible(false);

        btnHot = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addHot();
            }
        }, MessageKey.hdm_keychain_add_hot, AwesomeIcon.FA_RECYCLE);
        btnCold = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCold();
            }
        }, MessageKey.hdm_keychain_add_cold, AwesomeIcon.FA_RECYCLE);
        btnService = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addService();
            }
        }, MessageKey.hdm_keychain_add_server, AwesomeIcon.FA_RECYCLE);


        panel.add(btnHot, "align center,cell 2 1 ,shrink,wrap");
        panel.add(btnCold, "align center,cell 3 1 ,shrink,wrap");
        panel.add(btnService, "align center,cell 4 1 ,shrink,wrap");
        btnSignle = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isSignle = true;
                setCancelEnabled(false);
                hdmHotAddDesktop.hotClick();
            }
        }, MessageKey.hdm_singular_check_title, AwesomeIcon.FA_TREE);

        btnAddHdmAddress = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closePanel();
                HDMAddAddressPanel hdmAddAddressPanel = new HDMAddAddressPanel();
                hdmAddAddressPanel.showPanel();

            }
        }, MessageKey.activity_name_add_hdm_address, AwesomeIcon.PLUS);
        panel.add(btnSignle, "align center,cell 4 3 ,shrink,wrap");
        panel.add(btnAddHdmAddress, "align center,cell 5 3 ,shrink,wrap");
        findCurrentStep();


    }

    private void addHot() {
        hdmHotAddDesktop.hotClick();

    }

    private void addCold() {
        hdmHotAddDesktop.coldClick();
    }


    private void addService() {
        hdmHotAddDesktop.serviceClick();
    }

    private void findCurrentStep() {
        moveToHot(false);
        if (AddressManager.getInstance().getHdmKeychain() != null) {
            moveToCold(false);
            if (AddressManager.getInstance().getHdmKeychain().uncompletedAddressCount() > 0) {
                moveToServer(false);
                if (hdmHotAddDesktop.hdmKeychainLimit) {
                    moveToFinal(false);
                }
            }
            if (AddressManager.getInstance().getHdmKeychain().isInRecovery()) {
                btnSignle.setEnabled(false);
                btnHot.setEnabled(false);
                btnCold.setEnabled(false);
                btnService.setEnabled(false);
                btnAddHdmAddress.setEnabled(false);
            }
        }

    }

    private void moveToHot(boolean anim) {
        btnSignle.setEnabled(true);
        btnHot.setEnabled(true);
        btnCold.setEnabled(false);
        btnService.setEnabled(false);
        btnAddHdmAddress.setEnabled(false);

    }

    @Override
    public void moveToCold(boolean anim) {
        btnSignle.setEnabled(false);
        btnHot.setEnabled(false);
        btnCold.setEnabled(true);
        btnService.setEnabled(false);
        btnAddHdmAddress.setEnabled(false);
        if (hdmHotAddDesktop.singular.isInSingularMode()) {
            hdmHotAddDesktop.singular.cold();
        }

    }

    @Override
    public void moveToFinal(boolean isFinal) {
        btnSignle.setEnabled(false);
        btnHot.setEnabled(false);
        btnCold.setEnabled(false);
        btnService.setEnabled(false);
        closePanel();
        Bither.refreshFrame();
        btnAddHdmAddress.setEnabled(true);
    }

    @Override
    public void moveToServer(boolean anim) {
        btnSignle.setEnabled(false);
        btnHot.setEnabled(false);
        btnCold.setEnabled(false);
        btnService.setEnabled(true);
        btnAddHdmAddress.setEnabled(false);
        if (hdmHotAddDesktop.singular.isInSingularMode()) {
            hdmHotAddDesktop.singular.server();
        }
    }

    @Override
    public void callServerQRCode() {

    }

    @Override
    public void callKeychainHotUEntropy() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                passwordGetter.getPassword();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        HDMKeychainHotUEntropyDialog hdmKeychainHotUEntropyDialog = new HDMKeychainHotUEntropyDialog(passwordGetter);
                        hdmKeychainHotUEntropyDialog.pack();
                        hdmKeychainHotUEntropyDialog.setVisible(true);
                        findCurrentStep();
                    }
                });

            }
        }).start();

    }

    @Override
    public void callScanCold() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SelectTransportQRCodePanel selectTransportQRCodePanel = new SelectTransportQRCodePanel(new IScanQRCode() {
                    @Override
                    public void handleResult(String result, IReadQRCode readQRCode) {
                        readQRCode.close();
                        hdmHotAddDesktop.setCallScanColdResult(result);
                    }
                });
                selectTransportQRCodePanel.showPanel();

            }
        });

    }

    @Override
    public void beforePasswordDialogShow() {

    }

    @Override
    public void afterPasswordDialogDismiss() {

    }

    public void setSingularModeAvailable(boolean available) {

    }

    public void onSingularModeBegin() {
        labelRefrsh.setVisible(true);
        btnService.setEnabled(true);


    }

    public boolean shouldGoSingularMode() {
        return isSignle;

    }

    public void singularHotFinish() {
        moveToCold(true);
        btnService.setEnabled(true);

    }

    public void singularColdFinish() {
        moveToServer(true);
        btnService.setEnabled(true);
    }

    public void singularServerFinish(List<String> words, String qr) {
        endProgress();
        setCancelEnabled(true);
        moveToFinal(true);
        btnAddHdmAddress.setEnabled(true);
        btnService.setEnabled(false);
        HDMSingularSeedPanel hdmSingularSeedPanel = new HDMSingularSeedPanel(words, qr);
        hdmSingularSeedPanel.showPanel();


    }

    public void singularShowNetworkFailure() {
        endProgress();
        setCancelEnabled(true);
        new MessageDialog(LocaliserUtils.getString("network_or_connection_error")).showMsg();

    }

    @Override
    public void beginProgress() {
        labelRefrsh.setVisible(true);
    }

    @Override
    public void endProgress() {
        labelRefrsh.setVisible(false);
    }
}
