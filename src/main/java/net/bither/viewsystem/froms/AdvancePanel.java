package net.bither.viewsystem.froms;

import net.bither.Bither;
import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.BitherjSettings;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.crypto.PasswordSeed;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.db.TxProvider;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.preference.UserPreference;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.PeerUtil;
import net.bither.utils.TransactionsUtil;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.ConfirmTaskDialog;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.dialogs.PasswordDialog;
import net.bither.viewsystem.listener.IDialogPasswordListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;


public class AdvancePanel extends WizardPanel {
    private JRadioButton rbNormal;
    private JRadioButton rbLow;
    private JButton btnSwitchCold;
    private JButton btnReloadTx;

    public AdvancePanel() {
        super(MessageKey.ADVANCE, AwesomeIcon.FA_BOOK, true);
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
        JLabel label = Labels.newValueLabel(LocaliserUtils.getString("setting.name.transaction.fee"));

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


    }

    private JRadioButton getRbNormal() {
        JRadioButton jRadioButton = new JRadioButton();
        jRadioButton.setText(LocaliserUtils.getString("setting.name.transaction.fee.normal"));
        return jRadioButton;
    }

    private JRadioButton getRbLow() {
        JRadioButton jRadioButton = new JRadioButton();

        jRadioButton.setText(LocaliserUtils.getString("setting.name.transaction.fee.low"));
        return jRadioButton;

    }

    private void reloadTx() {

        if (TransactionsUtil.canReloadTx()) {
            Runnable confirmRunnable = new Runnable() {
                @Override
                public void run() {
                    TransactionsUtil.reloadTxTime = System.currentTimeMillis();
                    PasswordSeed passwordSeed = UserPreference.getInstance().getPasswordSeed();
                    if (passwordSeed == null) {
                        resetTx();
                    } else {
                        callPassword();
                    }
                }
            };
            ConfirmTaskDialog dialogConfirmTask = new ConfirmTaskDialog(
                    LocaliserUtils.getString("reload.tx.need.too.much.time"), confirmRunnable
            );
            dialogConfirmTask.pack();
            dialogConfirmTask.setVisible(true);
        } else {
            new MessageDialog(LocaliserUtils.getString("tx.cannot.reloding")).showMsg();

        }


    }

    private void callPassword() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (UserPreference.getInstance().getPasswordSeed() != null) {
                    PasswordDialog dialogPassword = new PasswordDialog(new IDialogPasswordListener() {
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
                try {
                    PeerUtil.stopPeer();
                    for (Address address : AddressManager.getInstance().getAllAddresses()) {
                        address.setSyncComplete(false);
                        address.updatePubkey();

                    }
                    TxProvider.getInstance().clearAllTx();
                    for (Address address : AddressManager.getInstance().getAllAddresses()) {
                        address.notificatTx(null, Tx.TxNotificationType.txFromApi);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    new MessageDialog(LocaliserUtils.getString("reload.tx.failed")).showMsg();

                    return;
                }
                try {
                    if (!AddressManager.getInstance().addressIsSyncComplete()) {
                        TransactionsUtil.getMyTxFromBither();
                    }
                    PeerUtil.startPeer();
                    new MessageDialog(LocaliserUtils.getString("reload.tx.success")).showMsg();

                } catch (Exception e) {
                    e.printStackTrace();
                    new MessageDialog(LocaliserUtils.getString("network.or.connection.error")).showMsg();

                }
            }


        }).start();


    }


    private void switchColdWallet() {
        ConfirmTaskDialog dialog = new ConfirmTaskDialog(LocaliserUtils.getString("launch.sequence.switch.to.cold.warn")
                , new Runnable() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Panels.hideLightBoxIfPresent();
                        Bither.getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        UserPreference.getInstance().setAppMode(BitherjSettings.AppMode
                                .COLD);
                        Bither.getCoreController().fireRecreateAllViews(true);
                        Bither.getCoreController().fireDataChangedUpdateNow();
                        if (Bither.getMainFrame() != null) {
                            Bither.getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        }
                    }
                });


            }
        });
        dialog.pack();
        dialog.setVisible(true);


    }

}
