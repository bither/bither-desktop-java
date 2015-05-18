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
import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.HDAccount;
import net.bither.bitherj.core.HDMKeychain;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.model.AddressCheck;
import net.bither.model.Check;
import net.bither.model.CheckPrivateKeyTableModel;
import net.bither.preference.UserPreference;
import net.bither.utils.CheckUtil;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.FontSizer;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.base.renderer.AddressRenderer;
import net.bither.viewsystem.base.renderer.CheckImageRenderer;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.listener.IDialogPasswordListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class CheckPrivateKeyPanel extends WizardPanel implements IDialogPasswordListener {


    private static final int PrivateKeyCheckThreadCount = 1;
    private static final int ListExpandAnimDuration = 500;


    private JTable table;
    private JButton checkNowButton;

    private List<AddressCheck> addressCheckList;
    private int point = 100;


    private int checkCount;
    private int checkFinishedCount;

    private ArrayList<CheckPoint> checkPoints = new ArrayList<CheckPoint>();
    private CheckPrivateKeyTableModel checkPrivateKeyTableModel;


    public CheckPrivateKeyPanel() {
        super(MessageKey.CHECK_PRIVATE_KEY, AwesomeIcon.SHIELD);
    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[]", // Column constraints
                "[]" // Row constraints
        ));

        addressCheckList = new ArrayList<AddressCheck>();
        HDAccount hdAccount = AddressManager.getInstance().getHdAccount();
        if (hdAccount != null) {
            addressCheckList.add(new AddressCheck(AddressCheck.CheckType.HDAccount, LocaliserUtils.getString("add_hd_account_tab_hd"),
                    AddressCheck.CheckStatus.Prepare));
        }

        HDMKeychain keychain = AddressManager.getInstance().getHdmKeychain();
        if (keychain != null) {
            String str = LocaliserUtils.getString("hdm_keychain_check_title_cold");
            if (UserPreference.getInstance().getAppMode() == BitherjSettings.AppMode.HOT) {
                if (keychain.isInRecovery()) {
                    str = LocaliserUtils.getString("address_group_hdm_recovery");
                } else {
                    str = LocaliserUtils.getString("hdm_keychain_check_title_hot");
                }

            }
            addressCheckList.add(new AddressCheck(AddressCheck.CheckType.HDMKeyChain, str,
                    AddressCheck.CheckStatus.Prepare));
        }
        for (Address address : AddressManager.getInstance().getPrivKeyAddresses()) {
            addressCheckList.add(new AddressCheck(address, AddressCheck.CheckStatus.Prepare));
        }
        checkPrivateKeyTableModel = new CheckPrivateKeyTableModel(addressCheckList);
        table = new JTable(checkPrivateKeyTableModel);

        table.setOpaque(false);
        table.setBorder(BorderFactory.createEmptyBorder());
        table.setRowHeight(Math.max(BitherSetting.MINIMUM_ICON_HEIGHT, panel.getFontMetrics(FontSizer.INSTANCE.getAdjustedDefaultFont()).getHeight()) + BitherSetting.HEIGHT_DELTA);
        table.getColumnModel().getColumn(1).setCellRenderer(new CheckImageRenderer());
        table.getColumnModel().getColumn(0).setCellRenderer(new AddressRenderer());

        checkNowButton = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (AddressManager.getInstance().getPrivKeyAddresses().size() > 0 || AddressManager.getInstance().getHdmKeychain() != null
                        || AddressManager.getInstance().getHdAccount() != null) {
                    PasswordPanel dialogPassword = new PasswordPanel((CheckPrivateKeyPanel.this));
                    dialogPassword.showPanel();


                } else {
                    new MessageDialog(LocaliserUtils.getString("private_key_is_empty")).showMsg();
                }
            }
        }, MessageKey.CHECK_PRIVATE_KEY, AwesomeIcon.CHECK);

        JScrollPane jScrollPane = new JScrollPane(table);
        panel.add(jScrollPane, "push,grow,align center, wrap");
        panel.add(checkNowButton, "push,shrink,align right");

    }

    private void check(final List<Check> checks, final int threadCount) {
        checkCount = checks.size();
        checkFinishedCount = 0;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                CheckUtil.runChecks(checks, threadCount);
            }
        });
//        checkHeaderView.setTotalCheckCount(checkCount);
//        checkHeaderView.setPassedCheckCount(0);

    }

    public void beginCheck(SecureCharSequence password) {
        for (AddressCheck addressCheck : addressCheckList) {
            addressCheck.setCheckStatus(AddressCheck.CheckStatus.Prepare);
        }
        checkPrivateKeyTableModel.fireTableDataChanged();

        checkPoints.clear();
        final ArrayList<Check> checks = new ArrayList<Check>();
        for (int i = 0; i < addressCheckList.size(); i++) {
            AddressCheck addressCheck = addressCheckList.get(i);
            CheckPoint point;
            switch (addressCheck.getCheckType()) {
                case Address:
                    point = new CheckPoint(addressCheck);
                    checkPoints.add(point);
                    checks.add(CheckUtil.initCheckForPrivateKey(addressCheck.getAddress(), new SecureCharSequence(password))
                            .setCheckListener(point));
                    break;
                case HDMKeyChain:
                    point = new CheckPoint(addressCheck);
                    checkPoints.add(point);
                    checks.add(CheckUtil.initCheckForHDMKeychain(AddressManager.getInstance()
                            .getHdmKeychain(), new SecureCharSequence(password)).setCheckListener(point));

                    break;
                case HDAccount:
                    point = new CheckPoint(addressCheck);
                    checkPoints.add(point);
                    checks.add(CheckUtil.initCheckForHDAccount(AddressManager.getInstance()
                            .getHdAccount(), new SecureCharSequence(password)).setCheckListener(point));

                    break;
            }
            if (addressCheck.getCheckType() != AddressCheck.CheckType.Address) {

            } else {
            }
        }
        password.wipe();
        checkPrivateKeyTableModel.fireTableDataChanged();

        check(checks, PrivateKeyCheckThreadCount);

    }

    @Override
    public void onPasswordEntered(SecureCharSequence password) {
        if (password == null) {
            return;
        }
        beginCheck(password);
    }

    private class CheckPoint implements Check.CheckListener {
        private boolean waiting;
        private boolean checking;
        private boolean result;
        private AddressCheck addressCheck;

        public CheckPoint(AddressCheck addressCheck) {
            waiting = true;
            this.addressCheck = addressCheck;
        }

        @Override
        public void onCheckBegin(Check check) {
            checking = true;
            waiting = false;
            checkPrivateKeyTableModel.fireTableDataChanged();
        }

        @Override
        public void onCheckEnd(Check check, boolean success) {
            checking = false;
            result = success;
            checkFinishedCount++;
            if (success) {
                this.addressCheck.setCheckStatus(AddressCheck.CheckStatus.Success);
                // checkHeaderView.addPassedCheckCount();
            } else {
                this.addressCheck.setCheckStatus(AddressCheck.CheckStatus.Failed);
            }
            checkPrivateKeyTableModel.fireTableDataChanged();
//            adapter.notifyDataSetChanged();
//            if (checkFinishedCount >= checkCount) {
//                vCheckHeader.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        vCheckHeader.stop();
//                    }
//                }, 600);
//            }
        }

        public boolean isWaiting() {
            return waiting;
        }

        public boolean isChecking() {
            return checking;
        }

        public boolean getResult() {
            return result;
        }

        public AddressCheck getAddressCheck() {
            return addressCheck;
        }
    }
}
