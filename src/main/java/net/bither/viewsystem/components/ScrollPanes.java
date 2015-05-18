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

package net.bither.viewsystem.components;


import net.bither.viewsystem.components.borders.TextBubbleBorder;
import net.bither.viewsystem.themes.Themes;

import javax.swing.*;

/**
 * <p>Decorator to provide the following to UI:</p>
 * <ul>
 * <li>Apply a scroll pane to the given components</li>
 * </ul>
 *
 * @since 0.0.1
 */
public class ScrollPanes {

    /**
     * Utilities have no public constructor
     */
    private ScrollPanes() {
    }

    /**
     * <p>Create a new scroll pane to wrap the given component using the read only theme</p>
     *
     * @param component The component to be wrapped in a scroll pane
     */
    public static JScrollPane newReadOnlyScrollPane(final JComponent component) {

        // Remove the border from the component
        component.setBorder(null);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setOpaque(true);
        scrollPane.setBackground(Themes.currentTheme.readOnlyBackground());
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // View port requires special handling
        scrollPane.setViewportView(component);
        scrollPane.getViewport().setBackground(Themes.currentTheme.readOnlyBackground());
        scrollPane.setViewportBorder(new TextBubbleBorder(Themes.currentTheme.readOnlyBorder()));

        ScrollBarUIDecorator.apply(scrollPane, false);

        return scrollPane;

    }

    /**
     * <p>Create a new scroll pane to wrap the given component using the data entry theme</p>
     *
     * @param component The component to be wrapped in a scroll pane
     */
    public static JScrollPane newDataEntryScrollPane(final JComponent component) {

        // Remove the border from the component
        component.setBorder(null);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setOpaque(true);
        scrollPane.setBackground(Themes.currentTheme.dataEntryBackground());
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // View port requires special handling
        scrollPane.setViewportView(component);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(Themes.currentTheme.detailPanelBackground());
        scrollPane.setViewportBorder(new TextBubbleBorder(Themes.currentTheme.dataEntryBorder()));

        ScrollBarUIDecorator.apply(scrollPane, false);

        return scrollPane;

    }

}