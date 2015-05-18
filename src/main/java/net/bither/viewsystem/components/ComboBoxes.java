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

import com.google.common.base.Preconditions;
import net.bither.BitherUI;
import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.BitherjSettings.MarketType;
import net.bither.languages.Languages;
import net.bither.languages.MessageKey;
import net.bither.preference.UserPreference;
import net.bither.utils.ExchangeUtil;
import net.bither.utils.MarketUtil;
import net.bither.viewsystem.base.AccessibilityDecorator;
import net.bither.viewsystem.themes.Themes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * <p>Utility to provide the following to UI:</p>
 * <ul>
 * <li>Provision of localised combo boxes</li>
 * </ul>
 *
 * @since 0.0.1
 */
public class ComboBoxes {

    /**
     * The "languages" combo box action command
     */
    public static final String LANGUAGES_COMMAND = "languages";
    /**
     * The "show balance" combo box action command
     */
    public static final String SHOW_BALANCE_COMMAND = "showBalance";
    /**
     * The "themes" combo box action command
     */
    public static final String THEMES_COMMAND = "themes";
    /**
     * The "paymentRequests" combo box action command
     */
    public static final String PAYMENT_REQUESTS_COMMAND = "paymentRequests";

    /**
     * The "alert sound" combo box action command
     */
    public static final String ALERT_SOUND_COMMAND = "alertSound";
    /**
     * The "receive sound" combo box action command
     */
    public static final String RECEIVE_SOUND_COMMAND = "receiveSound";
    /**
     * The "Bitcoin symbol" combo box action command
     */
    public static final String BITCOIN_SYMBOL_COMMAND = "bitcoinSymbol";
    /**
     * The "local symbol" combo box action command
     */
    public static final String LOCAL_SYMBOL_COMMAND = "localSymbol";
    /**
     * The "placement" combo box action command
     */
    public static final String PLACEMENT_COMMAND = "placement";
    /**
     * The "grouping separator" combo box action command
     */
    public static final String GROUPING_COMMAND = "grouping";
    /**
     * The "decimal separator" combo box action command
     */
    public static final String DECIMAL_COMMAND = "decimal";
    /**
     * The "exchange rate provider" combo box action command
     */
    public static final String EXCHANGE_RATE_PROVIDER_COMMAND = "exchange";
    /**
     * The "currency" combo box action command
     */
    public static final String CURRENCY_COMMAND = "currency";
    /**
     * The "Tor" combo box action command
     */
    public static final String TOR_COMMAND = "tor";
    /**
     * The "Trezor" combo box action command
     */
    public static final String TREZOR_COMMAND = "trezor";

    /**
     * Utilities have no public constructor
     */
    private ComboBoxes() {
    }

    /**
     * @param items The items for the combo box model
     * @return A new editable combo box with default styling (no listener since it will cause early event triggers during set up)
     */
    public static <T> JComboBox<T> newComboBox(T[] items) {

        JComboBox<T> comboBox = new JComboBox<T>(items);

        // Required to match icon button heights
        comboBox.setMinimumSize(new Dimension(25, BitherUI.NORMAL_ICON_SIZE + 14));

        // Required to blend in with panel
        comboBox.setBackground(Themes.currentTheme.detailPanelBackground());

        // Ensure we use the correct component orientation
        comboBox.applyComponentOrientation(Languages.currentComponentOrientation());

        // Ensure that keyboard navigation does not trigger action events
        comboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);

