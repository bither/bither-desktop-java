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

import net.bither.viewsystem.base.BitherLabel;
import net.bither.viewsystem.base.FontSizer;
import net.bither.viewsystem.themes.Themes;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Created by nn on 14-11-10.
 */
public class CurrencyCenterJustifiedRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 1549545L;

    private int moduloRow = 0;

    public CurrencyCenterJustifiedRenderer(int moduloRow) {
        this.moduloRow = moduloRow;

    }

    BitherLabel label = new BitherLabel("");

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
                                                   int column) {
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBackground(Themes.currentTheme.detailPanelBackground());
        label.setOpaque(true);
        label.setText((String) value);
        label.setFont(FontSizer.INSTANCE.getAdjustedDefaultFontWithDelta(-1));

        Color backgroundColor = (row % 2 == moduloRow ? Themes.currentTheme.detailPanelBackground()
                : Themes.currentTheme.detailPanelBackground());
        label.setBackground(backgroundColor);
        label.setForeground(table.getForeground());

        return label;
    }
}
