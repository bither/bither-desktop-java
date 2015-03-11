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
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.IProgress;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.DialogPassword;
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
    private DialogPassword.PasswordGetter passwordGetter;
    private boolean isSignle = false;
    private JLabel labelRefrsh;

    private HDMHotAddDesktop hdmHotAddDesktop;

    public HDMHotPanel() {
        super(MessageKey.HDM, AwesomeIcon.FA_RECYCLE, false);
        passwordGetter = new DialogPassword.PasswordGetter(HDMHotPanel.this);
        hdmHotAddDesktop = new HDMHotAddDesktop(HDMHotPanel.this, HDMHotPanel.this, HDMHotPanel.this);
    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][][][][]", // Column constraints
                "[]10[][][]10[]" // Row constraints

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
                hdmHotAddDesktop.hotClick();
            }
        }, MessageKey.hdm_singular_check_title, AwesomeIcon.FA_TREE);

        panel.add(btnSignle, "align center,cell 5 3 ,shrink,wrap");
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
        }
    }

    private void moveToHot(boolean anim) {
        btnSignle.setEnabled(true);
        btnHot.setEnabled(true);
        btnCold.setEnabled(false);
        btnService.setEnabled(false);


    }

    @Override
    public void moveToCold(boolean anim) {
        btnSignle.setEnabled(false);
        btnHot.setEnabled(false);
        btnCold.setEnabled(true);
        btnService.setEnabled(false);

    }

    @Override
    public void moveToFinal(boolean isFinal) {
        btnSignle.setEnabled(false);
        btnHot.setEnabled(false);
        btnCold.setEnabled(false);
        btnService.setEnabled(false);
        colsePanel();
        Bither.refreshFrame();
    }

    @Override
    public void moveToServer(boolean anim) {
        btnSignle.setEnabled(false);
        btnHot.setEnabled(false);
        btnCold.setEnabled(false);
        btnService.setEnabled(true);
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
                }, true);
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

    }

    public boolean shouldGoSingularMode() {
        return isSignle;

    }

    public void singularHotFinish() {

    }

    public void singularColdFinish() {

    }

    public void singularServerFinish(List<String> words, String qr) {

    }

    public void singularShowNetworkFailure() {

    }

    @Override
    public void begin() {
        labelRefrsh.setVisible(true);
    }

    @Override
    public void end() {
        labelRefrsh.setVisible(false);
    }
}
