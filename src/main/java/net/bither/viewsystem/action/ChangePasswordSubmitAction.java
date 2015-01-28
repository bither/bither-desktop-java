/**
 * Copyright 2012 multibit.org
 *
 * Licensed under the MIT license (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://opensource.org/licenses/mit-license.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.bither.viewsystem.action;

import net.bither.BitherSetting;
import net.bither.bitherj.crypto.PasswordSeed;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.runnable.EditPasswordThread;
import net.bither.preference.UserPreference;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.dialogs.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.Arrays;

import javax.swing.*;
import java.awt.event.ActionEvent;


/**
 * This {@link Action} action decrypts private keys with the old password and then encrypts the private keys with the new password.
 */
public class ChangePasswordSubmitAction extends AbstractAction {
    private static final Logger log = LoggerFactory.getLogger(ChangePasswordSubmitAction.class);

    private static final long serialVersionUID = 1923492460598757765L;

    private JPasswordField currentPassword;

    private JPasswordField newPassword;

    private JPasswordField repeatNewPassword;



    public ChangePasswordSubmitAction(
            JPasswordField currentPassword, JPasswordField newPassword, JPasswordField repeatNewPassword) {


        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.repeatNewPassword = repeatNewPassword;

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // changePasswordPanel.clearMessages();


        if (currentPassword.getPassword() == null || currentPassword.getPassword().length > BitherSetting.PASSWORD_LENGTH_MAX
                || currentPassword.getPassword().length < BitherSetting.PASSWORD_LENGTH_MIN) {
            // Notify must enter the current password.
            new MessageDialog(LocaliserUtils.getString("changePasswordPanel.enterCurrentPassword")).showMsg();
            return;
        }


        PasswordSeed passwordSeed = PasswordSeed.getPasswordSeed();
        SecureCharSequence currentCharSequence = new SecureCharSequence(currentPassword.getPassword());
        if (!passwordSeed.checkPassword(currentCharSequence)) {
            new MessageDialog(LocaliserUtils.getString("createNewReceivingAddressSubmitAction.passwordIsIncorrect")).showMsg();
            return;
        }

        // Get the new passwords on the password fields.
        if (newPassword.getPassword() == null || newPassword.getPassword().length > BitherSetting.PASSWORD_LENGTH_MAX
                || newPassword.getPassword().length < BitherSetting.PASSWORD_LENGTH_MIN) {
            // Notify the user must enter a new password.
            new MessageDialog(LocaliserUtils.getString("changePasswordPanel.enterPasswords")).showMsg();
            return;
        } else {

            if (!Arrays.areEqual(newPassword.getPassword(), repeatNewPassword.getPassword())) {
                // Notify user passwords are different.
                new MessageDialog(LocaliserUtils.getString(
                        "showExportPrivateKeysAction.passwordsAreDifferent")).showMsg();
                return;
            } else {
                SecureCharSequence newSequence = new SecureCharSequence(newPassword.getPassword());
                EditPasswordThread editPasswordThread = new EditPasswordThread(currentCharSequence, newSequence, new EditPasswordThread.EditPasswordListener() {
                    @Override
                    public void onSuccess() {
                        // Success.
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {

                                new MessageDialog(LocaliserUtils.getString("changePasswordPanel.changePasswordSuccess")).showMsg();

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