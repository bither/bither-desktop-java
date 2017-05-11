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

import net.bither.Bither;
import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.HDMBId;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.crypto.PasswordSeed;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.utils.TransactionsUtil;
import net.bither.db.HDAccountProvider;
import net.bither.db.TxProvider;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.preference.UserPreference;
import net.bither.utils.HDMKeychainRecoveryUtil;
import net.bither.utils.HDMResetServerPasswordUtil;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.PeerUtil;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.base.RadioButtons;
import net.bither.viewsystem.dialogs.DialogConfirmTask;
import net.bither.viewsystem.dialogs.DialogProgress;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.listener.IDialogPasswordListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class AdvancePanel extends WizardPanel {
    private JRadioButton rbNormal;
    private JRadioButton rbHigh;
    private JRadioButton rbHigher;
    private JRadioButton rbTimes10;

    private JRadioButton rbApiBlockchain;
    private JRadioButton rbApiBither;

    private JButton btnSwitchCold;
    private JButton btnReloadTx;
    private JButton btnRecovery;
    private JButton btnRestHDMPassword;
    private DialogProgress dp;
    private HDMKeychainRecoveryUtil hdmRecoveryUtil;
    private HDMResetServerPasswordUtil hdmResetServerPasswordUtil;

    public AdvancePanel() {
        super(MessageKey.ADVANCE, AwesomeIcon.FA_BOOK);
        dp = new DialogProgress();
        hdmRecoveryUtil = new HDMKeychainRecoveryUtil(dp);
    }

    @Override
    public void initialiseContent(JPanel panel) {
        System.out.println(UserPreference.getInstance().getTransactionFeeMode().getMinFeeSatoshi() + ".............................................");

        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][]", // Column constraints
                "[][][][][][]" // Row constraints
        ));

        rbNormal = getRbNormal();
        rbHigh = getRbHigh();
        rbHigher = getRbHigher();
        rbTimes10 = getRbTimes10();

        ButtonGroup groupFee = new ButtonGroup();
        groupFee.add(rbHigh);
        groupFee.add(rbHigher);
        groupFee.add(rbTimes10);
        groupFee.add(rbNormal);

        switch (UserPreference.getInstance().getTransactionFeeMode()){
            case Normal:
                rbNormal.setSelected(true);
                break;
            case High:
                rbHigh.setSelected(true);
                break;
            case Higher:
                rbHigher.setSelected(true);
                break;
            case Times10:
                rbTimes10.setSelected(true);
                break;
        }

        JLabel label = Labels.newValueLabel(LocaliserUtils.getString("setting_name_transaction_fee"));
        panel.add(label, "push,gaptop 12,span 1 4,align left top");
        panel.add(rbNormal, "push,wrap");
        panel.add(rbHigh, "push,wrap");
        panel.add(rbHigher, "push,wrap");
        panel.add(rbTimes10, "push,wrap");

        rbApiBlockchain = getRbApiConfigBlockchain();
        rbApiBither = getRbApiConfigBither();

        ButtonGroup groupApiConfig = new ButtonGroup();
        groupApiConfig.add(rbApiBlockchain);
        groupApiConfig.add(rbApiBither);
        if (UserPreference.getInstance().getApiConfig() == BitherjSettings.ApiConfig.BLOCKCHAIN_INFO){
            rbApiBlockchain.setSelected(true);
        } else {
            rbApiBither.setSelected(true);
        }
        JLabel labelApi = Labels.newValueLabel(LocaliserUtils.getString("setting_name_api_config"));
        panel.add(labelApi, "push,align left");
        panel.add(rbApiBlockchain, "push,align left");
        panel.add(rbApiBither, "push,align left,wrap");

        JCheckBox cbCheckPassword = RadioButtons.newCheckPassword();
        panel.add(cbCheckPassword, "push,align left,wrap");
