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
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.utils.Utils;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.qrcode.DisplayQRCodePanle;
import net.bither.utils.WalletUtils;
import net.bither.viewsystem.action.CopyAction;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Panels;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AddressDetailPanel extends WizardPanel implements CopyAction.ICopy {

    private JButton btnAddressQRCode;
    private JTextArea taAddress;
    private JButton btnCopy;


    public AddressDetailPanel() {
        super(MessageKey.ADDRESS, AwesomeIcon.FA_SEARCH_PLUS);

    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[]", // Column constraints
                "[][][][]80[]20[]" // Row constraints
        ));

        CopyAction copyAddressAction =
                new CopyAction(AddressDetailPanel.this);
        btnCopy = Buttons.newCopyButton(copyAddressAction);

        taAddress = new JTextArea();
        taAddress.setEditable(false);
        taAddress.setFont(new Font("Monospaced", taAddress.getFont().getStyle(), 18));
        taAddress.setBorder(null);
        taAddress.setOpaque(false);
        btnAddressQRCode = Buttons.newQRCodeButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closePanel();
                DisplayQRCodePanle displayQRCodePanle = new DisplayQRCodePanle(Bither.getActionAddress().getAddress()
                );
                displayQRCodePanle.showPanel();


            }
        });
        if (AddressManager.getInstance().getAllAddresses().size() == 0) {
            btnCopy.setVisible(false);
            btnAddressQRCode.setVisible(false);
            taAddress.setVisible(false);
        }
        panel.add(taAddress, "push,align center,wrap");
        panel.add(btnCopy, "push,align center,wrap");
        panel.add(btnAddressQRCode, "push,align center,wrap");
        updateUI();
    }

    @Override
    public String getCopyString() {
        return Bither.getActionAddress().getAddress();
    }

    private void updateUI() {
        String address = "";
        if (Bither.getActionAddress() != null) {
            address = Bither.getActionAddress().getAddress();
        }
        if (Utils.isEmpty(address)) {
            btnAddressQRCode.setVisible(false);
        } else {

            taAddress.setText(WalletUtils.formatHash(address, 4, 12));
        }
    }
}
