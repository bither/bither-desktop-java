package net.bither.viewsystem.froms;

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
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.base.RadioButtons;
import net.bither.viewsystem.dialogs.DialogPassword;
import net.bither.xrandom.HDMKeychainHotUEntropyDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class HDMHotPanel extends WizardPanel implements IPasswordGetterDelegate, HDMHotAdd.IHDMHotAddDelegate, HDMSingular.HDMSingularDelegate {

    private JButton btnHot;
    private JButton btnCold;
    private JButton btnService;
    private JRadioButton radioButton;
    private DialogPassword.PasswordGetter passwordGetter;


    private HDMHotAddDesktop hdmHotAddDesktop;

    public HDMHotPanel() {
        super(MessageKey.HDM, AwesomeIcon.FA_RECYCLE, false);
        passwordGetter = new DialogPassword.PasswordGetter(HDMHotPanel.this);
        hdmHotAddDesktop = new HDMHotAddDesktop(HDMHotPanel.this, HDMHotPanel.this);
    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][][][][]", // Column constraints
                "[]10[][][][]" // Row constraints

        ));


        btnHot = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addHot();
            }
        }, MessageKey.HDM, AwesomeIcon.FA_RECYCLE);
        btnCold = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCold();

            }
        }, MessageKey.HDM, AwesomeIcon.FA_RECYCLE);
        btnService = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addService();
            }
        }, MessageKey.HDM, AwesomeIcon.FA_RECYCLE);


        panel.add(btnHot, "align center,cell 3 2 ,grow,wrap");
        panel.add(btnCold, "align center,cell 3 3 ,grow,wrap");
        panel.add(btnService, "align center,cell 3 4 ,grow,wrap");

        radioButton = RadioButtons.newRadioButton(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        }, MessageKey.hdm_singular_check_title, null);
        radioButton.setFocusPainted(false);
        radioButton.setSelected(true);
        panel.add(radioButton, "align center,cell 3 5 ,grow,wrap");


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


    @Override
    public void moveToCold(boolean anim) {

    }

    @Override
    public void moveToFinal(boolean isFinal) {

    }

    @Override
    public void moveToServer(boolean anim) {

    }

    @Override
    public void callServerQRCode() {

    }

    @Override
    public void callKeychainHotUEntropy() {
        HDMKeychainHotUEntropyDialog hdmKeychainHotUEntropyDialog = new HDMKeychainHotUEntropyDialog(passwordGetter);
        hdmKeychainHotUEntropyDialog.pack();
        hdmKeychainHotUEntropyDialog.setVisible(true);

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
        return radioButton.isSelected();

    }

    public void singularHotFinish() {

    }

    public void singularColdFinish() {

    }

    public void singularServerFinish(List<String> words, String qr) {

    }

    public void singularShowNetworkFailure() {

    }

}
