package net.bither.viewsystem.base;

import javax.swing.*;
import java.awt.*;

public class HelpButton extends BitherButton {

    private static final long serialVersionUID = 6708096174704292284L;

    public HelpButton(Action action) {
        this(action, false);
    }

    public HelpButton(Action action, boolean paintBorder) {
        super(action);

        setBorderPainted(paintBorder);
        setContentAreaFilled(paintBorder);
        setFocusPainted(false);

        if (getIcon() != null && (getText() == null || "".equals(getText()))) {
            int width = getIcon().getIconWidth();
            int height = getIcon().getIconHeight();
            setPreferredSize(new Dimension(width, height));
        }

    }
}
