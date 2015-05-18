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

import net.bither.BitherSetting;
import net.bither.bitherj.utils.UnitUtil;
import net.bither.viewsystem.froms.ShowTransactionsForm;
import net.bither.viewsystem.themes.Themes;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.*;
import java.awt.*;

/**
 * Created by nn on 14-11-7.
 */
public class DecimalAlignRenderer implements TableCellRenderer {
    private final TabStop tabStopRight = new TabStop(40, TabStop.ALIGN_RIGHT, TabStop.LEAD_NONE);
    private final TabStop tabStopLeft = new TabStop(41, TabStop.ALIGN_LEFT, TabStop.LEAD_NONE);

    private final TabSet tabSet = new TabSet(new TabStop[]{tabStopRight, tabStopLeft});

    private AttributeSet paragraphAttributeSet;
    private JTextPane pane;
    private Style style;
    private ShowTransactionsForm showTransactionsFrom;

    public DecimalAlignRenderer(ShowTransactionsForm showTransactionsFrom) {
        this.showTransactionsFrom = showTransactionsFrom;
        pane = new JTextPane();

        StyleContext styleContext = StyleContext.getDefaultStyleContext();
        paragraphAttributeSet = styleContext.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.TabSet, tabSet);
        pane.setParagraphAttributes(paragraphAttributeSet, true);

        style = pane.addStyle("number", null);

        pane.setOpaque(true);
        pane.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Themes.currentTheme.detailPanelBackground()));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
                                                   int column) {
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setOpaque(true);
        outerPanel.setBorder(BorderFactory.createEmptyBorder());

        JLabel filler = new JLabel();
        filler.setOpaque(true);

        if (value == null) {
            pane.setText("\t" + "");
        } else {
            long longValue = Long.valueOf(value.toString());
            pane.setText(UnitUtil.formatValue(longValue, UnitUtil.BitcoinUnit.BTC));

            if (longValue > 0) {
                // debit
                if (isSelected) {
                    pane.setForeground(table.getSelectionForeground());
                } else {
                    pane.setForeground(BitherSetting.CREDIT_FOREGROUND_COLOR);
                }
            } else {
                // credit
                if (isSelected) {
                    pane.setForeground(table.getSelectionForeground());
                } else {
                    pane.setForeground(BitherSetting.DEBIT_FOREGROUND_COLOR);

                }
            }
        }

        if (isSelected) {
            showTransactionsFrom.setSelectedRow(row);
            pane.setBackground(table.getSelectionBackground());
            outerPanel.setBackground(table.getSelectionBackground());
            filler.setBackground(table.getSelectionBackground());
            pane.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, table.getSelectionBackground()));
        } else {
            if (row % 2 == 1) {
                pane.setBackground(Themes.currentTheme.detailPanelBackground());
                pane.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Themes.currentTheme.detailPanelBackground()));
                outerPanel.setBackground(Themes.currentTheme.detailPanelBackground());
                filler.setBackground(Themes.currentTheme.detailPanelBackground());
                outerPanel.setOpaque(true);
            } else {
                pane.setBackground(Themes.currentTheme.sidebarPanelBackground());
                pane.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Themes.currentTheme.sidebarPanelBackground()));
                outerPanel.setBackground(Themes.currentTheme.sidebarPanelBackground());
                filler.setBackground(Themes.currentTheme.sidebarPanelBackground());
                pane.setOpaque(true);
                outerPanel.setOpaque(true);
                filler.setOpaque(true);
            }
        }

        StyleConstants.setForeground(style, pane.getForeground());
        if (row % 2 == 1 || isSelected) {
            StyleConstants.setBackground(style, pane.getBackground());
        } else {
            StyleConstants.setBackground(style, Themes.currentTheme.sidebarPanelBackground());
        }
        StyleConstants.setSpaceBelow(style, 10);

        pane.getStyledDocument().setCharacterAttributes(0, pane.getText().length(), pane.getStyle("number"), true);

        outerPanel.add(pane, BorderLayout.LINE_START);
        outerPanel.add(filler, BorderLayout.CENTER);

        // Avoid flicker by doing layout.
        outerPanel.doLayout();

        return outerPanel;
    }
}