//        panel.add(rbCheckPWDOn, "push,align left");
//        panel.add(rbCheckPEDOff, "push,align left,wrap");


        if (AddressManager.getInstance().getAllAddresses().size() == 0 && UserPreference.getInstance().getAppMode() == BitherjSettings.AppMode.HOT) {
            btnSwitchCold = Buttons.newLargeSwitchColdWizardButton(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    switchColdWallet();


                }
            });

            panel.add(btnSwitchCold, "push,align left");

        }
        btnReloadTx = Buttons.newLargeReloadTxWizardButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reloadTx();
            }
        });
        panel.add(btnReloadTx, "push,align left");
        if (hdmRecoveryUtil.canRecover()) {
            btnRecovery = Buttons.newLargeRecoveryButton(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    closePanel();
                    if (!hdmRecoveryUtil.canRecover()) {
                        return;
                    }
                    new Thread() {
                        @Override
                        public void run() {
                            PeerUtil.stopPeer();
                            try {
                                final String result = hdmRecoveryUtil.recovery();
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        configureHDMRecovery();
                                        if (result != null) {
                                            new MessageDialog(result).showMsg();
                                        }
                                        Bither.refreshFrame();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();

                            }
                            PeerUtil.startPeer();
                        }

                    }.start();

                }
            });
            panel.add(btnRecovery, "push,align left");


        }
        if (HDMBId.getHDMBidFromDb() != null) {
            btnRestHDMPassword = Buttons.newLargeRestPasswordButton(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    closePanel();
                    restHDMPassword();
                }
            });
            panel.add(btnRestHDMPassword, "push,align left");
        }

    }

    private void restHDMPassword() {

        DialogConfirmTask dialogConfirmTask = new DialogConfirmTask(LocaliserUtils.getString("hdm_reset_server_password_confirm"), new Runnable() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dp.pack();
                        dp.setVisible(true);
                    }
                });

                hdmResetServerPasswordUtil = new HDMResetServerPasswordUtil(dp);
                final boolean result = hdmResetServerPasswordUtil.changePassword();
                hdmResetServerPasswordUtil = null;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dp.dispose();
                        if (result) {
                            new MessageDialog(LocaliserUtils.getString("hdm_reset_server_password_success")).showMsg();
                        }
                    }
                });
            }
        });
        dialogConfirmTask.pack();
        dialogConfirmTask.setVisible(true);
    }

    private void configureHDMRecovery() {
        if (hdmRecoveryUtil.canRecover()) {
            btnRecovery.setVisible(true);
        } else {
            btnRecovery.setVisible(false);
        }
    }


    private JRadioButton getRbPWDOn() {
        JRadioButton jRadioButton = new JRadioButton();
        jRadioButton.setText(LocaliserUtils.getString("password_strength_check_on"));
        jRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UserPreference.getInstance().setCheckPasswordStrength(true);
            }
        });
        return jRadioButton;
    }

    private JRadioButton getRbPWDOff() {
        JRadioButton jRadioButton = new JRadioButton();
        jRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DialogConfirmTask dialogConfirmTask = new DialogConfirmTask(LocaliserUtils.getString("password_strength_check_off"), new Runnable() {
                    @Override
                    public void run() {
                        UserPreference.getInstance().setCheckPasswordStrength(false);
                    }
                });
                dialogConfirmTask.pack();
                dialogConfirmTask.setVisible(true);

            }
        });

        jRadioButton.setText(LocaliserUtils.getString("setting_name_transaction_fee_high"));
        return jRadioButton;

    }


    private JRadioButton getRbNormal() {
        JRadioButton jRadioButton = new JRadioButton();
        jRadioButton.setText(LocaliserUtils.getString("setting_name_transaction_fee_normal"));
        jRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                UserPreference.getInstance().setTransactionFeeMode(BitherjSettings.TransactionFeeMode.Normal);

            }
        });
        return jRadioButton;
    }

    private JRadioButton getRbHigh() {
        JRadioButton jRadioButton = new JRadioButton();

        jRadioButton.setText(LocaliserUtils.getString("setting_name_transaction_fee_high"));
        jRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                UserPreference.getInstance().setTransactionFeeMode(BitherjSettings.TransactionFeeMode.High);
            }
        });
        return jRadioButton;

    }

    private JRadioButton getRbHigher() {
        JRadioButton jRadioButton = new JRadioButton();

        jRadioButton.setText(LocaliserUtils.getString("setting_name_transaction_fee_higher"));
        jRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                UserPreference.getInstance().setTransactionFeeMode(BitherjSettings.TransactionFeeMode.Higher);
            }
        });
        return jRadioButton;

    }

    private JRadioButton getRbTimes10() {
        JRadioButton jRadioButton = new JRadioButton();

        jRadioButton.setText(LocaliserUtils.getString("setting_name_transaction_fee_times10"));
        jRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                UserPreference.getInstance().setTransactionFeeMode(BitherjSettings.TransactionFeeMode.Times10);
            }
        });
        return jRadioButton;

    }

    private JRadioButton getRbApiConfigBlockchain(){
        JRadioButton jRadioButton = new JRadioButton();

        jRadioButton.setText(LocaliserUtils.getString("setting_name_api_config_blockchain"));
        jRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                UserPreference.getInstance().setApiConfig(BitherjSettings.ApiConfig.BLOCKCHAIN_INFO);
            }
        });
        return jRadioButton;
    }

    private JRadioButton getRbApiConfigBither(){
        JRadioButton jRadioButton = new JRadioButton();

        jRadioButton.setText(LocaliserUtils.getString("setting_name_api_config_bither"));
        jRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                UserPreference.getInstance().setApiConfig(BitherjSettings.ApiConfig.BITHER_NET);
            }
        });
        return jRadioButton;
    }

    private void reloadTx() {

        if (Bither.canReloadTx()) {
            Runnable confirmRunnable = new Runnable() {
                @Override
                public void run() {
                    Bither.reloadTxTime = System.currentTimeMillis();
                    PasswordSeed passwordSeed = PasswordSeed.getPasswordSeed();
                    if (passwordSeed == null) {
                        resetTx();
                    } else {
                        callPassword();
                    }
                }
            };
            DialogConfirmTask dialogConfirmTask = new DialogConfirmTask(
                    LocaliserUtils.getString("reload_tx_need_too_much_time"), confirmRunnable
            );
            dialogConfirmTask.pack();
            dialogConfirmTask.setVisible(true);
        } else {
            new MessageDialog(LocaliserUtils.getString("tx_cannot_reloding")).showMsg();

        }


    }

    private void callPassword() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (PasswordSeed.hasPasswordSeed()) {
                    closePanel();
                    PasswordPanel dialogPassword = new PasswordPanel(new IDialogPasswordListener() {
                        @Override
                        public void onPasswordEntered(SecureCharSequence password) {
                            resetTx();

                        }
                    });
                    dialogPassword.showPanel();

                } else {
                    resetTx();
                }
            }
        });
    }

    private void resetTx() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dp.pack();
                        dp.setVisible(true);
                    }
                });
                try {
                    PeerUtil.stopPeer();
                    for (Address address : AddressManager.getInstance().getAllAddresses()) {
                        address.setSyncComplete(false);
                        address.updateSyncComplete();

                    }
                    HDAccountProvider.getInstance().setSyncdNotComplete();
                    TxProvider.getInstance().clearAllTx();
                    for (Address address : AddressManager.getInstance().getAllAddresses()) {
                        address.notificatTx(null, Tx.TxNotificationType.txFromApi);
                    }

                    if (!AddressManager.getInstance().addressIsSyncComplete()) {
                        TransactionsUtil.getMyTxFromBither();
                    }
                    PeerUtil.startPeer();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            Bither.refreshFrame();
                            dp.dispose();
                            new MessageDialog(LocaliserUtils.getString("reload_tx_success")).showMsg();
                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dp.dispose();
                            new MessageDialog(LocaliserUtils.getString("network_or_connection_error")).showMsg();
                        }
                    });


                }

            }


        }).start();
    }

    private void switchColdWallet() {
        DialogConfirmTask dialog = new DialogConfirmTask(LocaliserUtils.getString("launch_sequence_switch_to_cold_warn")
                , new Runnable() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        PeerUtil.stopPeer();
                        Panels.hideLightBoxIfPresent();
                        UserPreference.getInstance().setAppMode(BitherjSettings.AppMode
                                .COLD);
                        Bither.refreshFrame();

                    }
                });


            }
        });
        dialog.pack();
        dialog.setVisible(true);


    }

}
