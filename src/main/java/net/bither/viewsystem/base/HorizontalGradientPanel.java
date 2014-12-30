/**
 * Copyright 2012 multibit.org
 *
 * Licensed under the MIT license (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://opensource.org/licenses/mit-license.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.bither.viewsystem.base;

import net.bither.viewsystem.themes.Themes;

import javax.swing.*;
import java.awt.*;

public class HorizontalGradientPanel extends JPanel {

    private static final long serialVersionUID = 1992327444249499976L;

    private static final double PROPORTION_TO_FILL = 0.618; // golden ratio
    private ComponentOrientation componentOrientation;

    public HorizontalGradientPanel(ComponentOrientation componentOrientation) {
        this.componentOrientation = componentOrientation;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Dimension d = this.getSize();

        if (ComponentOrientation.LEFT_TO_RIGHT == componentOrientation) {
            g2d.setPaint(new GradientPaint(0, 0, Themes.currentTheme.detailPanelBackground(),
                    ((int) (d.width * PROPORTION_TO_FILL)), 0, Themes.currentTheme.detailPanelBackground(), false));
        } else {
            g2d.setPaint(new GradientPaint(((int) (d.width * (1 - PROPORTION_TO_FILL))), 0,Themes.currentTheme.detailPanelBackground(),
                    d.width, 0, Themes.currentTheme.detailPanelBackground(), false));
        }

        g2d.fillRect(0, 0, d.width, d.height);

        super.paintComponent(g);
    }
}
