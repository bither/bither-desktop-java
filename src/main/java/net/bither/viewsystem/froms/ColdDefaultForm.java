package net.bither.viewsystem.froms;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

public class ColdDefaultForm implements Viewable, IScanQRCode {
    private JPanel panelMain;
    private JButton btnSignTransaction;
    private JButton btnWatchOnlyQRCode;
    private JButton btnBitherColdWallet;
    private JButton btnAddress;

    public ColdDefaultForm() {
        btnSignTransaction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (AddressManager.getInstance().getPrivKeyAddresses().size() == 0) {
                    new MessageDialog(LocaliserUtils.getString("private.key.is.empty")).showMsg();
                } else {
                    toSignTx();
                }
            }
        });

        btnWatchOnlyQRCode.addActionListener(new ActionListener() {
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

        });
        btnBitherColdWallet.addActionListener(new ActionListener() {
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
        });
        btnAddress.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddressDetailPanel addressDetailPanel = new AddressDetailPanel();
                addressDetailPanel.showPanel();

            }
        });
        Buttons.modiyWizardButton(btnSignTransaction, MessageKey.SIGN_TX, AwesomeIcon.PENCIL);
        Buttons.modiyWizardButton(btnWatchOnlyQRCode, MessageKey.WATCH_ONLY_QRCODE, AwesomeIcon.FA_EYE);
        Buttons.modiyWizardButton(btnBitherColdWallet, MessageKey.CLONE_QRCODE, AwesomeIcon.REPLY_ALL);
        Buttons.modiyWizardButton(btnAddress, MessageKey.ADDRESS_DETAIL, AwesomeIcon.FA_SEARCH_PLUS);
        if (AddressManager.getInstance().getAllAddresses().size() == 0) {
            btnAddress.setVisible(false);
        }
    }


    private void toSignTx() {
        SelectTransportQRCodePanel selectTransportQRCodePanel = new SelectTransportQRCodePanel(this);
        selectTransportQRCodePanel.showPanel();
    }


    @Override
    public void handleResult(String result, IReadQRCode readQRCode) {

        QRCodeTxTransport qrCodeTransport = QRCodeEnodeUtil.formatQRCodeTransport(result);
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


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panelMain = new JPanel();
        panelMain.setLayout(new GridLayoutManager(3, 6, new Insets(0, 0, 0, 0), -1, -1));
        panelMain.setBackground(new Color(-1));
        panelMain.setForeground(new Color(-4473925));
        panelMain.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20), null));
        final Spacer spacer1 = new Spacer();
        panelMain.add(spacer1, new GridConstraints(2, 2, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        btnSignTransaction = new JButton();
        this.$$$loadButtonText$$$(btnSignTransaction, ResourceBundle.getBundle("viewer").getString("sign.transaction"));
        panelMain.add(btnSignTransaction, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnBitherColdWallet = new JButton();
        this.$$$loadButtonText$$$(btnBitherColdWallet, ResourceBundle.getBundle("viewer").getString("clone.from.button"));
        panelMain.add(btnBitherColdWallet, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnWatchOnlyQRCode = new JButton();
        this.$$$loadButtonText$$$(btnWatchOnlyQRCode, ResourceBundle.getBundle("viewer").getString("qr.code.for.all.addresses"));
        panelMain.add(btnWatchOnlyQRCode, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnAddress = new JButton();
        btnAddress.setText("Button");
        panelMain.add(btnAddress, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelMain;
    }
}
