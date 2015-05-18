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

package net.bither.viewsystem.base.renderer;

import net.bither.bitherj.core.BlockChain;
import net.bither.bitherj.core.Tx;
import net.bither.utils.ImageLoader;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.froms.ShowTransactionsForm;
import net.bither.viewsystem.themes.Themes;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class ImageRenderer extends DefaultTableCellRenderer {


    private static final long serialVersionUID = 154545L;

    JLabel primaryLabel = new JLabel();

    // If the component is a doubleIcon the next fields are used.
    JLabel extraLabel = new JLabel();
    boolean doubleIcon = false;
    JPanel combinationPanel = new JPanel();
    private ShowTransactionsForm showTransactionsFrom;

    public ImageRenderer(ShowTransactionsForm showTransactionsFrom) {
        this.showTransactionsFrom = showTransactionsFrom;

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
                                                   int column) {

        // Prepare the primary icon (used always), and an extra icon and containing panel for use as required.
        primaryLabel.setHorizontalAlignment(SwingConstants.CENTER);
        primaryLabel.setVerticalAlignment(SwingConstants.CENTER);
        primaryLabel.setOpaque(true);
        extraLabel.setHorizontalAlignment(SwingConstants.CENTER);
        extraLabel.setVerticalAlignment(SwingConstants.CENTER);
        extraLabel.setOpaque(true);
        combinationPanel.setOpaque(true);
        combinationPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        // Prepare a double icon panel for use as required.
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.LINE_END;
        combinationPanel.add(primaryLabel, constraints);

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.LINE_START;

        combinationPanel.add(extraLabel, constraints);

        // Get the transaction and transaction confidence
        Tx transaction = (Tx) value;


        // Coinbase transactions have an extra pickaxe icon.
        if (transaction != null && transaction.isCoinBase()) {
            extraLabel.setIcon(ImageLoader.pickaxeIcon);
            doubleIcon = true;
        } else {
            doubleIcon = false;
        }

        if (BlockChain.getInstance().getLastBlock() == null) {
            primaryLabel.setText("?");
            primaryLabel.setIcon(null);
        } else {
            int numberOfBlocksEmbedded = transaction.getConfirmationCount();
            if (transaction != null && transaction.isCoinBase()) {
                // Coinbase tx mature slower than regular blocks.
                numberOfBlocksEmbedded = numberOfBlocksEmbedded / 20;
            }
            ImageIcon buildingIcon = ImageLoader.getBuildingIcon(transaction, numberOfBlocksEmbedded);
            primaryLabel.setIcon(buildingIcon);
            primaryLabel.setText("");
            if (numberOfBlocksEmbedded >= 6) {
                primaryLabel.setToolTipText(LocaliserUtils.getString("bither_frame_status_is_confirmed"));
            } else {
                if (transaction != null && transaction.isCoinBase()) {
                    primaryLabel.setToolTipText(LocaliserUtils.getString("bither_frame_status_being_confirmed_and_coinbase"));
                } else {
                    primaryLabel.setToolTipText(LocaliserUtils.getString("bither_frame_status_being_confirmed"));
                }
            }
        }


        // Propagate the tooltip text.
        extraLabel.setToolTipText(primaryLabel.getToolTipText());
        combinationPanel.setToolTipText(primaryLabel.getToolTipText());

        // Set foreground and background colors.
        if (isSelected) {
            showTransactionsFrom.setSelectedRow(row);
            primaryLabel.setBackground(table.getSelectionBackground());
            primaryLabel.setForeground(table.getSelectionForeground());
            extraLabel.setBackground(table.getSelectionBackground());
            extraLabel.setForeground(table.getSelectionForeground());
            combinationPanel.setBackground(table.getSelectionBackground());
        } else {
            primaryLabel.setForeground(table.getForeground());
            extraLabel.setForeground(table.getForeground());
            combinationPanel.setForeground(table.getForeground());
            if (row % 2 == 1) {
                primaryLabel.setBackground(Themes.currentTheme.detailPanelBackground());
                extraLabel.setBackground(Themes.currentTheme.detailPanelBackground());
                combinationPanel.setBackground(Themes.currentTheme.detailPanelBackground());
            } else {
                primaryLabel.setBackground(Themes.currentTheme.sidebarPanelBackground());
                extraLabel.setBackground(Themes.currentTheme.sidebarPanelBackground());
                combinationPanel.setBackground(Themes.currentTheme.sidebarPanelBackground());
                primaryLabel.setOpaque(true);
                extraLabel.setOpaque(true);
                combinationPanel.setOpaque(true);
            }
        }

        // Return either a single icon or a double icon panel.
        if (doubleIcon) {
            return combinationPanel;
        } else {
            return primaryLabel;
        }
    }


}