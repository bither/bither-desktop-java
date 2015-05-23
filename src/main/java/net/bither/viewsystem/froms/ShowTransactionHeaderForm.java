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

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import net.bither.Bither;
import net.bither.bitherj.core.HDAccount;
import net.bither.bitherj.core.HDMAddress;
import net.bither.bitherj.utils.UnitUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.fonts.MonospacedFont;
import net.bither.model.Ticker;
import net.bither.qrcode.DisplayQRCodePanle;
import net.bither.qrcode.QRCodeGenerator;
import net.bither.utils.MarketUtil;
import net.bither.utils.WalletUtils;
import net.bither.viewsystem.action.CopyAction;
import net.bither.viewsystem.base.Buttons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;

public class ShowTransactionHeaderForm implements CopyAction.ICopy {
    private JPanel panelMain;
    private JButton btnSend;
    private JButton btnAmt;
    private JButton btnQRCode;

    private JButton btnCopy;
    private JTextArea taAddress;
    private boolean isShowBtc = true;

    public ShowTransactionHeaderForm() {
        initUI();
        updateUI();
    }

    public void setVisible(boolean visible) {
        panelMain.setVisible(visible);
    }


    private void initUI() {

        Buttons.modifButton(btnAmt);
        Buttons.modifButton(btnQRCode);

        Buttons.modifSendButton(btnSend);
        Buttons.modifCopyButton(btnCopy);


        btnAmt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                isShowBtc = !isShowBtc;
                showAmt();
                panelMain.requestFocusInWindow();

            }
        });

        if (Bither.getActionAddress() != null) {

            btnQRCode.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    DisplayQRCodePanle displayQRCodePanle = new DisplayQRCodePanle(Bither.getActionAddress().getAddress());
                    displayQRCodePanle.showPanel();

                }
            });
        }

        btnSend.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (Bither.getActionAddress() != null) {
                    if (Bither.getActionAddress() instanceof HDAccount) {
                        HDAccountSendPanel hdAccountSendPanel = new HDAccountSendPanel();
                        hdAccountSendPanel.showPanel();
                    } else if (Bither.getActionAddress() instanceof HDMAddress) {
                        SendHDMBitcoinPanel hdmBitcoinPanel = new SendHDMBitcoinPanel();
                        hdmBitcoinPanel.showPanel();
                    } else if (Bither.getActionAddress().hasPrivKey()) {
                        SendBitcoinPanel sendBitcoinDialog = new SendBitcoinPanel();
                        sendBitcoinDialog.showPanel();

                    } else {
                        UnSignTxPanel unSignTxPanel = new UnSignTxPanel();
                        unSignTxPanel.showPanel();
                    }
                    panelMain.requestFocusInWindow();
                }


            }
        });

        CopyAction copyAddressAction =
                new CopyAction(ShowTransactionHeaderForm.this);

        btnCopy.addActionListener(copyAddressAction);

        taAddress.setBorder(null);

        taAddress.setFont(MonospacedFont.fontWithSize(taAddress.getFont().getSize()));

    }

    private void showAmt() {
        if (Bither.getActionAddress() == null) {
            return;
        }
        if (isShowBtc) {
            btnAmt.setText(UnitUtil.formatValue(Bither.getActionAddress().getBalance(), UnitUtil.BitcoinUnit.BTC));
        } else {
            Ticker ticker = MarketUtil.getTickerOfDefaultMarket();
            double amt = ((double) Bither.getActionAddress().getBalance()) * ticker.getDefaultExchangePrice() / Math.pow(10, 8);
            btnAmt.setText(Utils.formatDoubleToMoneyString(amt));
        }

    }

    public void updateUI() {
        final String address;
        showAmt();
        if (Bither.getActionAddress() != null) {
            address = Bither.getActionAddress().getAddress();
        } else {
            address = "";
        }
        taAddress.setText(WalletUtils.formatHash(address, 4, 12));
        btnQRCode.setText("");
        if (Bither.getActionAddress() != null && Bither.getActionAddress().getBalance() == 0) {
            btnSend.setEnabled(false);
        } else {
            btnSend.setEnabled(true);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                showQRCode(address);
            }
        }).start();


    }

    private void showQRCode(final String address) {
        try {
            BufferedImage image = QRCodeGenerator.generateQRcode(address, null, null);
            final ImageIcon icon;
            if (image != null) {
                icon = new ImageIcon(image);
            } else {
                icon = new ImageIcon();
            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (btnQRCode != null) {
                        btnQRCode.setIcon(icon);
                    }
                }
            });

        } catch (RuntimeException re) {
            re.printStackTrace();
        }

    }

    public JPanel getPanel() {
        return panelMain;
    }

    @Override
    public String getCopyString() {
        return Bither.getActionAddress().getAddress();
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
        panelMain.setLayout(new GridLayoutManager(3, 7, new Insets(0, 0, 0, 0), -1, -1));
        panelMain.setBackground(new Color(-1));
        panelMain.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20), null));
        final Spacer spacer1 = new Spacer();
        panelMain.add(spacer1, new GridConstraints(2, 0, 1, 7, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$(label1, ResourceBundle.getBundle("viewer").getString("address_balance"));
        panelMain.add(label1, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, 1, 1, null, null, null, 0, false));
        taAddress = new JTextArea();
        taAddress.setEditable(false);
        taAddress.setFont(new Font("Monospaced", taAddress.getFont().getStyle(), 18));
        panelMain.add(taAddress, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, 50), null, 0, false));
        final Spacer spacer2 = new Spacer();
        panelMain.add(spacer2, new GridConstraints(0, 4, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        btnAmt = new JButton();
        btnAmt.setText("Button");
        panelMain.add(btnAmt, new GridConstraints(1, 6, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, 1, 1, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setOpaque(false);
        panelMain.add(panel1, new GridConstraints(0, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnCopy = new JButton();
        btnCopy.setText("Button");
        panel1.add(btnCopy, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnSend = new JButton();
        this.$$$loadButtonText$$$(btnSend, ResourceBundle.getBundle("viewer").getString("send_bitcoin_action_text"));
        panel1.add(btnSend, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_NONE, 1, 1, null, null, null, 0, false));
        btnQRCode = new JButton();
        btnQRCode.setHorizontalTextPosition(0);
        btnQRCode.setText("Button");
        panelMain.add(btnQRCode, new GridConstraints(0, 5, 1, 2, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        return panelMain;
    }
}
