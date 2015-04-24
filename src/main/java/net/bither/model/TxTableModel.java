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
package net.bither.model;

import net.bither.Bither;
import net.bither.bitherj.core.Tx;
import net.bither.utils.LocaliserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TxTableModel extends AbstractTableModel {


    private static final Logger log = LoggerFactory.getLogger(TxTableModel.class);
    private static final String[] COLUMN_HEADER_KEYS = new String[]{"tx_status_text",
            "tx_date_text",
            "tx_amount_label"};

    private ArrayList<String> headers;

    private List<Tx> txList;

    public TxTableModel(List<Tx> txList) {
        createHeaders();
        this.txList = txList;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 2) {
            return Number.class;
        } else {
            return super.getColumnClass(columnIndex);
        }
    }

    @Override
    public int getColumnCount() {
        return headers.size();
    }

    @Override
    public int getRowCount() {
        return txList.size();
    }

    public Tx getRow(int row) {
        return txList.get(row);
    }

    @Override
    public String getColumnName(int column) {
        return headers.get(column);
    }

    @Override
    public Object getValueAt(int row, int column) {
        Tx tx = null;
        if (row >= 0 && row < txList.size()) {
            tx = txList.get(row);
        }
        if (tx == null) {
            return null;
        }

        switch (column) {
            case 0:
                return tx;
            case 1: {
                if (tx.getTxDate() == null) {
                    return new Date(0); // the earliest date (for sorting)
                } else {
                    return tx.getTxDate();
                }
            }

            case 2:
                return tx.deltaAmountFrom(Bither.getActionAddress());

            default:
                return null;
        }
    }

    /**
     * Table model is read only.
     */
    @Override
    public void setValueAt(Object value, int row, int column) {
        throw new UnsupportedOperationException();
    }


    public void createHeaders() {
        headers = new ArrayList<String>();
        for (int j = 0; j < COLUMN_HEADER_KEYS.length; j++) {
            if ("sendBitcoinPanel.amountLabel".equals(COLUMN_HEADER_KEYS[j])) {
                String header = LocaliserUtils.getString(COLUMN_HEADER_KEYS[j]) + " (" + LocaliserUtils.getString("sendBitcoinPanel.amountUnitLabel") + ")";
                headers.add(header);
            } else {
                headers.add(LocaliserUtils.getString(COLUMN_HEADER_KEYS[j]));
            }
        }


    }
}
