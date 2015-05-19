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
import net.bither.BitherSetting;
import net.bither.BitherUI;
import net.bither.bitherj.api.SignatureHDMApi;
import net.bither.bitherj.api.http.Http400Exception;
import net.bither.bitherj.api.http.HttpSetting;
import net.bither.bitherj.core.*;
import net.bither.bitherj.crypto.ECKey;
import net.bither.bitherj.crypto.KeyCrypterException;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.crypto.TransactionSignature;
import net.bither.bitherj.exception.PasswordException;
import net.bither.bitherj.qrcode.QRCodeTxTransport;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.GenericUtils;
import net.bither.bitherj.utils.UnitUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.fonts.AwesomeDecorator;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.qrcode.GenerateUnsignedTxPanel;
import net.bither.qrcode.IReadQRCode;
import net.bither.qrcode.IScanQRCode;
import net.bither.qrcode.SelectQRCodePanel;
import net.bither.runnable.CommitTransactionThread;
import net.bither.runnable.CompleteTransactionRunnable;
import net.bither.runnable.RCheckRunnable;
import net.bither.runnable.RunnableListener;
import net.bither.utils.HDMResetServerPasswordUtil;
import net.bither.utils.InputParser;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.TextBoxes;
import net.bither.viewsystem.action.PasteAddressAction;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.DialogConfirmTask;
import net.bither.viewsystem.dialogs.DialogProgress;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.themes.Themes;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SendHDMBitcoinPanel extends WizardPanel implements SelectAddressPanel.SelectAddressListener {

    private JTextField tfAmt;
    private JPasswordField currentPassword;
    private JTextField tfAddress;


    private String bitcoinAddress;

    private JLabel spinner;
    private String changeAddress = "";
    private DialogProgress dp;
    private HDMResetServerPasswordUtil resetServerPasswordUtil;
    private boolean signWithCold = false;
    private boolean isInRecovery = false;
    private HDMAddress hdmAddress;

    private String doateAddress;

    public SendHDMBitcoinPanel() {
        this(null);
    }

    public SendHDMBitcoinPanel(String doateAddress) {
        super(MessageKey.SEND, AwesomeIcon.SEND);
        this.doateAddress = doateAddress;
        setOkAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        dp = new DialogProgress();
        resetServerPasswordUtil = new HDMResetServerPasswordUtil(dp);
        hdmAddress = (HDMAddress) Bither.getActionAddress();
        isInRecovery = hdmAddress.isInRecovery();

    }


    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[]", // Column constraints
                "[][][][]" // Row constraints
        ));

        JLabel label = Labels.newValueLabel(LocaliserUtils.getString("address_balance") + " : " + Utils.bitcoinValueToPlainString(Bither.getActionAddress().getBalance()));
        panel.add(label, "align center,wrap");
        panel.add(newEnterAddressPanel(), "push,wrap");
        panel.add(newAmountPanel(), "push,wrap");
        panel.add(getenterPasswordMaV(), "push,wrap");
        if (!hdmAddress.isInRecovery()) {
            JButton btnSwitchCold = Buttons.newNormalButton(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    signWithCold = true;
                    onOK();
                }
            }, MessageKey.hdm_send_with_cold, AwesomeIcon.QRCODE);
            panel.add(btnSwitchCold, "push ,align right,shrink");
        }
        if (!Utils.isEmpty(this.doateAddress)) {
            tfAddress.setText(this.doateAddress);
        }
        validateValues();


    }


    public JPanel newAmountPanel() {

        JPanel panel = Panels.newPanel(new MigLayout(
                Panels.migXLayout(),
                "[][][][]", // Columns
                "[]" // Rows
        ));

        //panel.add(Labels.newAmount(), "span 4,grow,push,wrap");
        JLabel label = Labels.newBitcoinSymbol();
        label.setText("");
        AwesomeDecorator.applyIcon(AwesomeIcon.FA_BTC, label, true, BitherUI.NORMAL_ICON_SIZE);
        tfAmt = TextBoxes.newAmount(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateUI();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateUI();

            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateUI();
            }

            private void updateUI() {
                validateValues();
            }
        });
        panel.add(label, "shrink");

        panel.add(tfAmt, "grow,push,wrap");


        return panel;

    }

    public JPanel newEnterAddressPanel() {

        JPanel panel = Panels.newPanel(
                new MigLayout(
                        Panels.migXLayout(),
                        "[][][][]", // Columns
                        "[]" // Rows
                ));


        tfAddress = TextBoxes.newEnterAddress(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateUI();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateUI();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateUI();
            }

            private void updateUI() {
                validateValues();
            }
        });


        panel.add(Labels.newBitcoinAddress());

        panel.add(tfAddress, "growx," + BitherUI.COMBO_BOX_WIDTH_MIG + ",push");
        panel.add(Buttons.newPasteButton(new PasteAddressAction(tfAddress)), "shrink");

        panel.add(getQRCodeButton(), "shrink");
        panel.add(Buttons.newOptionsButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String defaultAddress = changeAddress;
                if (Utils.isEmpty(defaultAddress)) {
                    defaultAddress = Bither.getActionAddress().getAddress();
                }
                SelectAddressPanel selectAddressPanel = new SelectAddressPanel(SendHDMBitcoinPanel.this,
                        AddressManager.getInstance().getAllAddresses(), defaultAddress);
                selectAddressPanel.updateTitle(LocaliserUtils.getString("select_change_address_option_name"));
                selectAddressPanel.showPanel();


            }
        }), "shrink");


        return panel;

    }

    private void onOK() {
        spinner.setVisible(true);
        bitcoinAddress = tfAddress.getText().trim();
        String amtString = tfAmt.getText().trim();
        long btc = GenericUtils.toNanoCoins(amtString, 0).longValue();

        if (btc > 0) {
            if (Utils.validBicoinAddress(bitcoinAddress)) {
                if (Utils.compareString(bitcoinAddress,
                        changeAddress)) {
                    new MessageDialog(LocaliserUtils.getString("select_change_address_change_to_same_warn")).showMsg();
                    return;
                }
                try {
                    CompleteTransactionRunnable completeRunnable;
                    if (!isInRecovery) {
                        completeRunnable = new CompleteTransactionRunnable(hdmAddress,
                                btc,
                                bitcoinAddress,
                                changeAddress,
                                new SecureCharSequence(currentPassword.getPassword()),
                                signWithCold ? coldSignatureFetcher : remoteSignatureFetcher);
                    } else {
                        completeRunnable = new CompleteTransactionRunnable(hdmAddress,
                                btc,
                                bitcoinAddress,
                                changeAddress,
                                new SecureCharSequence(currentPassword.getPassword()),
                                coldSignatureFetcher, remoteSignatureFetcher);
                    }
                    completeRunnable.setRunnableListener(completeTransactionListener);
                    Thread thread = new Thread(completeRunnable);
                    thread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    new MessageDialog(LocaliserUtils.getString("send_failed")).showMsg();
                }
            } else {
                new MessageDialog(LocaliserUtils.getString("send_failed")).showMsg();
            }
        }
    }

    RunnableListener completeTransactionListener = new RunnableListener() {
        @Override
        public void prepare() {
        }

        @Override
        public void success(Object obj) {


            Tx tx = (Tx) obj;
            RCheckRunnable run = new RCheckRunnable(Bither.getActionAddress(), tx);
            run.setRunnableListener(rcheckHandler);
            new Thread(run).start();

        }

        @Override
        public void error(int errorCode, String errorMsg) {

            spinner.setVisible(false);
            if (!Utils.isEmpty(errorMsg)) {
                new MessageDialog(errorMsg).showMsg();
            }
        }
    };

    SendBitcoinConfirmPanel.SendConfirmListener sendConfirmListener = new SendBitcoinConfirmPanel.SendConfirmListener() {
        @Override
        public void onConfirm(Tx request) {

            onCancel();
            sendTx(request);

        }

        @Override
        public void onCancel() {

        }
    };

    private RunnableListener rcheckHandler = new RunnableListener() {
        @Override
        public void prepare() {
        }

        @Override
        public void success(Object obj) {
            spinner.setVisible(false);
            final Tx tx = (Tx) obj;
            SendBitcoinConfirmPanel sendBitcoinConfirmPanel = new SendBitcoinConfirmPanel(sendConfirmListener, bitcoinAddress, changeAddress, tx);
            sendBitcoinConfirmPanel.showPanel();
        }

        @Override
        public void error(int errorCode, String errorMsg) {
            spinner.setVisible(false);
        }


    };


    private void sendTx(Tx tx) {

        try {
            CommitTransactionThread commitTransactionThread =
                    new CommitTransactionThread(Bither.getActionAddress(), tx
                            , true, commitTransactionListener);
            commitTransactionThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    CommitTransactionThread.CommitTransactionListener commitTransactionListener = new CommitTransactionThread.CommitTransactionListener() {
        @Override
        public void onCommitTransactionSuccess(Tx tx) {
            closePanel();
            if (Utils.isEmpty(doateAddress)) {
                new MessageDialog(LocaliserUtils.getString("send_success")).showMsg();
            } else {
                new MessageDialog(LocaliserUtils.getString("donate_thanks")).showMsg();
            }
        }

        @Override
        public void onCommitTransactionFailed() {
            new MessageDialog(LocaliserUtils.getString("send_failed")).showMsg();

        }
    };

    private JButton getQRCodeButton() {
        JButton button = Buttons.newFromCameraIconButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SelectQRCodePanel qrCodePanel = new SelectQRCodePanel(new IScanQRCode() {

                    public void handleResult(final String result, final IReadQRCode readQRCode) {


                        new InputParser.StringInputParser(result) {
                            @Override
                            protected void bitcoinRequest(final String address, final String addressLabel,
                                                          final long amount, final String bluetoothMac) {
                                readQRCode.close();
                                tfAddress.setText(result);

                                if (amount > 0) {
                                    tfAmt.setText(UnitUtil.formatValue(amount, UnitUtil.BitcoinUnit.BTC));

                                    currentPassword.requestFocus();
                                } else {
                                    tfAmt.requestFocus();
                                }
                                validateValues();
                            }

                            @Override
                            protected void error(final String messageResId, final Object... messageArgs) {
                                readQRCode.reTry(LocaliserUtils.getString("scan_watch_only_address_error"));

                            }
                        }.parse();


                    }
                });
                qrCodePanel.showPanel();

            }
        });
        return button;
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
                        validateValues();
                        currentPassword.setBackground(Themes.currentTheme.dataEntryBackground());


                    }

                });
        panel.add(Labels.newEnterPassword(), "push,shrink");

        panel.add(currentPassword, "growx,h 32,push");
        //panel.add(showButton, "shrink");

        // Ensure the icon label is a size suitable for rotation
        panel.add(spinner, BitherUI.NORMAL_PLUS_ICON_SIZE_MIG + ",wrap");

        return panel;

    }


    private void validateValues() {
        boolean isValidAmounts = false;

        String amtString = tfAmt.getText().trim();

        if (Utils.isNubmer(amtString)) {
            long btc = GenericUtils.toNanoCoins(amtString, 0).longValue();
            if (btc > 0) {
                isValidAmounts = true;
            } else {
            }
        }
        boolean isValidAddress = Utils.validBicoinAddress(tfAddress.getText().trim());
        SecureCharSequence password = new SecureCharSequence(currentPassword.getPassword());
        boolean isValidPassword = Utils.validPassword(password) && password.length() >= BitherSetting.PASSWORD_LENGTH_MIN &&
                password.length() <= BitherSetting.PASSWORD_LENGTH_MAX;
        password.wipe();
        setOkEnabled(isValidAddress && isValidAmounts && isValidPassword);
    }

    @Override
    public void selectAddress(Address address) {
        changeAddress = address.getAddress();
    }


    private class ColdSignatureFetcher implements HDMAddress.HDMFetchOtherSignatureDelegate {
        private static final int RequestCode = 1251;

        private ReentrantLock lock = new ReentrantLock();
        private Condition fetchedCondition = lock.newCondition();
        private List<TransactionSignature> sigs;
        private int signingIndex = -1;

        @Override
        public List<TransactionSignature> getOtherSignature(int addressIndex,
                                                            CharSequence password,
                                                            List<byte[]> unsignHash, final Tx tx) {
            signingIndex = addressIndex;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    SendBitcoinConfirmPanel dialog = new SendBitcoinConfirmPanel(preConfirmListener,
                            bitcoinAddress, changeAddress, tx
                    );
                    dialog.showPanel();
                }
            });
            sigs = null;
            try {
                lock.lockInterruptibly();
                fetchedCondition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
            if (sigs == null) {
                throw new CompleteTransactionRunnable.HDMSignUserCancelExcetion();
            }
            signingIndex = -1;
            return sigs;
        }

        private SendBitcoinConfirmPanel.SendConfirmListener preConfirmListener = new SendBitcoinConfirmPanel.SendConfirmListener() {

            @Override
            public void onConfirm(Tx tx) {
                String addressCannotBtParsed = LocaliserUtils.getString("address_cannot_be_parsed");

                String qrCode = QRCodeTxTransport.getPresignTxString(tx, changeAddress,
                        addressCannotBtParsed, signingIndex);

                GenerateUnsignedTxPanel generateUnsignedTxPanel = new GenerateUnsignedTxPanel(new IScanQRCode() {
                    @Override
                    public void handleResult(String result, IReadQRCode readQRCode) {
                        readQRCode.close();
                        setQrCodeResult(result);

                    }
                }, qrCode);
                generateUnsignedTxPanel.showPanel();
                sigs = null;
            }

            @Override
            public void onCancel() {
                sigs = null;
                try {
                    lock.lock();
                    fetchedCondition.signal();
                } finally {
                    lock.unlock();
                }
            }
        };

        public boolean setQrCodeResult(String qr) {

            if (!Utils.isEmpty(qr)) {
                try {
                    String[] stringArray = QRCodeUtil.splitString(qr);
                    sigs = new ArrayList<TransactionSignature>();
                    for (String str : stringArray) {
                        if (!Utils.isEmpty(str)) {
                            TransactionSignature transactionSignature = new
                                    TransactionSignature(ECKey.ECDSASignature.decodeFromDER
                                    (Utils.hexStringToByteArray(str)),
                                    TransactionSignature.SigHash.ALL, false);
                            sigs.add(transactionSignature);
                        }
                    }
                } catch (Exception e) {
                    sigs = null;
                    e.printStackTrace();
                    new MessageDialog(LocaliserUtils.getString("send_failed")).showMsg();
                }
            } else {
                sigs = null;
            }
            try {
                lock.lock();
                fetchedCondition.signal();
            } finally {
                lock.unlock();
            }
            return true;

        }
    }

    private ColdSignatureFetcher coldSignatureFetcher = new ColdSignatureFetcher();

    private HDMAddress.HDMFetchOtherSignatureDelegate remoteSignatureFetcher = new HDMAddress
            .HDMFetchOtherSignatureDelegate() {
        private boolean toChangePassword = false;

        @Override
        public List<TransactionSignature> getOtherSignature(int addressIndex,
                                                            CharSequence password,
                                                            List<byte[]> unsignHash, Tx tx) {
            List<TransactionSignature> transactionSignatureList = new
                    ArrayList<TransactionSignature>();
            try {
                HDMBId hdmbId = HDMBId.getHDMBidFromDb();
                byte[] decryptedPassword = hdmbId.decryptHDMBIdPassword(password);
                SignatureHDMApi signatureHDMApi = new SignatureHDMApi(HDMBId.getHDMBidFromDb()
                        .getAddress(), addressIndex, decryptedPassword, unsignHash);
                signatureHDMApi.handleHttpPost();
                List<byte[]> bytesList = signatureHDMApi.getResult();
                for (byte[] bytes : bytesList) {
                    TransactionSignature transactionSignature = new TransactionSignature(ECKey
                            .ECDSASignature.decodeFromDER(bytes), TransactionSignature.SigHash
                            .ALL, false);
                    transactionSignatureList.add(transactionSignature);
                }
            } catch (Exception e) {
                if (e instanceof Http400Exception) {
                    if (((Http400Exception) e).getErrorCode() == HttpSetting.PasswordWrong) {
                        toChangePassword = false;
                        final ReentrantLock lock = new ReentrantLock();
                        final Condition changePasswordCondition = lock.newCondition();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                dp.dispose();
                                DialogConfirmTask dialogConfirmTask = new DialogConfirmTask(LocaliserUtils.getString("hdm_reset_server_password_password_wrong_confirm"),
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                toChangePassword = true;
                                                try {
                                                    lock.lock();
                                                    changePasswordCondition.signal();
                                                } finally {
                                                    lock.unlock();
                                                }
                                            }
                                        }, new Runnable() {
                                    @Override
                                    public void run() {
                                        toChangePassword = false;
                                        try {
                                            lock.lock();
                                            changePasswordCondition.signal();
                                        } finally {
                                            lock.unlock();
                                        }
                                    }
                                });
                                dialogConfirmTask.pack();
                                dialogConfirmTask.setVisible(true);
                            }
                        });
                        try {
                            lock.lock();
                            changePasswordCondition.awaitUninterruptibly();
                        } finally {
                            lock.unlock();
                        }
                        if (!toChangePassword) {
                            throw new CompleteTransactionRunnable.HDMSignUserCancelExcetion();
                        }
                        resetServerPasswordUtil.setPassword(password);
                        if (!resetServerPasswordUtil.changePassword()) {
                            throw new CompleteTransactionRunnable.HDMSignUserCancelExcetion();
                        }
                        return getOtherSignature(addressIndex, password, unsignHash, tx);
                    } else {
                        throw new CompleteTransactionRunnable.HDMServerSignException(LocaliserUtils.getString(
                                "hdm_address_sign_tx_server_error"));
                    }
                } else if (e instanceof KeyCrypterException) {
                    throw new PasswordException("hdm password decrypting error");
                } else {
                    throw new RuntimeException(e);
                }
            }

            return transactionSignatureList;
        }
    };

}
