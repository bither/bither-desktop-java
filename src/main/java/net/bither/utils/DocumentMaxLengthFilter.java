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

package net.bither.utils;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * <p>Document filter to provide the following to text fields/areas:</p>
 * <ul>
 * <li>Limiting input to a given maximum length</li>
 * </ul>
 *
 * @since 0.0.1
 */
public class DocumentMaxLengthFilter extends DocumentFilter {

    private final int maxCharacters;

    public DocumentMaxLengthFilter(int maxChars) {
        maxCharacters = maxChars;
    }

    public void insertString(FilterBypass fb, int offs, String str, AttributeSet a) throws BadLocationException {

        // Reject if the insertion would be too long
        if ((fb.getDocument().getLength() + str.length()) <= maxCharacters) {
            super.insertString(fb, offs, str, a);
        }
    }

    public void replace(FilterBypass fb, int offs, int length, String str, AttributeSet a) throws BadLocationException {

        // Reject if the replacement would be too long
        if ((fb.getDocument().getLength() + str.length() - length) <= maxCharacters) {
            super.replace(fb, offs, length, str, a);
        }
    }

}