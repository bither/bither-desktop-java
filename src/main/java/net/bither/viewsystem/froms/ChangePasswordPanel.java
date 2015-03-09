package net.bither.viewsystem.froms;

import net.bither.BitherSetting;
import net.bither.BitherUI;
import net.bither.bitherj.crypto.PasswordSeed;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.runnable.EditPasswordThread;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.TextBoxes;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.themes.Themes;
import net.miginfocom.swing.MigLayout;
import org.spongycastle.util.Arrays;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;

public class ChangePasswordPanel extends WizardPanel {

    private JPasswordField currentPassword;
    private JLabel spinner;
    private JPasswordField newPassword;
    private JPasswordField repeatNewPassword;
    private JLabel verificationStatusLabel;


    public ChangePasswordPanel() {
        super(MessageKey.SHOW_CHANGE_PASSWORD_WIZARD, AwesomeIcon.LOCK, false);
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
        panel.add(Labels.newChangePasswordNote2(), "wrap");
        panel.add(getNewPasswordPanel(), "wrap");

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
                    SecureCharSequence currentCharSequence = new SecureCharSequence(currentPassword.getPassword());
                    if (!passwordSeed.checkPassword(currentCharSequence)) {
                        new MessageDialog(LocaliserUtils.getString("password_wrong")).showMsg();
                        return;
                    }
                    SecureCharSequence newSequence = new SecureCharSequence(newPassword.getPassword());
                    EditPasswordThread editPasswordThread = new EditPasswordThread(currentCharSequence, newSequence, new EditPasswordThread.EditPasswordListener() {
                        @Override
                        public void onSuccess() {
                            // Success.
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    onCancel();
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

                                    new MessageDialog(LocaliserUtils.getString("changePasswordPanel.changePasswordFailed")).showMsg();

                                }
                            });

                        }
                    });
                    editPasswordThread.start();

                }
            }


        }


    }
}
