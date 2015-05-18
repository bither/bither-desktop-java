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

package net.bither.viewsystem.base;

import net.bither.BitherUI;
import net.bither.fonts.AwesomeDecorator;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;

import javax.swing.*;

/**
 * <p>Decorator to provide the following to UI:</p>
 * <ul>
 * <li>Various button effects</li>
 * <li>Consistent iconography and accessibility</li>
 * </ul>
 *
 * @since 0.0.1
 */
public class ButtonDecorator {

    /**
     * Utilities have no public constructor
     */
    private ButtonDecorator() {
    }

    /**
     * <p>Decorate the button so that clicking will cause a "show"</p>
     * <p>The icon reflects the current state to make it more intuitive</p>
     *
     * @param button The button
     */
    public static void applyShow(JButton button) {

        // #53 Do not use an eye for reveal
        AwesomeDecorator.applyIcon(
                AwesomeIcon.LOCK,
                button,
                true,
                BitherUI.NORMAL_ICON_SIZE
        );

        AccessibilityDecorator.apply(button, MessageKey.SHOW, MessageKey.SHOW);

    }

    /**
     * <p>Decorate the button so that clicking will cause a "hide"</p>
     * <p>The icon reflects the current state to make it more intuitive</p>
     *
     * @param button The button
     */
    public static void applyHide(JButton button) {

        // #53 Do not use an eye for reveal
        AwesomeDecorator.applyIcon(
                AwesomeIcon.UNLOCK_ALT,
                button,
                true,
                BitherUI.NORMAL_ICON_SIZE
        );

        AccessibilityDecorator.apply(button, MessageKey.HIDE, MessageKey.HIDE);

    }

}
