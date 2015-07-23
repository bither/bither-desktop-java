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
import net.bither.bitherj.crypto.PasswordSeed;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.delegate.IPasswordGetter;
import net.bither.bitherj.delegate.IPasswordGetterDelegate;
import net.bither.bitherj.utils.Utils;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.model.Check;
import net.bither.preference.UserPreference;
import net.bither.utils.CheckUtil;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.PasswordStrengthUtil;
import net.bither.viewsystem.TextBoxes;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.DialogConfirmTask;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.listener.ICheckPasswordListener;
import net.bither.viewsystem.listener.IDialogPasswordListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PasswordPanel extends WizardPanel {

    private JLabel verificationStatusLabel;

    private JPasswordField newPassword;
    private JPasswordField repeatNewPassword;
    private JLabel labPassword;
    private JLabel labConfirmPassword;

    private PasswordSeed passwordSeed;
    private IDialogPasswordListener listener;

    private ICheckPasswordListener checkPasswordListener;
    private boolean passwordEntered = false;
    private boolean checkPre = true;


    private ExecutorService executor;
    private boolean etPasswordConfirmIsVisible = false;
    private JProgressBar pb;
    private JLabel labStrength;

    private JPanel progressPanel;
    private JPanel newPasswordPanel;

    public PasswordPanel(IDialogPasswordListener dialogPasswordListener) {
        super(MessageKey.SHOW_CHANGE_PASSWORD_WIZARD, AwesomeIcon.LOCK);
        updateTitle(LocaliserUtils.getString("import_private_key_qr_code_password"));
        this.listener = dialogPasswordListener;
        passwordSeed = getPasswordSeed();
        progressPanel = getProgressPanel();
        newPasswordPanel = getNewPasswordPanel();
        setOkAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });


    }

    private PasswordSeed getPasswordSeed() {
        return PasswordSeed.getPasswordSeed();
    }

    @Override
    public void initialiseContent(JPanel panel) {

        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[]", // Column constraints
                "[][]" // Row constraints
        ));


        panel.add(progressPanel, "align center,shrink,wrap");

        panel.add(newPasswordPanel, "wrap");

        if (PasswordSeed.hasPasswordSeed()) {
            labConfirmPassword.setVisible(false);
            repeatNewPassword.setVisible(false);
        }

        passwordCheck.setCheckListener(passwordCheckListener);
        newPassword.addKeyListener(passwordWatcher);
        repeatNewPassword.addKeyListener(passwordWatcher);
        configureCheckPre();
        showCheckPre();


    }

    @Override
    public void showPanel() {
        super.showPanel();
        newPassword.requestFocus();
    }

    private JPanel getProgressPanel() {
        JPanel pbPanel = Panels.newPanel();
        pbPanel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[]", // Column constraints
                "[][]" // Row constraints
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
        pbPanel.add(pb, "align center,shrink");
        pbPanel.add(labStrength, "align center,shrink");
        return pbPanel;
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
                        if (!repeatNewPassword.isVisible()) {
                            return;
                        }
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
        labPassword = Labels.newEnterPassword();
        panel.add(labPassword);
        panel.add(newPassword, " wrap");

        labConfirmPassword = Labels.newRetypeNewPassword();
        panel.add(labConfirmPassword);
        panel.add(repeatNewPassword, "wrap");
        panel.add(verificationStatusLabel, "span 4,grow,push");

        return panel;
    }


    private KeyListener passwordWatcher = new KeyListener() {
        private SecureCharSequence password;
        private SecureCharSequence passwordConfirm;

        @Override
        public void keyTyped(KeyEvent keyEvent) {

        }

        @Override
        public void keyPressed(KeyEvent keyEvent) {
            if (password != null) {
                password.wipe();
            }
            if (passwordConfirm != null) {
                passwordConfirm.wipe();
            }
            password = new SecureCharSequence(newPassword.getPassword());
            passwordConfirm = new SecureCharSequence(repeatNewPassword.getPassword());

        }

        @Override
        public void keyReleased(KeyEvent keyEvent) {

            SecureCharSequence p = new SecureCharSequence(newPassword.getPassword());
            if (p.length() > 0) {
                if (!Utils.validPassword(p)) {
                    newPassword.setText(password.toString());
                }
            }
            p.wipe();
            if (repeatNewPassword.isVisible()) {
                SecureCharSequence pc = new SecureCharSequence(repeatNewPassword.getPassword());
                if (pc.length() > 0) {
                    if (!Utils.validPassword(pc)) {
                        repeatNewPassword.setText(passwordConfirm.toString());
                    }
                }
                pc.wipe();
            }
            checkValid();
            if (password != null) {
                password.wipe();
            }
            if (passwordConfirm != null) {
                passwordConfirm.wipe();
            }

        }
    };

    private void onOK() {

        SecureCharSequence password = new SecureCharSequence(newPassword.getPassword());

        SecureCharSequence passwordConfirm = new SecureCharSequence(repeatNewPassword.getPassword());

        if (password == null || password.length() == 0) {
            return;
        }
        if (passwordSeed == null && !password.equals(passwordConfirm) && checkPre) {
            password.wipe();
            passwordConfirm.wipe();
            new MessageDialog((LocaliserUtils.getString
                    ("add_address_generate_address_password_not_same"))).showMsg();
            repeatNewPassword.requestFocus();
            return;

        }
        PasswordStrengthUtil.PasswordStrength strength = PasswordStrengthUtil.checkPassword
                (password);
        password.wipe();
        passwordConfirm.wipe();
        if (UserPreference.getInstance().getCheckPasswordStrength() && repeatNewPassword.isVisible()) {
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
                                confirmPassword();
                            }
                        });
                    }
                });
                dialogConfirmTask.pack();
                dialogConfirmTask.setVisible(true);
                return;

            }
        }
        confirmPassword();
    }

    private void confirmPassword() {
        if ((passwordSeed != null && checkPre) || checkPasswordListener != null) {
            ArrayList<Check> checks = new ArrayList<Check>();
            checks.add(passwordCheck);
            executor = CheckUtil.runChecks(checks, 1);
        } else {
            passwordEntered = true;
            closePanel();
        }

    }

    private void configureCheckPre() {
        if (checkPre) {
            if (passwordSeed != null) {
                etPasswordConfirmIsVisible = false;
                repeatNewPassword.setVisible(false);
                labConfirmPassword.setVisible(false);
            } else {
                etPasswordConfirmIsVisible = true;
                repeatNewPassword.setVisible(true);
                labConfirmPassword.setVisible(true);
            }
        } else {
            etPasswordConfirmIsVisible = false;
            repeatNewPassword.setVisible(false);
            labConfirmPassword.setVisible(false);
        }
    }


    private void checkValid() {
        setOkEnabled(false);
        int passwordLength = newPassword.getPassword().length;
        if (passwordLength >= BitherSetting.PASSWORD_LENGTH_MIN && passwordLength <= BitherSetting.PASSWORD_LENGTH_MAX) {
            if (etPasswordConfirmIsVisible) {

                int passwordConfirmLength = repeatNewPassword.getPassword().length;
                if (passwordConfirmLength >= BitherSetting.PASSWORD_LENGTH_MIN && passwordConfirmLength <= BitherSetting.PASSWORD_LENGTH_MAX) {
                    setOkEnabled(true);
                } else {
                    setOkEnabled(false);
                }
            } else {
                setOkEnabled(true);
            }
        }
    }

    private void shake() {

    }

    public void setCheckPre(boolean check) {
        checkPre = check;
        configureCheckPre();
        showCheckPre();
    }


    public void setCheckPasswordListener(ICheckPasswordListener checkPasswordListener) {
        this.checkPasswordListener = checkPasswordListener;
    }

    public void showCheckPre() {
        if (checkPre) {
            if (etPasswordConfirmIsVisible) {
                updateTitle(LocaliserUtils.getString("add_address_generate_address_password_set_label"));

            } else {
                updateTitle(LocaliserUtils.getString("add_address_generate_address_password_label"));

            }
        }


    }

    private Check.CheckListener passwordCheckListener = new Check.CheckListener() {

        @Override
        public void onCheckBegin(Check check) {
            //  pb.setVisible(true);
        }

        @Override
        public void onCheckEnd(Check check, boolean success) {
            if (executor != null) {
                executor.shutdown();
                executor = null;
            }
            if (success) {
                passwordEntered = true;
                closePanel();

            } else {
                newPassword.setText("");
                checkValid();
                new MessageDialog(LocaliserUtils.getString("password_wrong")).showMsg();
                shake();

            }
        }
    };

    public void setTitle(String title) {
        updateTitle(title);
    }


    private Check passwordCheck = new Check("", new Check.ICheckAction() {
        @Override
        public boolean check() {
            SecureCharSequence password = new SecureCharSequence(newPassword.getPassword());
            if (checkPasswordListener != null) {
                boolean result = checkPasswordListener.checkPassword(password);
                password.wipe();
                return result;
            } else if (passwordSeed != null) {
                boolean result = passwordSeed.checkPassword(password);
                password.wipe();
                return result;
            } else {
                return true;
            }
        }
    });

    @Override
    public void closePanel() {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                PasswordPanel.super.closePanel();
                if (listener != null) {
                    if (passwordEntered) {

                        listener.onPasswordEntered(new SecureCharSequence(newPassword.getPassword()));
                        newPassword.setText("");
                        repeatNewPassword.setText("");
                    } else {
                        listener.onPasswordEntered(null);
                    }
                }

            }
        });

    }

    public static final class PasswordGetter implements IDialogPasswordListener, IPasswordGetter {

        private ReentrantLock getPasswordLock = new ReentrantLock();
        private Condition withPasswordCondition = getPasswordLock.newCondition();

        private SecureCharSequence password;
        private IPasswordGetterDelegate delegate;

        public PasswordGetter() {
            this(null);
        }

        public PasswordGetter(IPasswordGetterDelegate delegate) {
            this.delegate = delegate;
        }

        public void setPassword(SecureCharSequence password) {
            this.password = password;
        }

        public boolean hasPassword() {
            return password != null;
        }

        public SecureCharSequence getPassword() {
            if (password == null) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (delegate != null) {
                            delegate.beforePasswordDialogShow();
                        }
                        PasswordPanel d = new PasswordPanel(PasswordGetter.this);
                        d.showPanel();
                    }
                });
                try {
                    getPasswordLock.lockInterruptibly();
                    withPasswordCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    getPasswordLock.unlock();
                }
            }
            return password;
        }

        @Override
        public void onPasswordEntered(SecureCharSequence password) {
            setPassword(password);
            try {
                getPasswordLock.lock();
                withPasswordCondition.signal();
            } finally {
                getPasswordLock.unlock();
            }
            if (delegate != null && password != null) {
                delegate.afterPasswordDialogDismiss();
            }
        }

        public void wipe() {
            if (password != null) {
                password.wipe();
                password = null;
            }
        }
    }
}
