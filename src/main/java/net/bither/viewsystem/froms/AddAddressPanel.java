package net.bither.viewsystem.froms;

import net.bither.Bither;
import net.bither.BitherSetting;
import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.delegate.IPasswordGetterDelegate;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.preference.UserPreference;
import net.bither.utils.KeyUtil;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.WalletUtils;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.DialogPassword;
import net.bither.xrandom.PrivateKeyUEntropyDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AddAddressPanel extends WizardPanel implements IPasswordGetterDelegate {
    private JSpinner spinnerCount;
    private JCheckBox xrandomCheckBox;
    private DialogPassword.PasswordGetter passwordGetter;

    public AddAddressPanel() {
        super(MessageKey.ADD, AwesomeIcon.PLUS, false);

        setOkAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Panels.hideLightBoxIfPresent();
                generateKey();
            }
        });
        passwordGetter = new DialogPassword.PasswordGetter(AddAddressPanel.this);

    }


    private void generateKey() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                passwordGetter.getPassword();
            }
        }).start();

    }


    @Override
    public void beforePasswordDialogShow() {

    }

    @Override
    public void afterPasswordDialogDismiss() {
        int targetCount = Integer.valueOf(spinnerCount.getValue().toString());
        if (!xrandomCheckBox.isSelected()) {
            KeyUtil.addPrivateKeyByRandomWithPassphras(null, passwordGetter.getPassword(), targetCount);
            passwordGetter.wipe();
            Bither.refreshFrame();
        } else {
            PrivateKeyUEntropyDialog uEntropyDialog = new PrivateKeyUEntropyDialog(targetCount, passwordGetter);
            uEntropyDialog.pack();
            uEntropyDialog.setVisible(true);


        }


    }

    private int getMaxCount() {
        int max = 0;
        if (UserPreference.getInstance().getAppMode() == BitherjSettings.AppMode.COLD) {
            max = BitherSetting.WATCH_ONLY_ADDRESS_COUNT_LIMIT - AddressManager.getInstance()
                    .getAllAddresses().size();
        } else {
            max = BitherSetting.PRIVATE_KEY_OF_HOT_COUNT_LIMIT - AddressManager.getInstance()
                    .getPrivKeyAddresses().size();
        }
        return max;
    }

    @Override
    public void initialiseContent(JPanel panel) {

        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[]", // Column constraints
                "[][][][]80[]20[]" // Row constraints
        ));
        spinnerCount = new JSpinner();
        panel.add(spinnerCount, "align center,cell 0 2 ,wrap");
        xrandomCheckBox = new JCheckBox();
        xrandomCheckBox.setSelected(true);
        xrandomCheckBox.setText(LocaliserUtils.getString("xrandom"));
        panel.add(xrandomCheckBox, "align center,cell 0 3,wrap");

        if (WalletUtils.isPrivateLimit()) {
            spinnerCount.setEnabled(false);
            setOkEnabled(false);
            xrandomCheckBox.setEnabled(false);
        } else {
            Integer value = new Integer(1);
            Integer min = new Integer(1);

            Integer max = new Integer(getMaxCount());
            Integer step = new Integer(1);
            SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, step);
            spinnerCount.setModel(model);
        }
        xrandomCheckBox.setSelected(true);

    }
}
