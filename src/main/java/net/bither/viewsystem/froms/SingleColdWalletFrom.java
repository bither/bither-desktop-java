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
import net.bither.bitherj.core.Address;
import net.bither.bitherj.utils.UnitUtil;
import net.bither.fonts.MonospacedFont;
import net.bither.utils.ImageLoader;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.UnitUtilWrapper;
import net.bither.utils.WalletUtils;
import net.bither.viewsystem.action.WalletMouseListener;
import net.bither.viewsystem.base.ColorAndFontConstants;
import net.bither.viewsystem.panels.WalletListPanel;
import net.bither.viewsystem.themes.Themes;

import javax.swing.*;
import java.awt.*;

public class SingleColdWalletFrom implements IAddressForm {


    private static Color inactiveBackGroundColor;
    private static Color activeBackgroundColor = new Color(0x425e7a);
    private static final int COLOR_DELTA = 24;

    private static final int MIN_WIDTH_SCROLLBAR_DELTA = 20;
    private static final double MINIMUM_WIDTH_SCALE_FACTOR = 0.5;


    private Address perWalletModelData;
    private WalletListPanel walletListPanel;

    private boolean selected = false;


    private JPanel panelMain;
    private JPanel pnlBottom;

    private JLabel lblType;
    private JLabel lblXRandom;
    private JTextArea taAddress;


    public SingleColdWalletFrom(Address perWalletModelData, final WalletListPanel walletListPanel) {
        this.walletListPanel = walletListPanel;
        this.perWalletModelData = perWalletModelData;
        inactiveBackGroundColor = Color.WHITE;
        selected = false;
        panelMain.setOpaque(true);
        panelMain.setFocusable(true);
        panelMain.setBackground(inactiveBackGroundColor);

        if (ColorAndFontConstants.isInverse()) {
            inactiveBackGroundColor = new Color(Math.min(255, Themes.currentTheme.detailPanelBackground().getRed() + 2 * COLOR_DELTA), Math.min(255,
                    Themes.currentTheme.detailPanelBackground().getBlue() + 2 * COLOR_DELTA), Math.min(255, Themes.currentTheme.detailPanelBackground().getGreen() + 2 * COLOR_DELTA));
        } else {
            inactiveBackGroundColor = new Color(Math.max(0, Themes.currentTheme.detailPanelBackground().getRed() - COLOR_DELTA), Math.max(0,
                    Themes.currentTheme.detailPanelBackground().getBlue() - COLOR_DELTA), Math.max(0, Themes.currentTheme.detailPanelBackground().getGreen() - COLOR_DELTA));
        }
        panelMain.applyComponentOrientation(ComponentOrientation.getOrientation(LocaliserUtils.getLocale()));
        taAddress.setFont(MonospacedFont.fontWithSize(taAddress.getFont().getSize()));
        updateFromModel();
        panelMain.addMouseListener(new WalletMouseListener(this.walletListPanel, SingleColdWalletFrom.this));
        taAddress.addMouseListener(new WalletMouseListener(this.walletListPanel, SingleColdWalletFrom.this));
        setSelected(false);
        setContent();
        taAddress.setBorder(null);

    }

    /**
     * Update any UI elements from the model (hint that data has changed).
     */
    public void updateFromModel() {
        if (ColorAndFontConstants.isInverse()) {
            inactiveBackGroundColor = new Color(Math.min(255, Themes.currentTheme.detailPanelBackground().getRed() + 2 * COLOR_DELTA), Math.min(255,
                    Themes.currentTheme.detailPanelBackground().getBlue() + 2 * COLOR_DELTA), Math.min(255, Themes.currentTheme.detailPanelBackground().getGreen() + 2 * COLOR_DELTA));
        } else {
            inactiveBackGroundColor = new Color(Math.max(0, Themes.currentTheme.detailPanelBackground().getRed() - COLOR_DELTA), Math.max(0,
                    Themes.currentTheme.detailPanelBackground().getBlue() - COLOR_DELTA), Math.max(0, Themes.currentTheme.detailPanelBackground().getGreen() - COLOR_DELTA));
        }
        panelMain.invalidate();
        panelMain.revalidate();
        panelMain.repaint();
    }

