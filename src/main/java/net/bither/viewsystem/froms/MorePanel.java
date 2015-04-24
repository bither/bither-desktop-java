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
import java.util.ArrayList;
import java.util.List;

public class MorePanel extends WizardPanel {

    private JButton btnAdvance;
    private JButton btnVanitygen;
    private JButton btnPeer;
    private JButton btnBlcok;
    private JButton btnExchange;
    private JButton btnVerfyMessage;
    private JButton btnSignMessage;
    private JButton btnDonate;
    private JButton btnChangePassword;


    public MorePanel() {
        super(MessageKey.MORE, AwesomeIcon.ELLIPSIS_H);
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
        btnVanitygen = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                VanitygenPanel vanitygenPanel = new VanitygenPanel();
                vanitygenPanel.showPanel();


            }
        }, MessageKey.vanity_address, AwesomeIcon.VIMEO_SQUARE);
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

        btnChangePassword = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ChangePasswordPanel wizardForm = new ChangePasswordPanel();
                //  wizardForm.setOkAction(changePasswordForm.getOKAction());
                wizardForm.showPanel();
            }
        }, MessageKey.SHOW_CHANGE_PASSWORD_WIZARD, AwesomeIcon.LOCK);


        if (UserPreference.getInstance().getAppMode() == BitherjSettings.AppMode.HOT) {
            panel.add(btnChangePassword, "align center,cell 3 0 ,grow ,shrink,wrap");
            panel.add(btnVanitygen, "align center,cell 3 1 ,grow ,shrink,wrap");
            panel.add(btnAdvance, "align center,cell 3 2 ,shrink,grow,wrap");
            panel.add(btnExchange, "align center,cell 3 3,shrink,grow,wrap");
            panel.add(btnSignMessage, "align center,cell 3 4,shrink,grow,wrap");
            panel.add(btnVerfyMessage, "align center,cell 3 5,shrink,grow,wrap");
            panel.add(btnPeer, "align center,cell 3 6,shrink,grow,wrap");
            panel.add(btnBlcok, "align center,cell 3 7,shrink,grow,wrap");
            btnDonate = Buttons.newNormalButton(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    List<Address> availableList = new ArrayList<Address>();
                    for (Address address : AddressManager.getInstance().getAllAddresses()) {
                        if (address.getBalance() > 0) {
                            availableList.add(address);
                        }
                    }
                    if (availableList.size() == 0) {
                        new MessageDialog(LocaliserUtils.getString("donate_no_address")).showMsg();
                        return;
                    }
                    final String defaultAddress;
                    if (Bither.getActionAddress() != null) {
                        defaultAddress = Bither.getActionAddress().getAddress();
                    } else {
                        defaultAddress = "";
                    }
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
                    }, availableList, defaultAddress);
                    selectAddressPanel.updateTitle(LocaliserUtils.getString("select_address_to_donate"));
                    selectAddressPanel.showPanel();

                }
            }, MessageKey.donate_button, AwesomeIcon.BITCOIN);

            panel.add(btnDonate, "align center,cell 3 8,grow,shrink,wrap");
        } else {
            panel.add(btnChangePassword, "align center,cell 3 0 ,shrink");
            panel.add(btnVanitygen, "align center,cell 3 1 ,shrink");
        }


    }
}
