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

package net.bither.viewsystem.themes;


import net.bither.languages.Languages;
import net.bither.languages.MessageKey;

/**
 * <p>Enum to provide the following to Theme API:</p>
 * <ul>
 * <li>Provision of supporting meta-data for a theme</li>
 * </ul>
 *
 * @since 0.0.1
 */
public enum ThemeKey {

    LIGHT(new LightTheme()),
    DARK(new DarkTheme()),
    BOOTSTRAP(new BootstrapTheme()),

    // End of enum
    ;

    private final Theme theme;

    /**
     * @param theme The theme
     */
    ThemeKey(Theme theme) {
        this.theme = theme;
    }

    /**
     * @return The associated singleton instance of the theme
     */
    public Theme theme() {
        return theme;
    }

    /**
     * @return The names of the themes for the current locale
     */
    public static String[] localisedNames() {

        return new String[]{
                Languages.safeText(MessageKey.LIGHT_THEME),
                Languages.safeText(MessageKey.DARK_THEME),
                Languages.safeText(MessageKey.BOOTSTRAP_THEME),
        };

    }

    /**
     * @param theme The theme
     * @return The matching theme key
     */
    public static ThemeKey fromTheme(Theme theme) {

        // Simple approach for a few themes
        if (theme instanceof LightTheme) {
            return LIGHT;
        }
        if (theme instanceof DarkTheme) {
            return DARK;
        }
        if (theme instanceof BootstrapTheme) {
            return BOOTSTRAP;
        }
        throw new IllegalArgumentException("Unknown theme '" + theme.getClass().getName() + "'");
    }
}
