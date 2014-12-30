package net.bither.viewsystem.base.renderer;

/**
 * Created by nn on 14-11-10.
 */

import net.bither.viewsystem.base.BitherLabel;
import net.bither.viewsystem.base.ColorAndFontConstants;
import net.bither.viewsystem.base.FontSizer;
import net.bither.viewsystem.themes.Themes;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public   class CurrencyCenterJustifiedWithRightBorderRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 9949545L;
    private int moduloRow = 0;
    public  CurrencyCenterJustifiedWithRightBorderRenderer(int moduloRow){
        this.moduloRow=moduloRow;

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

        Color backgroundColor = (row % 2 == moduloRow ?Themes.currentTheme.detailPanelBackground()
                :Themes.currentTheme.detailPanelBackground());
        label.setBackground(backgroundColor);
        label.setForeground(table.getForeground());

        return label;
    }
}
