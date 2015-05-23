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
import net.bither.BitherSetting;
import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.HDAccount;
import net.bither.bitherj.core.HDMAddress;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.utils.UnitUtil;
import net.bither.fonts.AwesomeDecorator;
import net.bither.fonts.AwesomeIcon;
import net.bither.fonts.MonospacedFont;
import net.bither.implbitherj.TxNotificationCenter;
import net.bither.network.ReplayManager;
import net.bither.network.ReplayTask;
import net.bither.preference.UserPreference;
import net.bither.utils.*;
import net.bither.viewsystem.action.WalletMouseListener;
import net.bither.viewsystem.base.FontSizer;
import net.bither.viewsystem.panels.WalletListPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

public class SingleWalletForm implements ActionListener, FocusListener, TxNotificationCenter.ITxListener, IAddressForm {

    private static final int WIDTH_DELTA = 4;
    private static final int MIN_WIDTH_SCROLLBAR_DELTA = 20;
    private static final double MINIMUM_WIDTH_SCALE_FACTOR = 0.5;

    private Address address;
    private static Color inactiveBackGroundColor;
    private static Color activeBackgroundColor = new Color(0x425e7a);
    private boolean selected = false;
    private JPanel panelMain;
    private JLabel labAmt;
    private JTextArea taAddress;
    private JLabel lblMoney;
    private JLabel lblType;
    private JLabel lblXRandom;
    private JLabel lblTx;
    private JPanel pnlBottom;

    private WalletListPanel walletListPanel;

