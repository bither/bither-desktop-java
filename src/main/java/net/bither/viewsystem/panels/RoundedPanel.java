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

package net.bither.viewsystem.panels;

import net.bither.BitherUI;
import net.bither.viewsystem.components.ImageDecorator;

import javax.swing.*;
import java.awt.*;

/**
 * <p>Panel to provide the following to UI:</p>
 * <ul>
 * <li>Rounded corners for use with wizards/light boxes</li>
 * </ul>
 *
 * @since 0.0.1
 */

public class RoundedPanel extends JPanel {

    private final int cornerRadius;

    /**
     * @param layout The layout manager
     */
    public RoundedPanel(LayoutManager2 layout) {
        super(layout);

        setOpaque(false);

        this.cornerRadius = BitherUI.COMPONENT_CORNER_RADIUS;

    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        // Get the dimensions
        int width = getWidth();
        int height = getHeight();

        // Ensure we render with smooth outcome
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHints(ImageDecorator.smoothRenderingHints());

        // Fill in a solid rounded block of the panel
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);

        // Draw the panel foreground over the shadow with rounded corners to give a subtle border effect
        Stroke original = g2.getStroke();
        g2.setColor(getForeground());
        g2.setStroke(new BasicStroke(0));
        g2.drawRoundRect(0, 0, width, height, cornerRadius, cornerRadius);
        g2.setStroke(original);

    }
}
