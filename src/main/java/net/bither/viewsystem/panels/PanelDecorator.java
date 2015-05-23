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

import com.google.common.base.Preconditions;
import net.bither.languages.MessageKey;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.components.borders.TextBubbleBorder;
import net.bither.viewsystem.themes.Themes;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * <p>Decorator to provide the following to panels:</p>
 * <ul>
 * <li>Application of various themed styles to panels</li>
 * </ul>
 *
 * @since 0.0.1
 */
public class PanelDecorator {

    /**
     * Utilities have a private constructor
     */
    private PanelDecorator() {
    }

    /**
     * <p>Create the standard "wizard" theme</p>
     *
     * @param wizardScreenPanel The wizard panel to decorate and prepare for title, content and buttons
     */
    public static void applyWizardTheme(JPanel wizardScreenPanel) {

        Preconditions.checkNotNull(wizardScreenPanel, "'wizardScreenPanel' must be present");

        // Standard wizard layout
        MigLayout layout = new MigLayout(
                Panels.migLayout("fill,insets 15"),
                "[][][][]", // Column constraints
                "[]10[grow]10[]" // Row constraints
        );
        wizardScreenPanel.setLayout(layout);

        // Apply the theme
        wizardScreenPanel.setBackground(Themes.currentTheme.detailPanelBackground());

    }

    /**
     * <p>Create the standard "detail" theme</p>
     *
     * @param detailPanel The wizard panel to decorate (arranged as [title][dataPanel][buttons])
     * @param titleKey    The key to use for the title text
     */
    public static void applyScreenTheme(JPanel detailPanel, MessageKey titleKey) {

        Preconditions.checkNotNull(detailPanel, "'detailPanel' must be present");
        Preconditions.checkNotNull(titleKey, "'titleKey' must be present");

        // Standard wizard layout
        MigLayout layout = new MigLayout(
                Panels.migLayout("insets 5"),
                "[][][][]",
                "[shrink]10[grow]10[]" // Row constraints
        );
        detailPanel.setLayout(layout);

        // Apply the theme
        detailPanel.setBackground(Themes.currentTheme.detailPanelBackground());

        // Add the wizard components
        detailPanel.add(Labels.newTitleLabel(titleKey), "span 4,shrink,wrap,aligny top");

    }


    /**
     * <p>Make the panel have the "danger" theme</p>
     *
     * @param panel The panel to decorate
     */
    public static void applyDangerTheme(JPanel panel) {

        Preconditions.checkNotNull(panel, "'panel' must be present");

        Color background = Themes.currentTheme.dangerAlertBackground();
        Color border = Themes.currentTheme.dangerAlertBorder();
        Color text = Themes.currentTheme.dangerAlertText();

        applyTheme(panel, background, border, text);

    }

    /**
     * <p>Make the panel have the "danger faded" theme</p>
     *
     * @param panel The panel to decorate
     */
    public static void applyDangerFadedTheme(JPanel panel) {

        Preconditions.checkNotNull(panel, "'panel' must be present");

        Color background = Themes.currentTheme.dangerAlertFadedBackground();
        Color border = Themes.currentTheme.dangerAlertBorder();
        Color text = Themes.currentTheme.dangerAlertText();

        applyTheme(panel, background, border, text);

    }

    /**
     * <p>Make the panel have the "warning" theme</p>
     *
     * @param panel The panel to decorate
     */
    public static void applyWarningTheme(JPanel panel) {

        Preconditions.checkNotNull(panel, "'panel' must be present");

        Color background = Themes.currentTheme.warningAlertBackground();
        Color border = Themes.currentTheme.warningAlertBorder();
        Color text = Themes.currentTheme.warningAlertText();

        applyTheme(panel, background, border, text);

    }

    /**
     * <p>Make the panel have the "success" theme</p>
     *
     * @param panel The panel to decorate
     */
    public static void applySuccessTheme(JPanel panel) {

        Preconditions.checkNotNull(panel, "'panel' must be present");

        Color background = Themes.currentTheme.successAlertBackground();
        Color border = Themes.currentTheme.successAlertBorder();
        Color text = Themes.currentTheme.successAlertText();

        applyTheme(panel, background, border, text);

    }

    /**
     * <p>Make the panel have the "success faded" theme</p>
     *
     * @param panel The panel to decorate
     */
    public static void applySuccessFadedTheme(JPanel panel) {

        Preconditions.checkNotNull(panel, "'panel' must be present");

        Color background = Themes.currentTheme.successAlertFadedBackground();
        Color border = Themes.currentTheme.successAlertBorder();
        Color text = Themes.currentTheme.successAlertText();

        applyTheme(panel, background, border, text);

    }

    /**
     * <p>Make the panel have the "pending" theme</p>
     *
     * @param panel The panel to decorate
     */
    public static void applyPendingTheme(JPanel panel) {

        Preconditions.checkNotNull(panel, "'panel' must be present");

        Color background = Themes.currentTheme.pendingAlertBackground();
        Color border = Themes.currentTheme.pendingAlertBorder();
        Color text = Themes.currentTheme.pendingAlertText();

        applyTheme(panel, background, border, text);

    }

    /**
     * <p>Apply panel colours</p>
     *
     * @param panel      The target panel
     * @param background The background colour
     * @param border     The border colour
     * @param text       The text colour
     */
    private static void applyTheme(JPanel panel, Color background, Color border, Color text) {

        Preconditions.checkNotNull(panel, "'panel' must be present");

        panel.setBackground(background);
        panel.setForeground(text);

        // Ensure that the background color is presented
        panel.setOpaque(true);

        // Use a simple rounded border
        panel.setBorder(new TextBubbleBorder(border));

        for (Component component : panel.getComponents()) {
            if (component instanceof JLabel) {
                component.setForeground(text);
            }
        }

    }


}
