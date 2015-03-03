package net.bither.viewsystem.dialogs;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import net.bither.BitherSetting;
import net.bither.bitherj.crypto.PasswordSeed;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.delegate.IPasswordGetter;
import net.bither.bitherj.delegate.IPasswordGetterDelegate;
import net.bither.bitherj.utils.Utils;
import net.bither.model.Check;
import net.bither.model.Check.CheckListener;
import net.bither.model.Check.ICheckAction;
import net.bither.utils.CheckUtil;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.listener.ICheckPasswordListener;
import net.bither.viewsystem.listener.IDialogPasswordListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DialogPassword extends BitherDialog {

    private JPanel contentPane;
    private JButton btnOK;
    private JButton btnCancel;
    private JPasswordField etPassword;
    private JPasswordField etPasswordConfirm;
    private JLabel labPassword;
    private JLabel labConfirmPassword;
    private JLabel tvError;
    private JLabel labTitle;

    private PasswordSeed passwordSeed;
    private IDialogPasswordListener listener;

    private ICheckPasswordListener checkPasswordListener;
    private boolean passwordEntered = false;
    private boolean checkPre = true;
    private boolean cancelable = true;
    private boolean needCancelEvent = false;
    private ExecutorService executor;
    private boolean etPasswordConfirmIsVisible = false;


    public DialogPassword(IDialogPasswordListener dialogPasswordListener) {
        labTitle.setText(LocaliserUtils.getString("addPasswordSubmitAction.text"));
        this.listener = dialogPasswordListener;
        passwordSeed = getPasswordSeed();
        initUI();
        configureCheckPre();
        showCheckPre();
    }

    private PasswordSeed getPasswordSeed() {
        return PasswordSeed.getPasswordSeed();
    }

    public void initUI() {
        try {
            setContentPane(contentPane);
            setModal(true);
            Buttons.modifCanelButton(btnCancel);
            Buttons.modifOkButton(btnOK);
            btnOK.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onOK();
                }
            });
            btnCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onCancel();
                }
            });
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    onCancel();
                }
            });
            contentPane.registerKeyboardAction(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onCancel();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            setUIText();
            if (PasswordSeed.hasPasswordSeed()) {
                labConfirmPassword.setVisible(false);
                etPasswordConfirm.setVisible(false);
            }
            initDialog();
            passwordCheck.setCheckListener(passwordCheckListener);
            etPassword.addKeyListener(passwordWatcher);
            etPasswordConfirm.addKeyListener(passwordWatcher);

        } catch (NullPointerException npe) {
            npe.printStackTrace();

        }


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
            password = new SecureCharSequence(etPassword.getPassword());
            passwordConfirm = new SecureCharSequence(etPasswordConfirm.getPassword());

        }

        @Override
        public void keyReleased(KeyEvent keyEvent) {
            tvError.setVisible(false);
            SecureCharSequence p = new SecureCharSequence(etPassword.getPassword());
            if (p.length() > 0) {
                if (!Utils.validPassword(p)) {
                    etPassword.setText(password.toString());
                }
            }
            p.wipe();
            if (etPasswordConfirm.isVisible()) {
                SecureCharSequence pc = new SecureCharSequence(etPasswordConfirm.getPassword());
                if (pc.length() > 0) {
                    if (!Utils.validPassword(pc)) {
                        etPasswordConfirm.setText(passwordConfirm.toString());
                    }
                }
                pc.wipe();
            }
            checkValid();
            password.wipe();
            passwordConfirm.wipe();

        }
    };

    public void setTitleText(String text) {
        labTitle.setText(text);

    }

    private void setUIText() {
        tvError.setText("");

    }

    private void onOK() {
        SecureCharSequence password = new SecureCharSequence(etPassword.getPassword());
        SecureCharSequence passwordConfirm = new SecureCharSequence(etPasswordConfirm.getPassword());
        if (passwordSeed == null && !password.equals(passwordConfirm) && checkPre) {
            password.wipe();
            passwordConfirm.wipe();
            tvError.setText(LocaliserUtils.getString("add_address_generate_address_password_not_same"));
            tvError.setVisible(true);
            etPasswordConfirm.requestFocus();
            return;

        }
        password.wipe();
        passwordConfirm.wipe();
        if ((passwordSeed != null && checkPre) || checkPasswordListener != null) {
            ArrayList<Check> checks = new ArrayList<Check>();
            checks.add(passwordCheck);
            executor = CheckUtil.runChecks(checks, 1);
        } else {
            passwordEntered = true;
            dismiss();
        }


    }

    private void configureCheckPre() {
        if (checkPre) {
            if (passwordSeed != null) {
                etPasswordConfirmIsVisible = false;
                etPasswordConfirm.setVisible(false);
                labConfirmPassword.setVisible(false);
            } else {
                etPasswordConfirmIsVisible = true;
                etPasswordConfirm.setVisible(true);
                labConfirmPassword.setVisible(true);
            }
        } else {
            etPasswordConfirmIsVisible = false;
            etPasswordConfirm.setVisible(false);
            labConfirmPassword.setVisible(false);
        }
    }

    private void dismiss() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    if (passwordEntered) {

                        listener.onPasswordEntered(new SecureCharSequence(etPassword.getPassword()));
                        etPassword.setText("");
                        etPasswordConfirm.setText("");
                    } else if (needCancelEvent) {
                        listener.onPasswordEntered(null);
                    }
                }
                dispose();
            }
        });

    }

    private void checkValid() {
        btnOK.setEnabled(false);
        int passwordLength = etPassword.getPassword().length;
        if (passwordLength >= BitherSetting.PASSWORD_LENGTH_MIN && passwordLength <= BitherSetting.PASSWORD_LENGTH_MAX) {
            if (etPasswordConfirmIsVisible) {

                int passwordConfirmLength = etPasswordConfirm.getPassword().length;
                if (passwordConfirmLength >= BitherSetting.PASSWORD_LENGTH_MIN && passwordConfirmLength <= BitherSetting.PASSWORD_LENGTH_MAX) {
                    btnOK.setEnabled(true);
                } else {
                    btnOK.setEnabled(false);
                }
            } else {
                btnOK.setEnabled(true);
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
                labTitle.setText(LocaliserUtils.getString("add_address_generate_address_password_set_label"));

            } else {
                labTitle.setText(LocaliserUtils.getString("add_address_generate_address_password_label"));

            }
        }
        if (cancelable) {

            btnCancel.setVisible(true);
        } else {
            btnCancel.setVisible(false);
        }

    }

    private CheckListener passwordCheckListener = new CheckListener() {

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
                dismiss();

            } else {
                etPassword.setText("");
                checkValid();
                tvError.setText(LocaliserUtils.getString("password_wrong"));
                tvError.setVisible(true);
                shake();

            }
        }
    };

    public void setTitle(String title) {
        contentPane.setToolTipText(title);
    }


    private Check passwordCheck = new Check("", new ICheckAction() {
        @Override
        public boolean check() {
            SecureCharSequence password = new SecureCharSequence(etPassword.getPassword());
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


    private void onCancel() {
        passwordEntered = false;
        dispose();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(3, 1, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.setBackground(new Color(-328966));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setOpaque(false);
        contentPane.add(panel1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel2.setOpaque(false);
        panel1.add(panel2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnOK = new JButton();
        this.$$$loadButtonText$$$(btnOK, ResourceBundle.getBundle("viewer").getString("ok"));
        panel2.add(btnOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnCancel = new JButton();
        this.$$$loadButtonText$$$(btnCancel, ResourceBundle.getBundle("viewer").getString("close"));
        panel2.add(btnCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tvError = new JLabel();
        tvError.setText("Label");
        panel1.add(tvError, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.setOpaque(false);
        contentPane.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        labConfirmPassword = new JLabel();
        labConfirmPassword.setHorizontalAlignment(4);
        labConfirmPassword.setHorizontalTextPosition(4);
        this.$$$loadLabelText$$$(labConfirmPassword, ResourceBundle.getBundle("viewer").getString("repeat_password_prompt"));
        panel3.add(labConfirmPassword, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        etPasswordConfirm = new JPasswordField();
        panel3.add(etPasswordConfirm, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        labPassword = new JLabel();
        labPassword.setHorizontalAlignment(4);
        labPassword.setHorizontalTextPosition(4);
        this.$$$loadLabelText$$$(labPassword, ResourceBundle.getBundle("viewer").getString("password_prompt"));
        labPassword.setVerifyInputWhenFocusTarget(false);
        panel3.add(labPassword, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        etPassword = new JPasswordField();
        etPassword.setHorizontalAlignment(10);
        panel3.add(etPassword, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        labTitle = new JLabel();
        labTitle.setHorizontalAlignment(0);
        labTitle.setHorizontalTextPosition(0);
        labTitle.setText("Label");
        contentPane.add(labTitle, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadLabelText$$$(JLabel component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
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
                        DialogPassword d = new DialogPassword(PasswordGetter.this);
                        d.pack();
                        d.setVisible(true);
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
