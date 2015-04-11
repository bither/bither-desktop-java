/**
 * Copyright 2012 multibit.org
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

import net.bither.bitherj.core.Tx;
import net.bither.utils.ImageLoader;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.dialogs.TxDetailsDialog;
import net.bither.viewsystem.froms.ShowTransactionsForm;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * This {@link Action} shows the transaction details dialog
 */
public class ShowTransactionDetailsAction extends AbstractAction {

    private static final long serialVersionUID = 1913592498732457765L;

    private ShowTransactionsForm showTransactionsPanel;

    /**
     * Creates a new {@link ShowTransactionDetailsAction}.
     */
    public ShowTransactionDetailsAction(ShowTransactionsForm showTransactionsPanel) {
        super(LocaliserUtils.getString("showTransactionsDetailAction.text"), ImageLoader.createImageIcon(ImageLoader.TRANSACTIONS_ICON_FILE));

        this.showTransactionsPanel = showTransactionsPanel;


        MnemonicUtil mnemonicUtil = new MnemonicUtil();
        putValue(SHORT_DESCRIPTION, LocaliserUtils.getString("showTransactionsDetailAction.tooltip"));
        putValue(MNEMONIC_KEY, mnemonicUtil.getMnemonic("showTransactionsDetailAction.mnemonicKey"));
    }

    /**
     * show the show transaction details dialog
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Tx rowTableData = showTransactionsPanel.getSelectedRowData();

        final TxDetailsDialog transactionDetailsDialog = new TxDetailsDialog(rowTableData);
        //final TransactionDetailsDialog transactionDetailsDialog = new TransactionDetailsDialog(rowTableData);
        transactionDetailsDialog.setVisible(true);

        // Put the focus back on the table so that the up and down arrows work.
        showTransactionsPanel.getTable().requestFocusInWindow();
    }
}