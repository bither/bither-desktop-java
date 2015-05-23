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

package net.bither.viewsystem.dialogs;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.crypto.ECKey;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.crypto.hd.DeterministicKey;
import net.bither.bitherj.qrcode.QRCodeTxTransport;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.UnitUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.qrcode.DisplayBitherQRCodePanel;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.WalletUtils;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.froms.PasswordPanel;
import net.bither.viewsystem.listener.IDialogPasswordListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SignTxDialg extends BitherDialog implements IDialogPasswordListener {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel panelTx;
    private JLabel labFrom;
    private JLabel labPayTo;
    private JLabel labPayAmt;
    private JLabel labFee;
    private JLabel labChangeAmt;
    private JLabel labChangeTo;
    private JLabel labChangeToValue;
    private JLabel labChangeAmtValue;
    private QRCodeTxTransport qrCodeTransport;
    private DialogProgress dp;

    public SignTxDialg(QRCodeTxTransport qrCodeTransport) {

        this.qrCodeTransport = qrCodeTransport;
        Buttons.modifCanelButton(buttonCancel);
        Buttons.modifButton(buttonOK);
        setContentPane(contentPane);
        setModal(true);

        initDialog();

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        showTransaction();

    }

    private void onOK() {
        PasswordPanel dialogPassword = new PasswordPanel(this);
        dialogPassword.showPanel();

    }

    @Override
    public void onPasswordEntered(final SecureCharSequence password) {
        dp = new DialogProgress();
        Thread thread = new Thread() {
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dp.pack();
                        dp.setVisible(true);
                    }
                });
                List<String> strings = new ArrayList<String>();
                if (qrCodeTransport.getHdmIndex() >= 0) {
                    if (!AddressManager.getInstance().hasHDMKeychain()) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                dp.dispose();
                                new MessageDialog(LocaliserUtils.getString("hdm_send_with_cold_no_requested_seed"));

                            }
                        });
                        password.wipe();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                dp.dispose();
                            }
                        });
                        return;
                    }
                    try {
                        DeterministicKey key = AddressManager.getInstance().getHdmKeychain()
                                .getExternalKey(qrCodeTransport.getHdmIndex(), password);

                        List<String> hashes = qrCodeTransport.getHashList();
                        strings = new ArrayList<String>();
                        for (String hash : hashes) {
                            ECKey.ECDSASignature signed = key.sign(Utils.hexStringToByteArray
                                    (hash));
                            strings.add(Utils.bytesToHexString(signed.encodeToDER()));
                        }
                        key.wipe();
                    } catch (Exception e) {
                        e.printStackTrace();

                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                dp.dispose();
                                new MessageDialog(LocaliserUtils.getString("hdm_send_with_cold_no_requested_seed")).showMsg();
                            }
                        });
                        password.wipe();
                        return;
                    }
                } else {
                    Address address = WalletUtils.findPrivateKey(qrCodeTransport.getMyAddress());
                    strings = address.signStrHashes(qrCodeTransport.getHashList(), password);
                }
                password.wipe();
                String result = "";
                for (int i = 0;
                     i < strings.size();
                     i++) {
                    if (i < strings.size() - 1) {
                        result = result + strings.get(i) + QRCodeUtil.QR_CODE_SPLIT;
                    } else {
                        result = result + strings.get(i);
                    }
                }
                final String r = result;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dp.dispose();
                        dispose();
                        DisplayBitherQRCodePanel displayBitherQRCodePanle = new DisplayBitherQRCodePanel(r);
                        displayBitherQRCodePanle.showPanel();

                    }
                });

            }

            ;
        };
        thread.start();

    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    private void showTransaction() {
        if (Utils.isEmpty(qrCodeTransport.getChangeAddress()) || qrCodeTransport.getChangeAmt() == 0) {
            labChangeAmt.setVisible(false);
            labChangeAmtValue.setVisible(false);
            labChangeTo.setVisible(false);
            labChangeToValue.setVisible(false);
        } else {
            labChangeToValue.setText(qrCodeTransport.getChangeAddress());
            labChangeAmtValue.setText(UnitUtil.formatValue(qrCodeTransport.getChangeAmt(), UnitUtil.BitcoinUnit.BTC));
        }
        panelTx.setVisible(true);
        labFrom.setText(qrCodeTransport.getMyAddress());
        labPayTo.setText(qrCodeTransport.getToAddress());
        labFee.setText(UnitUtil.formatValue(qrCodeTransport.getFee(), UnitUtil.BitcoinUnit.BTC));
        labPayAmt.setText(UnitUtil.formatValue(qrCodeTransport.getTo(), UnitUtil.BitcoinUnit.BTC));

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
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.setBackground(new Color(-328966));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setOpaque(false);
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel2.setOpaque(false);
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        this.$$$loadButtonText$$$(buttonOK, ResourceBundle.getBundle("viewer").getString("sign_transaction"));
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        this.$$$loadButtonText$$$(buttonCancel, ResourceBundle.getBundle("viewer").getString("cancel"));
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.setOpaque(false);
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panelTx = new JPanel();
        panelTx.setLayout(new GridLayoutManager(6, 4, new Insets(0, 0, 0, 0), -1, -1));
        panelTx.setOpaque(false);
        panel3.add(panelTx, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$(label1, ResourceBundle.getBundle("viewer").getString("send_coins_fragment_sending_address_label"));
        panelTx.add(label1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panelTx.add(spacer2, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        labFrom = new JLabel();
        labFrom.setText("Label");
        panelTx.add(labFrom, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panelTx.add(spacer3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        this.$$$loadLabelText$$$(label2, ResourceBundle.getBundle("viewer").getString("send_coins_fragment_receiving_address_label"));
        panelTx.add(label2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labPayTo = new JLabel();
        labPayTo.setText("Label");
        panelTx.add(labPayTo, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        this.$$$loadLabelText$$$(label3, ResourceBundle.getBundle("viewer").getString("send_coins_fragment_amount_label"));
        panelTx.add(label3, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labPayAmt = new JLabel();
        labPayAmt.setText("Label");
        panelTx.add(labPayAmt, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        this.$$$loadLabelText$$$(label4, ResourceBundle.getBundle("viewer").getString("send_confirm_fee"));
        panelTx.add(label4, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labFee = new JLabel();
        labFee.setText("Label");
        panelTx.add(labFee, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labChangeTo = new JLabel();
        this.$$$loadLabelText$$$(labChangeTo, ResourceBundle.getBundle("viewer").getString("send_confirm_change_to_label"));
        panelTx.add(labChangeTo, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labChangeToValue = new JLabel();
        labChangeToValue.setText("Label");
        panelTx.add(labChangeToValue, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labChangeAmt = new JLabel();
        this.$$$loadLabelText$$$(labChangeAmt, ResourceBundle.getBundle("viewer").getString("sign_transaction_change_amount_label"));
        panelTx.add(labChangeAmt, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labChangeAmtValue = new JLabel();
        labChangeAmtValue.setText("Label");
        panelTx.add(labChangeAmtValue, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadLabelText$$$(JLabel component, String text) {
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
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
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
        return contentPane;
    }
}
