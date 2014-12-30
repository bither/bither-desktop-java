package net.bither.viewsystem.froms;

import net.bither.Bither;
import net.bither.bitherj.utils.UnitUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.fonts.MonospacedFont;
import net.bither.languages.MessageKey;
import net.bither.model.Ticker;
import net.bither.qrcode.QRCodeGenerator;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.MarketUtil;
import net.bither.utils.WalletUtils;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.themes.Themes;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

public class ShowTxHeaderPanel {
    private JPanel panel;
    private JTextArea taAddress;
    private JButton btnAmt;

    private JButton btnSend;
    private JButton btnQRCode;
    private JButton btnCopy;
    private boolean isShowBtc = true;

    public ShowTxHeaderPanel() {
        initUi();

    }

    private void initUi() {
        panel = Panels.newPanel(new MigLayout(
                Panels.migXYLayout(),
                "10[][][][][][][]10", // Column constraints
                "10[]10" // Row constraints
        ));
        panel.setOpaque(true);
        final JLabel label1 = new JLabel();
        label1.setText(LocaliserUtils.getString("address.balance"));
        taAddress = new JTextArea();
        taAddress.setEditable(false);
        taAddress.setBorder(null);
        taAddress.setFont(MonospacedFont.fontWithSize(taAddress.getFont().getSize()));
        panel.add(taAddress);
        btnCopy = Buttons.newQRCodeButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        btnQRCode = Buttons.newQRCodeButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        btnSend = Buttons.newQRCodeButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        btnAmt = Buttons.newButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        btnAmt.setText("0.00");
        panel.add(btnCopy,"shrink");
        panel.add(btnQRCode,"shrink");
        panel.add(btnSend,"shrink");
        panel.add(label1);
        panel.add(btnAmt);
        updateUI();

    }

    public JPanel getPanel() {
        return panel;
    }

    private void showAmt() {
        if (Bither.getActionAddress() == null) {
            return;
        }
        if (isShowBtc) {
            btnAmt.setText(UnitUtil.formatValue(Bither.getActionAddress().getBalance(), UnitUtil.BitcoinUnit.BTC));

        } else {
            Ticker ticker = MarketUtil.getTickerOfDefaultMarket();
            double amt = ((double) Bither.getActionAddress().getBalance()) * ticker.getDefaultExchangePrice() / Math.pow(10, 8);
            btnAmt.setText(Utils.formatDoubleToMoneyString(amt));
        }

    }
    public void updateUI() {
        String address = "";
        showAmt();

        if (Bither.getActionAddress() != null) {
            address = Bither.getActionAddress().getAddress();
        }
        taAddress.setText(WalletUtils.formatHash(address, 4, 12));

        if (Bither.getActionAddress() != null && Bither.getActionAddress().getBalance() == 0) {
            btnSend.setEnabled(false);
        } else {
            btnSend.setEnabled(true);
        }


    }


}
