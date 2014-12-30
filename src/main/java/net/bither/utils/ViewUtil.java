package net.bither.utils;

import net.bither.viewsystem.base.FontSizer;
import net.bither.viewsystem.dialogs.MessageDialog;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class ViewUtil {
    private static final int WIDTH_DELTA = 8;
    private static final int HEIGHT_DELTA = 8;

    public static int getMax(int... args) {
        int max = Integer.MIN_VALUE;
        for (int i : args) {
            if (max < i) {
                max = i;
            }
        }
        return max;
    }

    public static int calculateNormalWidth(JComponent component, String str) {
        Font font = FontSizer.INSTANCE.getAdjustedDefaultFont();
        FontMetrics fontMetrics = component.getFontMetrics(font);
        return fontMetrics.stringWidth(str) + WIDTH_DELTA;
    }


    public static void setDimension(JComponent component, Dimension dimension) {
        component.setPreferredSize(dimension);
        component.setMinimumSize(dimension);
        component.setMaximumSize(dimension);
    }

    public static int calculateNormalHeight(JComponent component) {
        Font font = FontSizer.INSTANCE.getAdjustedDefaultFont();
        FontMetrics fontMetrics = component.getFontMetrics(font);
        return fontMetrics.getHeight() + HEIGHT_DELTA;
    }

    public static void openURI(URI uri) {
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(uri);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            new MessageDialog(LocaliserUtils.getString("browser.unableToLoad", new String[]{uri.toString(), ioe.getMessage()})).showMsg();

        }
    }
}
