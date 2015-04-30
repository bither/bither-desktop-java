package net.bither.viewsystem.base;

import net.bither.languages.Languages;
import net.bither.languages.MessageKey;
import net.bither.preference.UserPreference;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.dialogs.DialogConfirmTask;
import net.bither.viewsystem.themes.Themes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * <p>Utility to provide the following to UI:</p>
 * <ul>
 * <li>Provision of localised buttons</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public class RadioButtons {

    /**
     * Utilities have no public constructor
     */
    private RadioButtons() {
    }

    /**
     * @param key    The resource key for the language string
     * @param values The values to apply to the string (can be null)
     * @return A new JButton with default styling
     */
    public static JRadioButton newRadioButton(MessageKey key, Object... values) {

        JRadioButton radio = new JRadioButton();
        radio.setText(Languages.safeText(key, values));

        // Ensure it is accessible
        AccessibilityDecorator.apply(radio, key);

        // Apply the current theme
        radio.setForeground(Themes.currentTheme.text());

        // Reinforce the idea of clicking
        radio.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Ensure we use the correct component orientation
        radio.applyComponentOrientation(Languages.currentComponentOrientation());

        return radio;
    }

    /**
     * @param key    The resource key for the language string
     * @param values The values to apply to the string (can be null)
     * @return A new JButton with default styling
     */
    public static JRadioButton newRadioButton(ActionListener listener, MessageKey key, Object... values) {

        JRadioButton radio = newRadioButton(key, values);

        radio.addActionListener(listener);

        return radio;
    }

    public static JCheckBox newCheckPassword() {
        final JCheckBox cbCheckPassword = new JCheckBox();
        cbCheckPassword.setSelected(UserPreference.getInstance().getCheckPasswordStrength());
        cbCheckPassword.setText(LocaliserUtils.getString("password_strength_check"));
        cbCheckPassword.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!cbCheckPassword.isSelected()) {
                    DialogConfirmTask dialogConfirmTask = new DialogConfirmTask(LocaliserUtils.getString("password_strength_check_off_warn"), new Runnable() {
                        @Override
                        public void run() {
                            UserPreference.getInstance().setCheckPasswordStrength(false);
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            cbCheckPassword.setSelected(true);
                        }
                    });
                    dialogConfirmTask.pack();
                    dialogConfirmTask.setVisible(true);
                } else {
                    UserPreference.getInstance().setCheckPasswordStrength(true);
                }
            }
        });
        return cbCheckPassword;
    }

}
