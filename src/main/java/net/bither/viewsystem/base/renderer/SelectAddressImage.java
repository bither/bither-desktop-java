package net.bither.viewsystem.base.renderer;

import net.bither.BitherUI;
import net.bither.fonts.AwesomeDecorator;
import net.bither.fonts.AwesomeIcon;
import net.bither.viewsystem.themes.Themes;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class SelectAddressImage extends DefaultTableCellRenderer {
    private JLabel labCheck;
    private JLabel labNull;

    public SelectAddressImage() {
        labCheck = new JLabel();
        labNull = new JLabel();
        labCheck.setOpaque(true);
        labNull.setOpaque(true);
        labNull.setText("");

        AwesomeDecorator.applyIcon(AwesomeIcon.CHECK, labCheck, true, BitherUI.SMALL_ICON_SIZE);


    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
                                                   int column) {
        if (row % 2 == 0) {
            labCheck.setBackground(Themes.currentTheme.detailPanelBackground());
            labNull.setBackground(Themes.currentTheme.detailPanelBackground());

        } else {
            labCheck.setBackground(Themes.currentTheme.sidebarPanelBackground());
            labNull.setBackground(Themes.currentTheme.sidebarPanelBackground());

        }

        if (Boolean.valueOf(value.toString())) {
            return labCheck;
        } else {
            return labNull;
        }

    }
}
