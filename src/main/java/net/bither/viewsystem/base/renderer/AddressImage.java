package net.bither.viewsystem.base.renderer;

import net.bither.BitherUI;
import net.bither.fonts.AwesomeDecorator;
import net.bither.fonts.AwesomeIcon;
import net.bither.viewsystem.themes.Themes;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class AddressImage extends DefaultTableCellRenderer {
    private JLabel labPrivateKey;
    private JLabel labWatchOnly;

    public AddressImage() {
        labPrivateKey = new JLabel();
        labWatchOnly = new JLabel();

        labWatchOnly.setOpaque(true);
        labPrivateKey.setOpaque(true);

        AwesomeDecorator.applyIcon(AwesomeIcon.LOCK, labPrivateKey, true, BitherUI.SMALL_ICON_SIZE);
        AwesomeDecorator.applyIcon(AwesomeIcon.FA_EYE, labWatchOnly, true, BitherUI.SMALL_ICON_SIZE);
        labPrivateKey.setHorizontalTextPosition(SwingConstants.CENTER);
        labWatchOnly.setHorizontalTextPosition(SwingConstants.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
                                                   int column) {

        if (row % 2 == 0) {
            labPrivateKey.setBackground(Themes.currentTheme.detailPanelBackground());
            labWatchOnly.setBackground(Themes.currentTheme.detailPanelBackground());

        } else {
            labPrivateKey.setBackground(Themes.currentTheme.sidebarPanelBackground());
            labWatchOnly.setBackground(Themes.currentTheme.sidebarPanelBackground());

        }
        if (Boolean.valueOf(value.toString())) {
            return labPrivateKey;
        } else {
            return labWatchOnly;
        }
    }
}
