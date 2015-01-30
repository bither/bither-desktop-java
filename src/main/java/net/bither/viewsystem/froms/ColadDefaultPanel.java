package net.bither.viewsystem.froms;

import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.qrcode.QRCodeEnodeUtil;
import net.bither.bitherj.qrcode.QRCodeTxTransport;
import net.bither.bitherj.utils.PrivateKeyUtil;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.qrcode.DisplayBitherQRCodePanel;
import net.bither.qrcode.IReadQRCode;
import net.bither.qrcode.IScanQRCode;
import net.bither.qrcode.SelectTransportQRCodePanel;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.*;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.dialogs.PasswordDialog;
import net.bither.viewsystem.dialogs.SignTxDialg;
import net.bither.viewsystem.listener.IDialogPasswordListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ColadDefaultPanel implements Viewable, IScanQRCode {

    private JPanel panelMain;
    private JButton btnSignTransaction;
    private JButton btnWatchOnlyQRCode;
    private JButton btnBitherColdWallet;
    private JButton btnAddress;
    private JButton btnHDMCold;

    public ColadDefaultPanel() {
        Action signActionListener = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (AddressManager.getInstance().getPrivKeyAddresses().size() == 0) {
                    new MessageDialog(LocaliserUtils.getString("private.key.is.empty")).showMsg();
                } else {
                    toSignTx();
                }
            }
        };

        Action watchOnlyActionListener = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (AddressManager.getInstance().getPrivKeyAddresses().size() == 0) {
                    new MessageDialog(LocaliserUtils.getString("private.key.is.empty")).showMsg();
                } else {
                    String content = QRCodeEnodeUtil.getPublicKeyStrOfPrivateKey();
                    DisplayBitherQRCodePanel displayBitherQRCodePanle = new DisplayBitherQRCodePanel(content);
                    displayBitherQRCodePanle.showPanel();
                }

            }

        };
        Action bitherColdeActionListener = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (AddressManager.getInstance().getPrivKeyAddresses().size() == 0) {
                    new MessageDialog(LocaliserUtils.getString("private.key.is.empty")).showMsg();
                } else {
                    PasswordDialog passwordDialog = new PasswordDialog(new IDialogPasswordListener() {
                        @Override
                        public void onPasswordEntered(SecureCharSequence password) {

                            String content = PrivateKeyUtil.getEncryptPrivateKeyStringFromAllAddresses();
                            DisplayBitherQRCodePanel displayBitherQRCodePanle = new DisplayBitherQRCodePanel(content);
                            displayBitherQRCodePanle.showPanel();

                        }
                    });

                    passwordDialog.pack();
                    passwordDialog.setVisible(true);
                }

            }
        };
        Action addressActionListener = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddressDetailPanel addressDetailPanel = new AddressDetailPanel();
                addressDetailPanel.showPanel();

            }
        };
        Action hdmColdAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };
        btnSignTransaction = Buttons.addWizardButton(signActionListener, MessageKey.SIGN_TX, AwesomeIcon.PENCIL);
        btnWatchOnlyQRCode = Buttons.addWizardButton(watchOnlyActionListener, MessageKey.WATCH_ONLY_QRCODE, AwesomeIcon.FA_EYE);
        btnBitherColdWallet = Buttons.addWizardButton(bitherColdeActionListener, MessageKey.CLONE_QRCODE, AwesomeIcon.REPLY_ALL);
        btnAddress = Buttons.addWizardButton(addressActionListener, MessageKey.ADDRESS_DETAIL, AwesomeIcon.FA_SEARCH_PLUS);
        btnHDMCold = Buttons.addWizardButton(hdmColdAction, MessageKey.HDM, AwesomeIcon.FA_RECYCLE);
        if (AddressManager.getInstance().getAllAddresses().size() == 0) {
            btnAddress.setVisible(false);
        }
        panelMain = Panels.newPanel();
        panelMain.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "20[][][][][]10", // Column constraints
                "[][80][][30][30][20]" // Row constraints
        ));
        panelMain.add(btnAddress);
        panelMain.add(btnWatchOnlyQRCode);
        panelMain.add(btnBitherColdWallet);
        panelMain.add(btnSignTransaction);
        panelMain.add(btnHDMCold);
    }


    private void toSignTx() {
        SelectTransportQRCodePanel selectTransportQRCodePanel = new SelectTransportQRCodePanel(this);
        selectTransportQRCodePanel.showPanel();
    }

    @Override
    public void handleResult(String result, IReadQRCode readQRCode) {

        QRCodeTxTransport qrCodeTransport = QRCodeTxTransport.formatQRCodeTransport(result);
        if (qrCodeTransport != null) {
            Panels.hideLightBoxIfPresent();
            readQRCode.close();
            SignTxDialg signTxDialg = new SignTxDialg(qrCodeTransport);
            signTxDialg.pack();
            signTxDialg.setVisible(true);

        } else {
            readQRCode.reTry("qrcode error");
        }

    }

    @Override
    public void displayView(DisplayHint displayHint) {
        if (DisplayHint.WALLET_TRANSACTIONS_HAVE_CHANGED == displayHint) {
            return;
        }

    }


    @Override
    public ViewEnum getViewId() {
        return ViewEnum.COLD_WALLET_VIEW;
    }

    public JPanel getPanel() {
        return panelMain;
    }
}