    private void updateColors() {
        ImageIcon icon;
        if (selected) {
            panelMain.setBackground(activeBackgroundColor);
            taAddress.setBackground(activeBackgroundColor);
            taAddress.setForeground(Color.WHITE);

            pnlBottom.setBackground(new Color(0xccd1d7));
            icon = UnitUtilWrapper.BitcoinUnitWrapper.getWrapper(UnitUtil.BitcoinUnit.BTC).getBmpSlim();
        } else {
            panelMain.setBackground(inactiveBackGroundColor);
            taAddress.setBackground(inactiveBackGroundColor);
            taAddress.setForeground(Color.BLACK);

            pnlBottom.setBackground(new Color(0xeeeeee));
            icon = UnitUtilWrapper.BitcoinUnitWrapper.getWrapper(UnitUtil.BitcoinUnit.BTC).getBmpBlack();
        }
        float width = icon.getIconWidth();
        float height = icon.getIconHeight();
        float scale = (float) 1.0f;
        icon.setImage(icon.getImage().getScaledInstance((int) (width * scale), (int) (height * scale), Image.SCALE_SMOOTH));

    }


    private void setContent() {
        taAddress.setText(WalletUtils.formatHash(perWalletModelData.getAddress(), 4, 12));
        String iconPath = "/images/address_type_private.png";
        ImageIcon icon = ImageLoader.createImageIcon(iconPath);
        icon.setImage(icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        lblType.setIcon(icon);
        icon = (ImageIcon) lblXRandom.getIcon();
        icon.setImage(icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        lblXRandom.setIcon(icon);
        lblXRandom.setVisible(perWalletModelData.isFromXRandom());


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


    public JPanel getPanel() {
        return panelMain;
    }

    @Override
    public Address getPerWalletModelData() {
        return perWalletModelData;
    }

    @Override
    public String getOnlyName() {
        return perWalletModelData.getAddress();
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
        panelMain.setLayout(new GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
        panelMain.setBackground(new Color(-1));
        panelMain.setMinimumSize(new Dimension(60, 130));
        panelMain.setPreferredSize(new Dimension(60, 130));
        pnlBottom = new JPanel();
        pnlBottom.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        panelMain.add(pnlBottom, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        lblType = new JLabel();
        lblType.setBackground(new Color(-1));
        lblType.setHorizontalAlignment(0);
        lblType.setHorizontalTextPosition(0);
        lblType.setIcon(new ImageIcon(getClass().getResource("/images/address_type_private.png")));
        lblType.setText("");
        lblType.setVerticalAlignment(0);
        lblType.setVerticalTextPosition(1);
        pnlBottom.add(lblType, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(20, 30), new Dimension(20, 30), new Dimension(20, 30), 0, false));
        lblXRandom = new JLabel();
        lblXRandom.setHorizontalAlignment(4);
        lblXRandom.setHorizontalTextPosition(10);
        lblXRandom.setIcon(new ImageIcon(getClass().getResource("/images/xrandom_address_label_normal.png")));
        lblXRandom.setText("");
        lblXRandom.setVerticalAlignment(0);
        lblXRandom.setVerticalTextPosition(1);
        pnlBottom.add(lblXRandom, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(24, 30), new Dimension(24, 30), new Dimension(24, 30), 0, false));
        final Spacer spacer1 = new Spacer();
        pnlBottom.add(spacer1, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(10, -1), new Dimension(10, -1), new Dimension(10, -1), 0, false));
        final Spacer spacer2 = new Spacer();
        pnlBottom.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        pnlBottom.add(spacer3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(10, -1), new Dimension(10, -1), new Dimension(10, -1), 0, false));
        taAddress = new JTextArea();
        taAddress.setBackground(new Color(-1));
        taAddress.setEditable(false);
        taAddress.setFont(new Font("Monospaced", taAddress.getFont().getStyle(), taAddress.getFont().getSize()));
        taAddress.setText("");
        panelMain.add(taAddress, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(100, 50), null, 0, false));
        final Spacer spacer4 = new Spacer();
        panelMain.add(spacer4, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panelMain.add(spacer5, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        panelMain.add(spacer6, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        panelMain.add(spacer7, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(10, -1), new Dimension(10, -1), new Dimension(10, -1), 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelMain;
    }
}