        // Increase border insets to create better visual clarity
        comboBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));

        // Push out the standard scrollbar beyond the default
        comboBox.setMaximumRowCount(10);

        // Adjust the scrollbar UI for the popup menu
        Object popupComponent = comboBox.getUI().getAccessibleChild(comboBox, 0);
        if (popupComponent instanceof JPopupMenu) {

            JPopupMenu popupMenu = (JPopupMenu) popupComponent;
            for (Component component : popupMenu.getComponents()) {
                if ((component instanceof JScrollPane)) {
                    JScrollPane scrollPane = (JScrollPane) component;

                    // Ensure we maintain the overall theme
                    ScrollBarUIDecorator.apply(scrollPane, true);

                }
            }
        }

        return comboBox;

    }

    /**
     * @return A new read only combo box (no listeners attached)
     */
    public static <T> JComboBox<T> newReadOnlyComboBox(T[] items) {

        JComboBox<T> comboBox = newComboBox(items);

        comboBox.setEditable(false);

        // Apply theme
        comboBox.setBackground(Themes.currentTheme.readOnlyComboBox());

        return comboBox;

    }

    /**
     * @param listener  The action listener to alert when the selection is made
     * @param selectYes True if the "yes" option [0] should be selected, otherwise "no" is selected [1]
     * @return A new "yes/no" read only combo box
     */
    public static JComboBox<String> newYesNoComboBox(ActionListener listener, boolean selectYes) {

        JComboBox<String> comboBox = newReadOnlyComboBox(new String[]{
                Languages.safeText(MessageKey.YES),
                Languages.safeText(MessageKey.NO)
        });

        // Ensure it is accessible
        AccessibilityDecorator.apply(comboBox, MessageKey.YES);

        comboBox.setEditable(false);

        comboBox.setSelectedIndex(selectYes ? 0 : 1);

        // Apply theme
        comboBox.setBackground(Themes.currentTheme.readOnlyComboBox());

        // Set the listener at the end to avoid spurious events
        comboBox.addActionListener(listener);

        return comboBox;

    }

    /**
     * @param listener   The action listener to alert when the selection is made
     * @param alertSound True if the "yes" option should be pre-selected
     * @return A new "yes/no" combo box
     */
    public static JComboBox<String> newAlertSoundYesNoComboBox(ActionListener listener, boolean alertSound) {

        JComboBox<String> comboBox = newYesNoComboBox(listener, alertSound);

        // Ensure it is accessible
        AccessibilityDecorator.apply(comboBox, MessageKey.ALERT_SOUND, MessageKey.ALERT_SOUND_TOOLTIP);

        comboBox.setActionCommand(ALERT_SOUND_COMMAND);

        return comboBox;
    }

    /**
     * @param listener     The action listener to alert when the selection is made
     * @param receiveSound True if the "yes" option should be pre-selected
     * @return A new "yes/no" combo box
     */
    public static JComboBox<String> newReceiveSoundYesNoComboBox(ActionListener listener, boolean receiveSound) {

        JComboBox<String> comboBox = newYesNoComboBox(listener, receiveSound);

        // Ensure it is accessible
        AccessibilityDecorator.apply(comboBox, MessageKey.RECEIVE_SOUND, MessageKey.RECEIVE_SOUND_TOOLTIP);

        comboBox.setActionCommand(RECEIVE_SOUND_COMMAND);

        return comboBox;
    }

    /**
     * @param listener The action listener to alert when the selection is made
     * @param useTor   True if the "yes" option should be pre-selected
     * @return A new "yes/no" combo box
     */
    public static JComboBox<String> newTorYesNoComboBox(ActionListener listener, boolean useTor) {

        JComboBox<String> comboBox = newYesNoComboBox(listener, useTor);

        // Ensure it is accessible
        AccessibilityDecorator.apply(comboBox, MessageKey.SELECT_TOR, MessageKey.SELECT_TOR_TOOLTIP);

        comboBox.setActionCommand(TOR_COMMAND);

        return comboBox;
    }


    /**
     * @param listener    The action listener to alert when the selection is made
     * @param showBalance True if the "yes" option should be pre-selected
     * @return A new "yes/no" combo box
     */
    public static JComboBox<String> newShowBalanceYesNoComboBox(ActionListener listener, boolean showBalance) {

        JComboBox<String> comboBox = newYesNoComboBox(listener, showBalance);

        // Ensure it is accessible
        AccessibilityDecorator.apply(comboBox, MessageKey.SHOW_BALANCE, MessageKey.SHOW_BALANCE_TOOLTIP);

        comboBox.setActionCommand(SHOW_BALANCE_COMMAND);

        return comboBox;
    }

    /**
     * @param listener The action listener to alert when the selection is made
     * @return A new "contact checkbox" combo box (all, none)
     */
    public static JComboBox<String> newContactsCheckboxComboBox(ActionListener listener) {

        String[] items = new String[]{
                Languages.safeText(MessageKey.ALL),
                Languages.safeText(MessageKey.NONE),
        };

        JComboBox<String> comboBox = newReadOnlyComboBox(items);

        // Ensure it is accessible
        AccessibilityDecorator.apply(comboBox, MessageKey.CONTACTS, MessageKey.CONTACTS_TOOLTIP);

        // Add the listener at the end to avoid false events
        comboBox.addActionListener(listener);

        return comboBox;

    }

    /**
     * @param listener The action listener to alert when the selection is made
     * @return A new "history checkbox" combo box (all, none) - kept separate from contacts
     */
    public static JComboBox<String> newHistoryCheckboxComboBox(ActionListener listener) {

        JComboBox<String> comboBox = newContactsCheckboxComboBox(listener);

        // Ensure it is accessible
        AccessibilityDecorator.apply(comboBox, MessageKey.HISTORY, MessageKey.HISTORY_TOOLTIP);

        return comboBox;

    }


    /**
     * @param listener The action listener
     * @return A new "exchange rate provider" combo box
     */
    public static JComboBox<MarketUtil.MarketTypeMode> newExchangeRateProviderComboBox(ActionListener listener) {

        Preconditions.checkNotNull(listener, "'listener' must be present");

        // Get all the exchange names

        MarketUtil.MarketTypeMode[] marketTypeModes = new MarketUtil.MarketTypeMode[MarketType.values().length];
        for (int i = 0; i < MarketType.values().length; i++) {
            marketTypeModes[i] = new MarketUtil.MarketTypeMode(MarketType.values()[i]);
        }
        JComboBox<MarketUtil.MarketTypeMode> comboBox = newReadOnlyComboBox(marketTypeModes);
        comboBox.setMaximumRowCount(BitherUI.COMBOBOX_MAX_ROW_COUNT);

        // Ensure it is accessible
        AccessibilityDecorator.apply(comboBox, MessageKey.EXCHANGE_RATE_PROVIDER, MessageKey.EXCHANGE_RATE_PROVIDER_TOOLTIP);
        // Determine the selected index
        MarketType marketType = UserPreference.getInstance().getDefaultMarket();
        comboBox.setSelectedIndex(marketType.ordinal());

        // Add the listener at the end to avoid false events
        comboBox.setActionCommand(EXCHANGE_RATE_PROVIDER_COMMAND);
        comboBox.addActionListener(listener);

        return comboBox;

    }

    public static JComboBox<String> newCurrencyCodeComboBox(ActionListener listener) {

        final JComboBox<String> comboBox = newReadOnlyComboBox(ExchangeUtil.exchangeNames);

        // Ensure it is accessible
        AccessibilityDecorator.apply(comboBox, MessageKey.SELECT_LOCAL_CURRENCY, MessageKey.SELECT_LOCAL_CURRENCY_TOOLTIP);
        ExchangeUtil.Currency currency = UserPreference.getInstance().getDefaultCurrency();
        comboBox.setSelectedIndex(currency.ordinal());
        // Add the listener at the end to avoid false events
        comboBox.setActionCommand(ComboBoxes.CURRENCY_COMMAND);
        comboBox.addActionListener(listener);

        return comboBox;

    }

    /**
     * @param listener The action listener
     * @return A new "seed size" combo box
     */
    public static JComboBox<String> newSeedSizeComboBox(ActionListener listener) {

        JComboBox<String> comboBox = newReadOnlyComboBox(new String[]{
                "12",
                "18",
                "24"
        });

        // Ensure it is accessible
        AccessibilityDecorator.apply(comboBox, MessageKey.SEED_SIZE, MessageKey.SEED_SIZE_TOOLTIP);

        comboBox.setSelectedIndex(0);

        // Add the listener at the end to avoid false events
        comboBox.addActionListener(listener);

        return comboBox;
    }

    /**
     * @param comboBox The combo box to set the selection on
     * @param items    The items in the model
     * @param item     the item that should be matched using a case-sensitive "starts with" approach
     */
    public static void selectFirstMatch(JComboBox<String> comboBox, String[] items, String item) {

        // Avoid working with nulls
        if (item == null) {
            comboBox.setSelectedIndex(-1);
            return;
        }

        // Determine the first matching separator
        for (int i = 0; i < items.length; i++) {
            Preconditions.checkNotNull(items[i], "'items[" + i + "]' must be present");
            if (items[i].startsWith(item)) {
                comboBox.setSelectedIndex(i);
                break;
            }
        }

    }

}