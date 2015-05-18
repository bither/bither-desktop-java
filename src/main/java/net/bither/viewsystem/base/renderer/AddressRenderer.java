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

import net.bither.viewsystem.themes.Themes;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Created by nn on 14/12/8.
 */
public class AddressRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 154545L;

    JLabel primaryLabel = new JLabel();
    JPanel combinationPanel = new JPanel();


    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
                                                   int column) {

        // Prepare the primary icon (used always), and an extra icon and containing panel for use as required.
        primaryLabel.setHorizontalAlignment(SwingConstants.LEFT);
        primaryLabel.setVerticalAlignment(SwingConstants.CENTER);
        primaryLabel.setOpaque(true);

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
        // pb.setIndeterminate(true);

        primaryLabel.setText(value.toString());


        if (isSelected) {

            primaryLabel.setBackground(table.getSelectionBackground());
            primaryLabel.setForeground(table.getSelectionForeground());

            combinationPanel.setBackground(table.getSelectionBackground());
        } else {
            primaryLabel.setForeground(table.getForeground());

            combinationPanel.setForeground(table.getForeground());
            if (row % 2 == 1) {
                primaryLabel.setBackground(Themes.currentTheme.detailPanelBackground());

                combinationPanel.setBackground(Themes.currentTheme.detailPanelBackground());
            } else {
                primaryLabel.setBackground(Themes.currentTheme.sidebarPanelBackground());

                combinationPanel.setBackground(Themes.currentTheme.sidebarPanelBackground());
                primaryLabel.setOpaque(true);

                combinationPanel.setOpaque(true);
            }
        }

        // Return either a single icon or a double icon panel.

        return primaryLabel;

    }
}
