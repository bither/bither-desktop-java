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


import net.bither.core.CoreMessageKey;
import net.bither.languages.Languages;
import net.bither.languages.MessageKey;

import javax.swing.*;

/**
 * <p>Decorator to provide the following to application:</p>
 * <ul>
 * <li>Standard technique for applying FEST and Accessibility API information</li>
 * </ul>
 *
 * @since 0.0.1
 */
public class AccessibilityDecorator {

    /**
     * <p>Full FEST and Accessibility support (tooltip and description)</p>
     *
     * @param component  The Swing component to decorate
     * @param nameKey    The component name (used directly for FEST and with lookup for accessible name)
     * @param tooltipKey The component tooltip and accessible description
     */
    public static void apply(JComponent component, MessageKey nameKey, MessageKey tooltipKey) {

        // Ensure FEST can find it
        component.setName(nameKey.getKey());

        // Ensure we have a suitable tooltip
        component.setToolTipText(Languages.safeText(tooltipKey));

        // Ensure Accessibility API can find it
        component.getAccessibleContext().setAccessibleName(Languages.safeText(nameKey));
        component.getAccessibleContext().setAccessibleDescription(Languages.safeText(tooltipKey));

    }

    /**
     * <p>Basic FEST and Accessibility support (no tooltip or description)</p>
     *
     * @param component The Swing component to decorate
     * @param nameKey   The component name (used directly for FEST and with lookup for accessible name)
     */
    public static void apply(JComponent component, MessageKey nameKey) {

        // Ensure FEST can find it
        component.setName(nameKey.getKey());

        // Ensure Accessibility API can find it
        component.getAccessibleContext().setAccessibleName(Languages.safeText(nameKey));

    }

    /**
     * <p>Full FEST and Accessibility support (tooltip and description)</p>
     *
     * @param component  The Swing component to decorate
     * @param nameKey    The component name (used directly for FEST and with lookup for accessible name)
     * @param tooltipKey The component tooltip and accessible description
     */
    public static void apply(JComponent component, CoreMessageKey nameKey, CoreMessageKey tooltipKey) {

        // Ensure FEST can find it
        component.setName(nameKey.getKey());

        // Ensure we have a suitable tooltip
        component.setToolTipText(Languages.safeText(tooltipKey.getKey()));

        // Ensure Accessibility API can find it
        component.getAccessibleContext().setAccessibleName(Languages.safeText(nameKey.getKey()));
        component.getAccessibleContext().setAccessibleDescription(Languages.safeText(tooltipKey.getKey()));

    }

    /**
     * <p>Basic FEST and Accessibility support (no tooltip or description)</p>
     *
     * @param component The Swing component to decorate
     * @param nameKey   The component name (used directly for FEST and with lookup for accessible name)
     */
    public static void apply(JComponent component, CoreMessageKey nameKey) {

        // Ensure FEST can find it
        component.setName(nameKey.getKey());

        // Ensure Accessibility API can find it
        component.getAccessibleContext().setAccessibleName(Languages.safeText(nameKey.getKey()));

    }

}
