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
import net.bither.utils.ImageLoader;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.UnitUtilWrapper;
import net.bither.viewsystem.action.WalletMouseListener;
import net.bither.viewsystem.base.ColorAndFontConstants;
import net.bither.viewsystem.panels.WalletListPanel;
import net.bither.viewsystem.themes.Themes;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * Created by nn on 15/3/4.
 */
public class HDMColdAccountForm implements IAddressForm {

    public static final String HDM_COLD_ACCOUNT = "hdm_cold_account";

    private static Color inactiveBackGroundColor;
    private static Color activeBackgroundColor = new Color(0x425e7a);
    private static final int COLOR_DELTA = 24;

    private WalletListPanel walletListPanel;

    private boolean selected = false;


    private JPanel panelMain;
    private JPanel pnlBottom;

    private JLabel lblType;


    public HDMColdAccountForm(final WalletListPanel walletListPanel) {
        this.walletListPanel = walletListPanel;

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

        updateFromModel();
        panelMain.addMouseListener(new WalletMouseListener(this.walletListPanel, HDMColdAccountForm.this));

        setSelected(false);
        setContent();


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


            pnlBottom.setBackground(new Color(0xccd1d7));
            icon = UnitUtilWrapper.BitcoinUnitWrapper.getWrapper(UnitUtil.BitcoinUnit.BTC).getBmpSlim();
        } else {
            panelMain.setBackground(inactiveBackGroundColor);

            pnlBottom.setBackground(new Color(0xeeeeee));
            icon = UnitUtilWrapper.BitcoinUnitWrapper.getWrapper(UnitUtil.BitcoinUnit.BTC).getBmpBlack();
        }
        float width = icon.getIconWidth();
        float height = icon.getIconHeight();
        float scale = (float) 1.0f;
        icon.setImage(icon.getImage().getScaledInstance((int) (width * scale), (int) (height * scale), Image.SCALE_SMOOTH));

    }


    private void setContent() {
        String iconPath = "/images/address_type_hdm.png";
        ImageIcon icon = ImageLoader.createImageIcon(iconPath);
        icon.setImage(icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        lblType.setIcon(icon);


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
        return null;
    }

    @Override
    public String getOnlyName() {
        return HDM_COLD_ACCOUNT;
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
        panelMain.setLayout(new GridLayoutManager(4, 4, new Insets(0, 0, 0, 0), -1, -1));
        final Spacer spacer1 = new Spacer();
        panelMain.add(spacer1, new GridConstraints(1, 3, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$(label1, ResourceBundle.getBundle("viewer").getString("hdm_account_cold_address_list_label"));
        panelMain.add(label1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panelMain.add(spacer2, new GridConstraints(1, 2, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panelMain.add(spacer3, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 10), new Dimension(-1, 10), new Dimension(-1, 10), 0, false));
        final Spacer spacer4 = new Spacer();
        panelMain.add(spacer4, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 10), new Dimension(-1, 10), new Dimension(-1, 10), 0, false));
        final Spacer spacer5 = new Spacer();
        panelMain.add(spacer5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(10, -1), new Dimension(10, -1), new Dimension(10, -1), 0, false));
        pnlBottom = new JPanel();
        pnlBottom.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panelMain.add(pnlBottom, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        lblType = new JLabel();
        lblType.setBackground(new Color(-1));
        lblType.setHorizontalAlignment(0);
        lblType.setHorizontalTextPosition(0);
        lblType.setIcon(new ImageIcon(getClass().getResource("/images/address_type_private.png")));
        lblType.setText("");
        lblType.setVerticalAlignment(0);
        lblType.setVerticalTextPosition(1);
        pnlBottom.add(lblType, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(20, 30), new Dimension(20, 30), new Dimension(20, 30), 0, false));
        final Spacer spacer6 = new Spacer();
        pnlBottom.add(spacer6, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(10, -1), new Dimension(10, -1), new Dimension(10, -1), 0, false));
        final Spacer spacer7 = new Spacer();
        pnlBottom.add(spacer7, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer8 = new Spacer();
        pnlBottom.add(spacer8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(10, -1), new Dimension(10, -1), new Dimension(10, -1), 0, false));
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
    public JComponent $$$getRootComponent$$$() {
        return panelMain;
    }
}
