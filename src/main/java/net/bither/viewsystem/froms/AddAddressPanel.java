package net.bither.viewsystem.froms;

import net.bither.Bither;
import net.bither.BitherSetting;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.preference.UserPreference;
import net.bither.utils.KeyUtil;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.WalletUtils;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.PasswordDialog;
import net.bither.viewsystem.listener.IDialogPasswordListener;
import net.bither.xrandom.UEntropyDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AddAddressPanel extends WizardPanel implements IDialogPasswordListener {
    private JSpinner spinnerCount;
    private JCheckBox xrandomCheckBox;

    public AddAddressPanel() {
        super(MessageKey.ADD, AwesomeIcon.PLUS,false);

        setOkAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Panels.hideLightBoxIfPresent();
                generateKey();
            }
        });
    }


    private void generateKey() {
        PasswordDialog passwordDialog = new PasswordDialog(this);
        passwordDialog.pack();
        passwordDialog.setVisible(true);

    }

    @Override
    public void onPasswordEntered(SecureCharSequence password) {
        int targetCount = Integer.valueOf(spinnerCount.getValue().toString());
        if (!xrandomCheckBox.isSelected()) {
            KeyUtil.addPrivateKeyByRandomWithPassphras(null, password, targetCount);
            password.wipe();
            Bither.getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Bither.getCoreController().fireRecreateAllViews(true);
            Bither.getCoreController().fireDataChangedUpdateNow();
            if (Bither.getMainFrame() != null) {
                Bither.getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
            Bither.getMainFrame().getMainFrameUi().clearScroll();
        } else {
            UEntropyDialog uEntropyDialog = new UEntropyDialog(targetCount, password);
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