    public SingleWalletForm(Address perWalletModelData, final WalletListPanel walletListPanel) {
        this.walletListPanel = walletListPanel;
        TxNotificationCenter.addTxListener(SingleWalletForm.this);
        this.address = perWalletModelData;
        selected = false;
        inactiveBackGroundColor = Color.WHITE;
        panelMain.setOpaque(true);
        panelMain.setFocusable(true);
        panelMain.setBackground(inactiveBackGroundColor);

        panelMain.applyComponentOrientation(ComponentOrientation.getOrientation(LocaliserUtils.getLocale()));

        setSelected(false);

        updateFromModel();

        panelMain.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent arg0) {
                if (walletListPanel != null) {
                    walletListPanel.selectAdjacentWallet(arg0, "SingleWalletPanel");
                }
                super.keyTyped(arg0);
            }
        });

        panelMain.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xc5c5c5)));
        // See if there is a waiting ReplayTask for this SingleWalletPanel and set up UI accordingly.
        ReplayTask replayTask = ReplayManager.INSTANCE.getWaitingReplayTask(perWalletModelData);
        if (replayTask != null) {
            String waitingText = LocaliserUtils.getString("singleWalletPanel.waiting.verb");

            // When busy occasionally the localiser fails to localise.
            if (!(waitingText.indexOf("singleWalletPanel.waiting.verb") > -1)) {
                setSyncMessage(waitingText, BitherSetting.NOT_RELEVANT_PERCENTAGE_COMPLETE);
            }

        }
        taAddress.setFont(MonospacedFont.fontWithSize(taAddress.getFont().getSize()));
        setContent();
        updateBalance();
        panelMain.addMouseListener(new WalletMouseListener(this.walletListPanel, SingleWalletForm.this));
        taAddress.addMouseListener(new WalletMouseListener(this.walletListPanel, SingleWalletForm.this));
        taAddress.setBorder(null);


    }

    private void setContent() {
        taAddress.setText(WalletUtils.formatHash(address.getAddress(), 4, 12));
        String iconPath;
        if (address instanceof HDAccount) {
            iconPath = "/images/address_type_hd.png";
        } else if (address instanceof HDMAddress) {
            iconPath = "/images/address_type_hdm.png";
        } else {
            if (address.hasPrivKey()) {
                iconPath = "/images/address_type_private.png";
            } else {
                iconPath = "/images/address_type_watchonly.png";
            }
        }
        ImageIcon icon = ImageLoader.createImageIcon(iconPath);
        icon.setImage(icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        lblType.setIcon(icon);
        icon = (ImageIcon) lblXRandom.getIcon();
        icon.setImage(icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        lblXRandom.setIcon(icon);
        lblXRandom.setVisible(address.isFromXRandom());

        icon = (ImageIcon) lblTx.getIcon();
        icon.setImage(icon.getImage().getScaledInstance(12, 12, Image.SCALE_SMOOTH));
        lblTx.setIcon(icon);
    }

    private void updateBalance() {
        ImageIcon icon = UnitUtilWrapper.BitcoinUnitWrapper.getWrapper(UnitUtil.BitcoinUnit.BTC).getBmpBlack();
        float width = icon.getIconWidth();
        float height = icon.getIconHeight();
        float scale = (float) labAmt.getFont().getSize() / height;
        icon.setImage(icon.getImage().getScaledInstance((int) (width * scale), (int) (height * scale), Image.SCALE_SMOOTH));
        labAmt.setIcon(icon);
        labAmt.setText(UnitUtil.formatValue(address.getBalance(), UnitUtil.BitcoinUnit.BTC));
        DecimalFormat formate = new DecimalFormat("0.00");
        double tickerPrice = 0;
        if (MarketUtil.getDefaultMarket() != null && MarketUtil.getDefaultMarket().getTicker() != null) {
            tickerPrice = MarketUtil.getDefaultMarket().getTicker().getDefaultExchangePrice();
        }
        lblMoney.setText(UserPreference.getInstance().getDefaultCurrency().getSymbol() + formate.format(tickerPrice * (address.getBalance() / 100000000.0)));
        lblTx.setText(String.valueOf(address.txCount()));
        updateColors();
    }


    public static int calculateNormalWidth(JComponent component) {
        Font font = FontSizer.INSTANCE.getAdjustedDefaultFont();
        FontMetrics fontMetrics = component.getFontMetrics(font);
        return fontMetrics.stringWidth(BitherSetting.EXAMPLE_MEDIUM_FIELD_TEXT) + WIDTH_DELTA;
    }

    private int calculateMinimumWidth(int normalWidth) {
        if (ComponentOrientation.LEFT_TO_RIGHT == ComponentOrientation.getOrientation(LocaliserUtils.getLocale())) {
            return (int) Math.max(0, normalWidth * MINIMUM_WIDTH_SCALE_FACTOR - MIN_WIDTH_SCROLLBAR_DELTA);
        } else {
            return normalWidth;
        }
    }


    public void setSelected(boolean selected) {
        this.selected = selected;

        updateColors();
    }

    private void updateColors() {
        ImageIcon icon;
        if (selected) {
            panelMain.setBackground(activeBackgroundColor);
            taAddress.setBackground(activeBackgroundColor);
            taAddress.setForeground(Color.WHITE);
            lblMoney.setForeground(Color.WHITE);
            labAmt.setForeground(Color.WHITE);
            pnlBottom.setBackground(new Color(0xccd1d7));
            icon = UnitUtilWrapper.BitcoinUnitWrapper.getWrapper(UnitUtil.BitcoinUnit.BTC).getBmpSlim();
        } else {
            panelMain.setBackground(inactiveBackGroundColor);
            taAddress.setBackground(inactiveBackGroundColor);
            taAddress.setForeground(Color.BLACK);
            lblMoney.setForeground(Color.BLACK);
            labAmt.setForeground(Color.BLACK);
            pnlBottom.setBackground(new Color(0xeeeeee));
            icon = UnitUtilWrapper.BitcoinUnitWrapper.getWrapper(UnitUtil.BitcoinUnit.BTC).getBmpBlack();
        }
        float width = icon.getIconWidth();
        float height = icon.getIconHeight();
        float scale = (float) labAmt.getFont().getSize() / height;
        icon.setImage(icon.getImage().getScaledInstance((int) (width * scale), (int) (height * scale), Image.SCALE_SMOOTH));
        labAmt.setIcon(icon);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        saveChanges();
        panelMain.requestFocusInWindow();
    }

    @Override
    public Address getPerWalletModelData() {
        return address;
    }

    @Override
    public String getOnlyName() {
        return address.getAddress();
    }

    @Override
    public void focusGained(FocusEvent arg0) {
        if (!(arg0.getSource() instanceof JTextField)) {
            panelMain.requestFocusInWindow();
        }

    }

    @Override
    public void focusLost(FocusEvent arg0) {
        saveChanges();
    }

    private void saveChanges() {
        String titleText = LocaliserUtils.getString("bitherframe_title");
        Bither.getMainFrame().setTitle(titleText);
    }

    /**
     * Update any UI elements from the model (hint that data has changed).
     */
    public void updateFromModel() {
        panelMain.invalidate();
        panelMain.revalidate();
        panelMain.repaint();
    }

    public void setSyncMessage(String message, double syncPercent) {
        if (message == null) {
            return;
        }
    }

    public JPanel getPanel() {
        return panelMain;
    }

    @Override
    public void notificatTx(String address, Tx tx, Tx.TxNotificationType txNotificationType, long deltaBalance) {
        updateBalance();
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
        panelMain.setLayout(new GridLayoutManager(5, 6, new Insets(0, 0, 0, 0), -1, -1));
        panelMain.setAutoscrolls(false);
        panelMain.setBackground(new Color(-1));
        panelMain.setEnabled(true);
        panelMain.setMinimumSize(new Dimension(60, 130));
        panelMain.setPreferredSize(new Dimension(60, 130));
        final Spacer spacer1 = new Spacer();
        panelMain.add(spacer1, new GridConstraints(1, 2, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(0, 11), null, 0, false));
        taAddress = new JTextArea();
        taAddress.setBackground(new Color(-1));
        taAddress.setEditable(false);
        taAddress.setFont(new Font("Monospaced", taAddress.getFont().getStyle(), taAddress.getFont().getSize()));
        taAddress.setText("");
        panelMain.add(taAddress, new GridConstraints(1, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(100, 50), null, 0, false));
        labAmt = new JLabel();
        labAmt.setBackground(new Color(-1));
        labAmt.setText("Label");
        panelMain.add(labAmt, new GridConstraints(1, 3, 1, 2, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        lblMoney = new JLabel();
        lblMoney.setBackground(new Color(-1));
        lblMoney.setText("Label");
        panelMain.add(lblMoney, new GridConstraints(2, 3, 1, 2, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panelMain.add(spacer2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(10, -1), new Dimension(10, -1), new Dimension(10, -1), 0, false));
        final Spacer spacer3 = new Spacer();
        panelMain.add(spacer3, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(10, -1), new Dimension(10, -1), new Dimension(10, -1), 0, false));
        pnlBottom = new JPanel();
        pnlBottom.setLayout(new GridLayoutManager(1, 6, new Insets(0, 0, 0, 0), -1, -1));
        panelMain.add(pnlBottom, new GridConstraints(4, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        lblTx = new JLabel();
        lblTx.setForeground(new Color(-6710887));
        lblTx.setIcon(new ImageIcon(getClass().getResource("/images/transaction_icon_small.png")));
        lblTx.setText("");
        lblTx.setVerticalAlignment(0);
        lblTx.setVerticalTextPosition(0);
        pnlBottom.add(lblTx, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 16), new Dimension(-1, 16), new Dimension(-1, 16), 0, false));
        lblType = new JLabel();
        lblType.setBackground(new Color(-1));
        lblType.setHorizontalAlignment(0);
        lblType.setHorizontalTextPosition(0);
        lblType.setIcon(new ImageIcon(getClass().getResource("/images/address_type_private.png")));
        lblType.setText("");
        lblType.setVerticalAlignment(0);
        lblType.setVerticalTextPosition(1);
        pnlBottom.add(lblType, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(20, 30), new Dimension(20, 30), new Dimension(20, 30), 0, false));
        lblXRandom = new JLabel();
        lblXRandom.setHorizontalAlignment(4);
        lblXRandom.setHorizontalTextPosition(10);
        lblXRandom.setIcon(new ImageIcon(getClass().getResource("/images/xrandom_address_label_normal.png")));
        lblXRandom.setText("");
        lblXRandom.setVerticalAlignment(0);
        lblXRandom.setVerticalTextPosition(1);
        pnlBottom.add(lblXRandom, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(24, 30), new Dimension(24, 30), new Dimension(24, 30), 0, false));
        final Spacer spacer4 = new Spacer();
        pnlBottom.add(spacer4, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(10, -1), new Dimension(10, -1), new Dimension(10, -1), 0, false));
        final Spacer spacer5 = new Spacer();
        pnlBottom.add(spacer5, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        pnlBottom.add(spacer6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(10, -1), new Dimension(10, -1), new Dimension(10, -1), 0, false));
        final Spacer spacer7 = new Spacer();
        panelMain.add(spacer7, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer8 = new Spacer();
        panelMain.add(spacer8, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelMain;
    }
}
