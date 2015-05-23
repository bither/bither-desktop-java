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

package net.bither.viewsystem.froms;

import net.bither.BitherSetting;
import net.bither.BitherUI;
import net.bither.bitherj.crypto.PasswordSeed;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.runnable.EditPasswordThread;
import net.bither.bitherj.utils.Utils;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.preference.UserPreference;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.PasswordStrengthUtil;
import net.bither.viewsystem.TextBoxes;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.DialogConfirmTask;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.themes.Themes;
import net.miginfocom.swing.MigLayout;
import org.spongycastle.util.Arrays;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ChangePasswordPanel extends WizardPanel {

    private JPasswordField currentPassword;
    private JLabel spinner;
    private JPasswordField newPassword;
    private JPasswordField repeatNewPassword;
    private JLabel verificationStatusLabel;
    private JLabel labStrength;
    private JProgressBar pb;


    public ChangePasswordPanel() {
        super(MessageKey.SHOW_CHANGE_PASSWORD_WIZARD, AwesomeIcon.LOCK);
        setOkAction(new ChangePasswordSubmitAction());
    }

    @Override
    public void initialiseContent(JPanel panel) {

        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[]", // Column constraints
                "[]10[]" // Row constraints
        ));

        panel.add(Labels.newChangePasswordNote1(), "wrap");
        panel.add(getenterPasswordMaV(), "wrap");
        panel.add(getProgressPanel(), "align left,shrink,wrap");


        panel.add(getNewPasswordPanel(), "wrap");

    }

    @Override
    public void showPanel() {
        super.showPanel();
        currentPassword.requestFocus();
    }

    private JPanel getProgressPanel() {
        JPanel pbPanel = Panels.newPanel();
        pbPanel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[]50[][]", // Column constraints
                "[]" // Row constraints
        ));
        pb = new JProgressBar();
        Painter p = new Painter() {

            @Override
            public void paint(Graphics2D g, Object object, int width, int height) {
                JProgressBar bar = (JProgressBar) object;
                g.setColor(bar.getForeground());
                g.fillRect(0, 1, width - 2, height - 2);
            }

        };
        // install custom painter on the bar
        UIDefaults properties = new UIDefaults();
        properties.put("ProgressBar[Enabled].foregroundPainter", p);

        pb.setBorderPainted(false);
        pb.putClientProperty("Nimbus.Overrides", properties);

        pb.setStringPainted(false);

        pb.setMaximum(5);
        pb.setVisible(false);

        labStrength = Labels.newValueLabel("");
        pbPanel.add(Labels.newChangePasswordNote2(), "align left,shrink");
        pbPanel.add(pb, "align center,shrink");
        pbPanel.add(labStrength, "align center,shrink");
        return pbPanel;
    }


    private JPanel getenterPasswordMaV() {

        JPanel panel = Panels.newPanel(
                new MigLayout(
                        Panels.migXLayout(), // Layout
                        "[][][][]", // Columns
                        "[]" // Rows
                ));

        // Keep track of the credentials fields
        currentPassword = TextBoxes.newPassword();

        // Provide an invisible tar pit spinner
        spinner = Labels.newSpinner(Themes.currentTheme.fadedText(), BitherUI.NORMAL_PLUS_ICON_SIZE);
        spinner.setVisible(false);


        // Bind a document listener to allow instant update of UI to matched passwords
        currentPassword.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        updateModel();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        updateModel();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        updateModel();
                    }

                    /**
                     * Trigger any UI updates
                     */
                    private void updateModel() {
                        // Reset the credentials background


                        currentPassword.setBackground(Themes.currentTheme.dataEntryBackground());


                    }

                });


        panel.add(currentPassword, "growx,h 32,push");
        //panel.add(showButton, "shrink");

        // Ensure the icon label is a size suitable for rotation
        panel.add(spinner, BitherUI.NORMAL_PLUS_ICON_SIZE_MIG + ",wrap");

        return panel;

    }

    private JPanel getNewPasswordPanel() {

        JPanel panel = Panels.newPanel(
                new MigLayout(
                        Panels.migXLayout(), // Layout
                        "[][][][]", // Columns (require 4 columns for alignment with EnterPasswordView)
                        "[][][]" // Rows
                ));


        newPassword = TextBoxes.newPassword();
        newPassword.setName(MessageKey.ENTER_NEW_PASSWORD.getKey());

        repeatNewPassword = TextBoxes.newPassword();
        repeatNewPassword.setName(MessageKey.RETYPE_NEW_PASSWORD.getKey());


        // Bind a document listener to allow instant update of UI to matched passwords
        newPassword.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        updateModel();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        updateModel();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        updateModel();
                    }

                    private void updateModel() {
                        if (!pb.isVisible()) {
                            pb.setVisible(true);
                        }
                        SecureCharSequence secureCharSequence = new SecureCharSequence(newPassword.getPassword());
                        PasswordStrengthUtil.PasswordStrength strength = PasswordStrengthUtil.checkPassword
                                (secureCharSequence);
                        pb.setValue(strength.getValue() + 1);
                        pb.setForeground(strength.getColor());
                        labStrength.setText(strength.getName());
                        secureCharSequence.wipe();


                    }

                }

        );

        // Bind a document listener to allow instant update of UI to matched passwords
        repeatNewPassword.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        updateModel();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        updateModel();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        updateModel();
                    }

                    private void updateModel() {
                    }

                });


        verificationStatusLabel = Labels.newVerificationStatus(".credentials", true);
        verificationStatusLabel.setVisible(false);
        panel.add(Labels.newEnterNewPassword());
        panel.add(newPassword, " wrap");

        panel.add(Labels.newRetypeNewPassword());
        panel.add(repeatNewPassword, "wrap");
        panel.add(verificationStatusLabel, "span 4,grow,push");

        return panel;
    }

    public class ChangePasswordSubmitAction extends AbstractAction {


        @Override
        public void actionPerformed(ActionEvent e) {
            // changePasswordPanel.clearMessages();


            if (currentPassword.getPassword() == null || currentPassword.getPassword().length > BitherSetting.PASSWORD_LENGTH_MAX
                    || currentPassword.getPassword().length < BitherSetting.PASSWORD_LENGTH_MIN) {
                // Notify must enter the current password.
                new MessageDialog(LocaliserUtils.getString("edit_password_enter_current_password")).showMsg();
                return;
            }


            // Get the new passwords on the password fields.
            if (newPassword.getPassword() == null || newPassword.getPassword().length > BitherSetting.PASSWORD_LENGTH_MAX
                    || newPassword.getPassword().length < BitherSetting.PASSWORD_LENGTH_MIN) {
                // Notify the user must enter a new password.
                new MessageDialog(LocaliserUtils.getString("edit_password_enter_passwords")).showMsg();
                return;
            } else {

                if (!Arrays.areEqual(newPassword.getPassword(), repeatNewPassword.getPassword())) {
                    // Notify user passwords are different.
                    new MessageDialog(LocaliserUtils.getString(
                            "edit_password_passwords_are_different")).showMsg();
                    return;
                } else {
                    PasswordSeed passwordSeed = PasswordSeed.getPasswordSeed();
                    final SecureCharSequence currentCharSequence = new SecureCharSequence(currentPassword.getPassword());
                    if (!passwordSeed.checkPassword(currentCharSequence)) {
                        new MessageDialog(LocaliserUtils.getString("password_wrong")).showMsg();
                        return;
                    }
                    final SecureCharSequence newSequence = new SecureCharSequence(newPassword.getPassword());

                    PasswordStrengthUtil.PasswordStrength strength = PasswordStrengthUtil.checkPassword
                            (newSequence);
                    if (UserPreference.getInstance().getCheckPasswordStrength()) {
                        if (strength == PasswordStrengthUtil.PasswordStrength.Weak) {
                            String msg = Utils.format(LocaliserUtils.getString("password_strength_error"), strength.getName());
                            new MessageDialog(msg).showMsg();
                            return;

                        } else if (strength == PasswordStrengthUtil.PasswordStrength.Normal) {
                            String msg = Utils.format(LocaliserUtils.getString("password_strength_warning"), strength.getName());
                            DialogConfirmTask dialogConfirmTask = new DialogConfirmTask(msg, new Runnable() {
                                @Override
                                public void run() {
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            changePassword(currentCharSequence, newSequence);
                                        }
                                    });
                                }
                            });
                            dialogConfirmTask.pack();
                            dialogConfirmTask.setVisible(true);
                            return;

                        }
                    }
                    changePassword(currentCharSequence, newSequence);

                }
            }


        }

        private void changePassword(SecureCharSequence currentCharSequence, SecureCharSequence newSequence) {
            spinner.setVisible(true);
            EditPasswordThread editPasswordThread = new EditPasswordThread(currentCharSequence, newSequence, new EditPasswordThread.EditPasswordListener() {
                @Override
                public void onSuccess() {
                    // Success.
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            spinner.setVisible(false);
                            closePanel();
                            new MessageDialog(LocaliserUtils.getString("edit_password_success")).showMsg();

                        }
                    });

                }

                @Override
                public void onFailed() {
                    // Success.
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            spinner.setVisible(false);
                            new MessageDialog(LocaliserUtils.getString("edit_password_fail")).showMsg();

                        }
                    });

                }
            });
            editPasswordThread.start();

        }


    }
}
