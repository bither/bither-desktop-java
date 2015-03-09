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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public final class ColorAndFontConstants {
    private static final Logger log = LoggerFactory.getLogger(ColorAndFontConstants.class);

    public static String BITHER_DEFAULT_FONT_NAME;
    public static int BITHER_DEFAULT_FONT_STYLE;
    public static int BITHER_DEFAULT_FONT_SIZE;


    public static final int BRIGHTEN_CONSTANT = 4;

    private static boolean inverse = false;

    /**
     * Utility class should not have a public constructor
     */
    private ColorAndFontConstants() {
    }

    public static void init() {
        BITHER_DEFAULT_FONT_NAME = UIManager.get("Label.font") == null ? Font.DIALOG : ((Font) UIManager.get("Label.font"))
                .getFontName();
        BITHER_DEFAULT_FONT_STYLE = UIManager.get("Label.font") == null ? 0 : ((Font) UIManager.get("Label.font")).getStyle();
        BITHER_DEFAULT_FONT_SIZE = UIManager.get("Label.font") == null ? 13 : ((Font) UIManager.get("Label.font")).getSize() + 1;

        // Work out if we are using an inverse color scheme

        Color labelBackground = (Color) UIManager.get("Label.background");
        if (labelBackground != null) {
            log.debug("labelBackground = " + labelBackground.getRed() + " " + labelBackground.getGreen() + " " + labelBackground.getBlue());
            inverse = (labelBackground.getRed() + labelBackground.getGreen() + labelBackground.getBlue() < 384);

            // Brighten it.
            labelBackground = new Color(Math.min(255, labelBackground.getRed() + BRIGHTEN_CONSTANT),
                    Math.min(255, labelBackground.getGreen() + BRIGHTEN_CONSTANT),
                    Math.min(255, labelBackground.getBlue() + BRIGHTEN_CONSTANT));
        }

        // Logged simply for interest - it might be useful to determine inverse on some machines in the future.
        Color labelForeground = (Color) UIManager.get("Label.foreground");
        if (labelForeground != null) {
            log.debug("labelForeground = " + labelForeground.getRed() + " " + labelForeground.getGreen() + " " + labelForeground.getBlue());
        }

    }

    public static boolean isInverse() {
        return inverse;
    }
}
