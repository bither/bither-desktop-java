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
import net.bither.viewsystem.dialogs.DialogConfirmTask;
import net.bither.viewsystem.dialogs.DialogPassword;
import net.bither.viewsystem.dialogs.DialogProgress;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.listener.IDialogPasswordListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class AdvancePanel extends WizardPanel {
    private JRadioButton rbNormal;
    private JRadioButton rbLow;
    private JButton btnSwitchCold;
    private JButton btnReloadTx;
    private JButton btnRecovery;
    private JButton btnRestHDMPassword;
    private DialogProgress dp;
    private HDMKeychainRecoveryUtil hdmRecoveryUtil;
    private HDMResetServerPasswordUtil hdmResetServerPasswordUtil;

    public AdvancePanel() {
        super(MessageKey.ADVANCE, AwesomeIcon.FA_BOOK, true);
        dp = new DialogProgress();
        hdmRecoveryUtil = new HDMKeychainRecoveryUtil(dp);
    }

    @Override
    public void initialiseContent(JPanel panel) {

        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][]80[][][][]10", // Column constraints
                "[][][][][][]80[]20[][][]" // Row constraints
        ));
        rbLow = getRbLow();
        rbNormal = getRbNormal();
        ButtonGroup group = new ButtonGroup();
        group.add(rbLow);
        group.add(rbNormal);
        if (UserPreference.getInstance().getTransactionFeeMode() == BitherjSettings.TransactionFeeMode.Normal) {
            rbNormal.setSelected(true);

        } else {
            rbLow.setSelected(true);
        }
        rbNormal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                UserPreference.getInstance().setTransactionFeeMode(BitherjSettings.TransactionFeeMode.Normal);

            }
        });
        rbLow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                UserPreference.getInstance().setTransactionFeeMode(BitherjSettings.TransactionFeeMode.Low);
            }
        });
        JLabel label = Labels.newValueLabel(LocaliserUtils.getString("setting_name_transaction_fee"));

        panel.add(label, "push,align left");
        panel.add(rbNormal, "push,align left");
        panel.add(rbLow, "push,align left,wrap");
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
                                        } else {
                                            Bither.refreshFrame();
                                        }
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

    private JRadioButton getRbNormal() {
        JRadioButton jRadioButton = new JRadioButton();
        jRadioButton.setText(LocaliserUtils.getString("setting_name_transaction_fee_normal"));
        return jRadioButton;
    }

    private JRadioButton getRbLow() {
        JRadioButton jRadioButton = new JRadioButton();

        jRadioButton.setText(LocaliserUtils.getString("setting_name_transaction_fee_low"));
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
                    DialogPassword dialogPassword = new DialogPassword(new IDialogPasswordListener() {
                        @Override
                        public void onPasswordEntered(SecureCharSequence password) {
                            resetTx();

                        }
                    });
                    dialogPassword.pack();
                    dialogPassword.setVisible(true);
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
                PeerUtil.stopPeer();
                for (Address address : AddressManager.getInstance().getAllAddresses()) {
                    address.setSyncComplete(false);
                    address.updateSyncComplete();

                }
                TxProvider.getInstance().clearAllTx();
                for (Address address : AddressManager.getInstance().getAllAddresses()) {
                    address.notificatTx(null, Tx.TxNotificationType.txFromApi);
                }

                try {
                    if (!AddressManager.getInstance().addressIsSyncComplete()) {
                        TransactionsUtil.getMyTxFromBither();
                    }
                    PeerUtil.startPeer();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
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
