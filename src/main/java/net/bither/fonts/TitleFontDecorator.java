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

package net.bither.fonts;

import com.google.common.base.Preconditions;
import net.bither.exception.UIException;
import net.bither.languages.LanguageKey;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.Locale;

/**
 * <p>Decorator to provide the following to Swing components:</p>
 * <ul>
 * <li>Application of a generic title font</li>
 * <li>Series of fonts for different languages</li>
 * <li>Final fallback font is supported on Windows, Mac and Linux in all languages</li>
 * </ul>
 * z
 * <p>More fonts can be sourced from Google Fonts and extracted from a ZIP file as a TTF</p>
 *
 * @since 0.0.1
 */
public class TitleFontDecorator {

    /**
     * The Corben Regular font is good for a reduced set of Latin languages (North America, Western Europe)
     * and makes the application look attractive
     */
    public static final Font CORBEN_REGULAR;

    /**
     * The OpenSans Regular font is good for all Latin languages (North America, Europe)
     * and is a good fallback from Corben
     */
    public static final Font OPENSANS_SEMIBOLD;

    /**
     * The NotoSans Bold font is good for all Devangari languages (India, Nepal)
     * Currently elided due to its 415Kb payload
     */
    //public static final Font NOTOSANS_BOLD;

    /**
     * The Impact font is found on Windows, Mac and Linux variants
     * It is a good fall back position when other fonts are not suitable
     */
    public static final Font IMPACT_REGULAR = Font.decode("Impact").deriveFont(Font.PLAIN);

    /**
     * The currently selected font for the given locale (default is Impact Regular in case of problems)
     */
    private static Font TITLE_FONT = IMPACT_REGULAR;

    static {

        CORBEN_REGULAR = assignFont("Corben-Regular.ttf");
        OPENSANS_SEMIBOLD = assignFont("OpenSans-Semibold.ttf");
        //NOTOSANS_BOLD = assignFont("NotoSans-Bold.ttf");

    }


    private static Font assignFont(String fontName) {

        try {
            InputStream in = TitleFontDecorator.class.getResourceAsStream("/fonts/" + fontName);

            Font loadedFont = Font.createFont(Font.TRUETYPE_FONT, in);

            Preconditions.checkNotNull(loadedFont, fontName + " not loaded");

            // Always stick with plain for best effect
            Font derivedFont = loadedFont.deriveFont(Font.PLAIN);

            // HTML tags won't use the font unless the graphics environment has registered it
            GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .registerFont(derivedFont);

            return derivedFont;

        } catch (Exception e) {
            throw new UIException(e);
        }

    }

    public static void apply(JComponent component, float size) {

        //Font font = TITLE_FONT.deriveFont(size);
        Font font = component.getFont().deriveFont(size);
        component.setFont(font);

    }

    /**
     * Set the title font based on the given locale
     */
    public static synchronized void refresh(Locale locale) {

        TITLE_FONT = LanguageKey.fromLocale(locale).getTitleFont();

    }
}
