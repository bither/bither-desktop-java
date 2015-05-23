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

package net.bither.viewsystem.base.renderer;

import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.BitherLabel;

import javax.swing.*;
import java.awt.*;
import java.util.SortedSet;

/**
 * Created by nn on 14-11-13.
 */
public class LanguageComboBoxRenderer extends BitherLabel implements ListCellRenderer {
    private static final long serialVersionUID = -3301957214353702172L;
    private SortedSet<LanguageData> languageDataSet;

    public LanguageComboBoxRenderer(SortedSet<LanguageData> languageDataSet) {
        super("");
        this.languageDataSet = languageDataSet;
        setOpaque(true);
        setHorizontalAlignment(LEADING);
        setVerticalAlignment(CENTER);

        setComponentOrientation(ComponentOrientation.getOrientation(LocaliserUtils.getLocale()));
    }

    /*
     * This method finds the image and text corresponding to the selected
     * value and returns the label, set up to display the text and image.
     */
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        // Get the selected index. (The index param isn't
        // always valid, so just use the value.)
        int selectedIndex = 0;
        if (value != null) {
            selectedIndex = (Integer) value;
        }
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        // Set the icon and text. If icon was null, say so.
        int loopIndex = 0;
        for (LanguageData languageData : languageDataSet) {
            if (selectedIndex == loopIndex) {
                ImageIcon icon = languageData.image;
                String language = languageData.language;
                setIcon(icon);
                setText(language);
                break;
            }
            loopIndex++;
        }

        setFont(list.getFont());

        return this;
    }

    public static class LanguageData implements Comparable<LanguageData> {
        public String languageCode;
        public String language;
        public ImageIcon image;

        @Override
        public int compareTo(LanguageData other) {
            return languageCode.compareTo(other.languageCode);
        }
    }
}