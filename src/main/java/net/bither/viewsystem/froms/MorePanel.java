package net.bither.viewsystem.froms;

import net.bither.Bither;
import net.bither.BitherSetting;
import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.AddressManager;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.preference.UserPreference;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class MorePanel extends WizardPanel {

    private JButton btnAdvance;
    private JButton btnPeer;
    private JButton btnBlcok;
    private JButton btnExchange;
    private JButton btnVerfyMessage;
    private JButton btnSignMessage;
    private JButton btnDonate;

    public MorePanel() {
        super(MessageKey.MORE, AwesomeIcon.ELLIPSIS_H, false);
    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][][][][]", // Column constraints
                "[][][][][][]" // Row constraints
        ));
        btnAdvance = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                AdvancePanel advancePanel = new AdvancePanel();
                advancePanel.showPanel();

            }
        }, MessageKey.ADVANCE, AwesomeIcon.FA_BOOK);

        btnPeer = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                PeersPanel peersPanel = new PeersPanel();
                peersPanel.showPanel();
            }
        }, MessageKey.PEERS, AwesomeIcon.FA_USERS);
        btnBlcok = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BlockPanel blockPanel = new BlockPanel();
                blockPanel.showPanel();


            }
        }, MessageKey.BLOCKS, AwesomeIcon.FA_SHARE_ALT);
        btnExchange = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closePanel();
                ExchangePreferencePanel exchangePreferencePanel = new ExchangePreferencePanel();
                exchangePreferencePanel.showPanel();

            }
        }, MessageKey.EXCHANGE_SETTINGS_TITLE, AwesomeIcon.DOLLAR);
        btnVerfyMessage = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closePanel();
                VerifyMessagePanel verifyMessagePanel = new VerifyMessagePanel();
                verifyMessagePanel.showPanel();

            }
        }, MessageKey.VERIFY_MESSAGE_TITLE, AwesomeIcon.CHECK);
        btnSignMessage = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (AddressManager.getInstance().getPrivKeyAddresses().size() > 0) {
                    String defaultAddress = AddressManager.getInstance().getPrivKeyAddresses().get(0).getAddress();
                    SelectAddressPanel selectAddressPanel = new SelectAddressPanel(new SelectAddressPanel.SelectAddressListener() {
                        @Override
                        public void selectAddress(Address address) {
                            closePanel();
                            SignMessagePanel signMessagePanel = new SignMessagePanel(address);
                            signMessagePanel.showPanel();


                        }
                    }, AddressManager.getInstance().getPrivKeyAddresses(), defaultAddress);
                    selectAddressPanel.showPanel();
                } else {
                    new MessageDialog(LocaliserUtils.getString("private_key_is_empty")).showMsg();
                }
            }
        }, MessageKey.SIGN_MESSAGE_TITLE, AwesomeIcon.PENCIL);

        panel.add(btnAdvance, "align center,cell 3 0 ,grow,wrap");
        panel.add(btnExchange, "align center,cell 3 1,grow,wrap");
        panel.add(btnSignMessage, "align center,cell 3 2,grow,wrap");
        panel.add(btnVerfyMessage, "align center,cell 3 3,grow,wrap");
        if (UserPreference.getInstance().getAppMode() == BitherjSettings.AppMode.HOT) {
            panel.add(btnPeer, "align center,cell 3 4,grow,wrap");
            panel.add(btnBlcok, "align center,cell 3 5,grow,wrap");
            final String defaultAddress;
            if (Bither.getActionAddress() != null) {
                defaultAddress = Bither.getActionAddress().getAddress();
            } else {
                defaultAddress = "";
            }
            btnDonate = Buttons.newNormalButton(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SelectAddressPanel selectAddressPanel = new SelectAddressPanel(new SelectAddressPanel.SelectAddressListener() {
                        @Override
                        public void selectAddress(Address address) {
                            if (address.getBalance() == 0) {
                                new MessageDialog(LocaliserUtils.getString("donate_no_address")).showMsg();
                                return;
                            }
                            if (address != null) {
                                Bither.getMainFrame().getMainFrameUi().getWalletsView().selectWalletPanelByFilename(address.getAddress());
                                Bither.getCoreController().fireDataChangedUpdateNow();
                                closePanel();
                                if (address.isHDM()) {
                                    SendHDMBitcoinPanel sendHDMBitcoinPanel = new SendHDMBitcoinPanel(BitherSetting.DONATE_ADDRESS);
                                    sendHDMBitcoinPanel.showPanel();
                                } else {
                                    if (address.hasPrivKey()) {
                                        SendBitcoinPanel sendBitcoinPanel = new SendBitcoinPanel(BitherSetting.DONATE_ADDRESS);
                                        sendBitcoinPanel.showPanel();
                                    } else {
                                        UnSignTxPanel unSignTxPanel = new UnSignTxPanel(BitherSetting.DONATE_ADDRESS);
                                        unSignTxPanel.showPanel();

                                    }
                                }
                            }
                        }
                    }, AddressManager.getInstance().getAllAddresses(), defaultAddress);
                    selectAddressPanel.updateTitle(LocaliserUtils.getString("select_address_to_donate"));
                    selectAddressPanel.showPanel();

                }
            }, MessageKey.donate_button, AwesomeIcon.BITCOIN);

            panel.add(btnDonate, "align center,cell 3 6,grow,wrap");
        }


    }
}
