/**
 * Copyright 2011 multibit.org
 *
 * Licensed under the MIT license (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://opensource.org/licenses/mit-license.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.bither.viewsystem.action;

import net.bither.Bither;
import net.bither.bitherj.core.Address;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.WhitespaceTrimmer;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * This {@link Action} represents the swing paste address action
 */
public class PasteAddressAction extends AbstractAction {

    private JTextField jTextField;

    /**
     * Creates a new {@link PasteAddressAction}.
     */
    public PasteAddressAction(JTextField jTextField) {

        this.jTextField = jTextField;


        MnemonicUtil mnemonicUtil = new MnemonicUtil();
        putValue(SHORT_DESCRIPTION, LocaliserUtils.getString("pasteAddressAction.tooltip"));
        putValue(MNEMONIC_KEY, mnemonicUtil.getMnemonic("pasteAddressAction.mnemonicKey"));
    }

    /**
     * delegate to generic paste address action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // check to see if the wallet files have changed
        Address perWalletModelData = Bither.getActionAddress();


        TextTransfer textTransfer = new TextTransfer();
        String stringToPaste = textTransfer.getClipboardContents();
        stringToPaste = WhitespaceTrimmer.trim(stringToPaste);
        jTextField.setText(stringToPaste);
        //Bither.getBitcoinController().displayView(Bither.getBitcoinController().getCurrentView());

    }
}