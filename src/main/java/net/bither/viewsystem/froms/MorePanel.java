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
import net.bither.bitherj.core.HDAccount;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.preference.UserPreference;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.base.RadioButtons;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.froms.desktop.hdm.DesktopHDMColdPanel;
import net.bither.viewsystem.froms.desktop.hdm.DesktopHDMHotPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class MorePanel extends WizardPanel {

    private static final int MouseClickedCount = 7;
    private static final long MouseClickedTime = 5 * 1000;

    private JButton btnAdvance;
    private JButton btnVanitygen;
    private JButton btnPeer;
    private JButton btnBlcok;

    private JButton btnVerfyMessage;
    private JButton btnSignMessage;
    private JButton btnDonate;
    private JButton btnChangePassword;

    private JButton btnEnterpriseHDM;

    private long beginClickTime = System.currentTimeMillis();
    private int clickCount = 0;


    public MorePanel() {
        super(MessageKey.MORE, AwesomeIcon.ELLIPSIS_H);

    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][][][][]", // Column constraints
                "[][][][][]" // Row constraints
        ));
        panel.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (System.currentTimeMillis() - beginClickTime < MouseClickedTime) {
                    clickCount++;
                } else {
                    clickCount = 0;
                }
                beginClickTime = System.currentTimeMillis();
                if (clickCount == 7) {
                    btnEnterpriseHDM.setVisible(true);
                }
            }
        });
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
                closePanel();
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

        btnEnterpriseHDM = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closePanel();
                if (UserPreference.getInstance().getAppMode() == BitherjSettings.AppMode.HOT) {
                    DesktopHDMHotPanel desktopHDMHotPanel = new DesktopHDMHotPanel();
                    desktopHDMHotPanel.showPanel();
                } else {
                    DesktopHDMColdPanel enterpriseColdPanel = new DesktopHDMColdPanel();
                    enterpriseColdPanel.showPanel();
                }
            }
        }, MessageKey.desktop_enterprise_hdm, AwesomeIcon.HDD_O);


        if (UserPreference.getInstance().getAppMode() == BitherjSettings.AppMode.HOT) {
            panel.add(btnChangePassword, "align center,cell 3 0 ,grow ,shrink,wrap");
            panel.add(btnVanitygen, "align center,cell 3 1 ,grow ,shrink,wrap");
            panel.add(btnAdvance, "align center,cell 3 2 ,shrink,grow,wrap");
//            panel.add(btnExchange, "align center,cell 3 3,shrink,grow,wrap");
            panel.add(btnSignMessage, "align center,cell 3 3,shrink,grow,wrap");
            panel.add(btnVerfyMessage, "align center,cell 3 4,shrink,grow,wrap");
            panel.add(btnPeer, "align center,cell 3 5,shrink,grow,wrap");
            panel.add(btnBlcok, " align center,cell 3 6,shrink,grow,wrap");
            btnDonate = Buttons.newNormalButton(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    List<Address> availableList = new ArrayList<Address>();
                    for (Address address : AddressManager.getInstance().getAllAddresses()) {
                        if (address.getBalance() > 0) {
                            availableList.add(address);
                        }
                    }
                    if (AddressManager.getInstance().getHdAccount() != null
                            && AddressManager.getInstance().getHdAccount().getBalance() > 0) {
                        availableList.add(AddressManager.getInstance().getHdAccount());

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
                                if (address instanceof HDAccount) {
                                    HDAccountSendPanel hdAccountSendPanel = new HDAccountSendPanel(BitherjSettings.DONATE_ADDRESS);
                                    hdAccountSendPanel.showPanel();
                                } else if (address.isHDM()) {
                                    SendHDMBitcoinPanel sendHDMBitcoinPanel = new SendHDMBitcoinPanel(BitherjSettings.DONATE_ADDRESS);
                                    sendHDMBitcoinPanel.showPanel();
                                } else {
                                    if (address.hasPrivKey()) {
                                        SendBitcoinPanel sendBitcoinPanel = new SendBitcoinPanel(BitherjSettings.DONATE_ADDRESS);
                                        sendBitcoinPanel.showPanel();
                                    } else {
                                        UnSignTxPanel unSignTxPanel = new UnSignTxPanel(BitherjSettings.DONATE_ADDRESS);
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

            panel.add(btnDonate, "align center,cell 3 7,grow,shrink,wrap");
            panel.add(btnEnterpriseHDM, "align center,cell 3 8,grow,shrink,wrap");
        } else {
            panel.add(btnChangePassword, "align center,cell 3 0 ,shrink");
            panel.add(btnVanitygen, "align center,cell 3 1 ,shrink");
            JCheckBox cbCheckPassword = RadioButtons.newCheckPassword();
            panel.add(cbCheckPassword, "align center,cell 3 2 ,shrink");
            panel.add(btnEnterpriseHDM, "align center,cell 3 4,shrink,wrap");
        }
        btnEnterpriseHDM.setVisible(false);

    }

}
