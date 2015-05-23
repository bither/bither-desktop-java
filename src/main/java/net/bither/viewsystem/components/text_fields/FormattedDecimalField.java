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

package net.bither.viewsystem.components.text_fields;

import com.google.common.base.Preconditions;
import net.bither.utils.Numbers;
import net.bither.viewsystem.themes.Themes;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.math.BigDecimal;

/**
 * <p>Text field to provide the following to UI:</p>
 * <ul>
 * <li>Accepts decimal and integer values</li>
 * <li>Places upper and lower range limits (min/max)</li>
 * <li>Limits number of decimal places</li>
 * <li>Handles configured grouping and decimal characters for different locales</li>
 * </ul>
 *
 * @since 0.0.1
 */
public class FormattedDecimalField extends JFormattedTextField {

    /**
     * @param min           The minimum value
     * @param max           The maximum value
     * @param decimalPlaces The number of decimal places to show (padding as required)
     * @param maxLength     The maximum length
     */
    public FormattedDecimalField(double min, double max, int decimalPlaces, int maxLength) {

        super();

        Preconditions.checkNotNull(min, "'min' must be present");
        Preconditions.checkNotNull(max, "'max' must be present");
        Preconditions.checkState(min < max, "'min' must be less than 'max'");

        Preconditions.checkState(decimalPlaces >= 0 && decimalPlaces < 15, "'decimalPlaces' must be in range [0,15)");

        // setInputVerifier(new ThemeAwareDecimalInputVerifier(min, max));

        setBackground(Themes.currentTheme.dataEntryBackground());

        // Build number formatters
        NumberFormatter defaultFormatter = new NumberFormatter();
        defaultFormatter.setValueClass(BigDecimal.class);

        NumberFormatter displayFormatter = Numbers.newDisplayFormatter(decimalPlaces, maxLength);
        NumberFormatter editFormatter = Numbers.newEditFormatter(decimalPlaces, maxLength);

        setFormatterFactory(new DefaultFormatterFactory(
                defaultFormatter,
                displayFormatter,
                editFormatter
        ));

    }

}
