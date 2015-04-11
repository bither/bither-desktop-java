package net.bither.viewsystem.base.renderer;

import net.bither.BitherSetting;
import net.bither.bitherj.utils.Utils;
import net.bither.viewsystem.base.BitherLabel;
import net.bither.viewsystem.froms.ShowTransactionsForm;
import net.bither.viewsystem.themes.Themes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Created by nn on 14-11-7.
 */
public class TrailingJustifiedNumericRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 1549545L;

    private BitherLabel label;
    private ShowTransactionsForm showTransactionsFrom;

    public TrailingJustifiedNumericRenderer(ShowTransactionsForm showTransactionsFrom) {
        this.showTransactionsFrom = showTransactionsFrom;
        label = new BitherLabel("");
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
                                                   int column) {
        label.setHorizontalAlignment(SwingConstants.TRAILING);
        label.setOpaque(true);
        label.setBorder(new EmptyBorder(new Insets(0, BitherSetting.TABLE_BORDER, 1, BitherSetting.TABLE_BORDER)));
        if (Utils.isLong(value)) {
            long longValue = Long.valueOf(value.toString());


            if (longValue > 0) {
                // Debit.
                if (isSelected) {
                    label.setForeground(table.getSelectionForeground());
                } else {
                    label.setForeground(BitherSetting.DEBIT_FOREGROUND_COLOR);
                }
            } else {
                // Credit.
                if (isSelected) {
                    label.setForeground(table.getSelectionForeground());
                } else {
                    label.setForeground(BitherSetting.CREDIT_FOREGROUND_COLOR);
                }
            }
        }
        if (isSelected) {
            showTransactionsFrom.setSelectedRow(row);
            label.setBackground(table.getSelectionBackground());
        } else {
            if (row % 2 == 1) {
                label.setBackground(Themes.currentTheme.detailPanelBackground());
            } else {
                label.setBackground(Themes.currentTheme.sidebarPanelBackground());
                label.setOpaque(true);
            }
        }

        return label;
    }
}