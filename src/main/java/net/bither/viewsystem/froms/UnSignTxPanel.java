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
import net.bither.BitherUI;
import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.qrcode.QRCodeTxTransport;
import net.bither.bitherj.utils.GenericUtils;
import net.bither.bitherj.utils.TransactionsUtil;
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
import net.bither.runnable.RunnableListener;
import net.bither.utils.InputParser;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.TextBoxes;
import net.bither.viewsystem.action.PasteAddressAction;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;

public class UnSignTxPanel extends WizardPanel implements IScanQRCode, SelectAddressPanel.SelectAddressListener {

    private JTextField tfAddress;
    private JTextField tfAmt;
    private String bitcoinAddress;
    private Tx tx;
    private boolean needConfirm = true;
    private String changeAddress = "";
    private String doateAddress;

    public UnSignTxPanel() {
        this(null);
    }

    public UnSignTxPanel(String doateAddress) {
        super(MessageKey.UNSIGNED, AwesomeIcon.FA_BANK);
        this.doateAddress = doateAddress;
        setOkAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
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
                SelectAddressPanel selectAddressPanel = new SelectAddressPanel(UnSignTxPanel.this,
                        AddressManager.getInstance().getAllAddresses(), defaultAddress);
                selectAddressPanel.updateTitle(LocaliserUtils.getString("select_change_address_option_name"));
                selectAddressPanel.showPanel();


            }
        }), "shrink");


        return panel;

    }

    private JButton getQRCodeButton() {
        JButton button = Buttons.newFromCameraIconButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onQRcode();
            }
        });
        return button;
    }

    private void onQRcode() {

        SelectQRCodePanel qrCodePanel = new SelectQRCodePanel(new IScanQRCode() {

            public void handleResult(String result, final IReadQRCode readQRCode) {

                new InputParser.StringInputParser(result) {
                    @Override
                    protected void bitcoinRequest(final String address, final String addressLabel,
                                                  final long amount, final String bluetoothMac) {
                        tfAddress.setText(address);
                        if (amount > 0) {
                            tfAmt.setText(UnitUtil.formatValue(amount, UnitUtil.BitcoinUnit.BTC));

                        }
                        tfAmt.requestFocus();
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

    private void validateValues() {
        boolean isValidAmounts = false;
        String amtString = tfAmt.getText().trim();
        long btc = 0;
        if (!Utils.isEmpty(amtString)) {
            btc = GenericUtils.toNanoCoins(amtString, 0).longValue();
        }
        if (btc > 0) {
            isValidAmounts = true;
        } else {
        }
        boolean isValidAddress = Utils.validBicoinAddress(tfAddress.getText().trim());
        setOkEnabled(isValidAddress && isValidAmounts);
    }

    private void onOK() {
        bitcoinAddress = tfAddress.getText().trim();
        if (Utils.validBicoinAddress(bitcoinAddress)) {
            if (Utils.compareString(bitcoinAddress, changeAddress)) {
                new MessageDialog(LocaliserUtils.getString("select_change_address_change_to_same_warn")).showMsg();
                return;
            }
            String amtString = tfAmt.getText().trim();
            long btc = GenericUtils.toNanoCoins(amtString, 0).longValue();
            try {
                CompleteTransactionRunnable completeTransactionRunnable = new CompleteTransactionRunnable(
                        Bither.getActionAddress(), btc, bitcoinAddress, changeAddress, null);
                completeTransactionRunnable.setRunnableListener(completeTransactionListener);
                new Thread(completeTransactionRunnable).start();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    SendBitcoinConfirmPanel.SendConfirmListener sendConfirmListener = new SendBitcoinConfirmPanel.SendConfirmListener() {
        @Override
        public void onConfirm(Tx request) {

            String qrCodeString = QRCodeTxTransport.getPresignTxString(request, changeAddress, LocaliserUtils.getString("address_cannot_be_parsed"), QRCodeTxTransport.NO_HDM_INDEX);
            GenerateUnsignedTxPanel generateUnsignedTxPanel = new GenerateUnsignedTxPanel(UnSignTxPanel.this, qrCodeString);
            generateUnsignedTxPanel.showPanel();

        }

        @Override
        public void onCancel() {

        }
    };

    RunnableListener completeTransactionListener = new RunnableListener() {
        @Override
        public void prepare() {
        }

        @Override
        public void success(Object obj) {
            if (obj != null && obj instanceof Tx) {
                tx = (Tx) obj;
                if (needConfirm) {
                    SendBitcoinConfirmPanel confirmPanel = new SendBitcoinConfirmPanel
                            (sendConfirmListener, bitcoinAddress, changeAddress, tx);
                    confirmPanel.showPanel();
                } else {
                    sendConfirmListener.onConfirm(tx);
                }
            } else {
                new MessageDialog(LocaliserUtils.getString("password_wrong")).showMsg();
            }

        }

        @Override
        public void error(int errorCode, String errorMsg) {
            new MessageDialog(errorMsg).showMsg();

        }
    };

    @Override
    public void handleResult(String result, IReadQRCode readQRCode) {
        boolean success;
        try {
            success = TransactionsUtil.signTransaction(tx, result);
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }
        if (success) {
            readQRCode.close();
            sendTx(tx);
        } else {
            readQRCode.reTry(" qr code error");
        }

    }

    private void sendTx(Tx tx) {

        try {
            CommitTransactionThread commitTransactionThread =
                    new CommitTransactionThread(Bither.getActionAddress(), tx
                            , false, commitTransactionListener);
            commitTransactionThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    CommitTransactionThread.CommitTransactionListener commitTransactionListener = new CommitTransactionThread.CommitTransactionListener() {
        @Override
        public void onCommitTransactionSuccess(Tx tx) {
            Panels.hideLightBoxIfPresent();
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

    @Override
    public void selectAddress(Address address) {
        changeAddress = address.getAddress();
    }
}
