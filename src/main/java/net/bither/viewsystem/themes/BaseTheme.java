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

import java.awt.*;

/**
 * <p>Abstract base class to provide the following to themes:</p>
 * <ul>
 * <li>Access to common values</li>
 * </ul>
 *
 * @since 0.0.1
 */
public abstract class BaseTheme implements Theme {

    @Override
    public Color creditText() {
        // The status color value provides suitable contrast across all themes
        return statusGreen();
    }

    @Override
    public Color debitText() {
        // The status color value provides suitable contrast across all themes
        return statusRed();
    }

    @Override
    public Color statusRed() {
        return new Color(210, 50, 45);
    }

    @Override
    public Color statusAmber() {
        return new Color(237, 156, 40);
    }

    @Override
    public Color statusGreen() {
        return new Color(71, 164, 71);
    }

    @Override
    public Color tableRowBackground() {
        return sidebarPanelBackground();
    }

    @Override
    public Color tableRowAltBackground() {
        return detailPanelBackground();
    }

    @Override
    public Color tableRowSelectedBackground() {
        return sidebarSelectedText();
    }

}
