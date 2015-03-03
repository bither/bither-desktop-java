package net.bither.viewsystem.froms;

import net.bither.BitherSetting;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.HDMBId;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.delegate.HDMHotAdd;
import net.bither.bitherj.delegate.IPasswordGetterDelegate;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.qrcode.IReadQRCode;
import net.bither.qrcode.IScanQRCode;
import net.bither.qrcode.SelectTransportQRCodePanel;
import net.bither.utils.HDMHotAddDesktop;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.DialogPassword;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.xrandom.HDMKeychainHotUEntropyDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

public class HDMHotPanel extends WizardPanel implements IPasswordGetterDelegate, HDMHotAdd.IHDMHotAddDelegate {
    private JButton btnHot;
    private JButton btnCold;
    private JButton btnService;
    private DialogPassword.PasswordGetter passwordGetter;

    private HDMBId hdmBid;

    private byte[] coldRoot;
    private boolean hdmKeychainLimit;
    private HDMHotAddDesktop hdmHotAddDesktop;

    public HDMHotPanel() {
        super(MessageKey.HDM, AwesomeIcon.FA_RECYCLE, false);
        passwordGetter = new DialogPassword.PasswordGetter(HDMHotPanel.this);
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
        passwordGetter = new DialogPassword.PasswordGetter();


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
        SelectTransportQRCodePanel selectTransportQRCodePanel = new SelectTransportQRCodePanel(new IScanQRCode() {
            @Override
            public void handleResult(String result, IReadQRCode readQRCode) {
                try {
                    readQRCode.close();
                    if (Utils.isEmpty(result) || !QRCodeUtil.verifyBitherQRCode(result)) {
                        return;
                    }
                    coldRoot = Utils.hexStringToByteArray(result);
                    final int count = BitherSetting.HDM_ADDRESS_PER_SEED_PREPARE_COUNT -
                            AddressManager.getInstance().getHdmKeychain().uncompletedAddressCount();
                    if (passwordGetter.hasPassword() && count > 0) {

                    }
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                if (count > 0) {
                                    SecureCharSequence password = passwordGetter.getPassword();
                                    if (password == null) {

                                        return;
                                    }
                                    AddressManager.getInstance().getHdmKeychain().prepareAddresses
                                            (count, password, Arrays.copyOf(coldRoot, coldRoot.length));
                                }
                                hdmHotAddDesktop.initHDMBidFromColdRoot();
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        moveToServer(true);
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                coldRoot = null;
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        new MessageDialog(LocaliserUtils.getString("hdm_keychain_add_scan_cold")).showMsg();
                                    }
                                });
                            }
                        }
                    }.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    coldRoot = null;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            new MessageDialog(LocaliserUtils.getString("hdm_keychain_add_scan_cold")).showMsg();
                        }
                    });
                }
            }
        }, true);
        selectTransportQRCodePanel.showPanel();

    }



    @Override
    public void beforePasswordDialogShow() {

    }

    @Override
    public void afterPasswordDialogDismiss() {

    }

}
