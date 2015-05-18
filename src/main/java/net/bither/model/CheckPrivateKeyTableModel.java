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

package net.bither.model;

import net.bither.utils.LocaliserUtils;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class CheckPrivateKeyTableModel extends AbstractTableModel {
    private List<AddressCheck> addressCheckList;

    public CheckPrivateKeyTableModel(List<AddressCheck> addressCheckList) {
        this.addressCheckList = addressCheckList;

    }

    @Override
    public int getRowCount() {
        return this.addressCheckList.size();
    }

    @Override
    public Object getValueAt(int i, int i2) {
        AddressCheck addressCheck = this.addressCheckList.get(i);
        switch (i2) {
            case 0:
                return addressCheck.getDispalyName();
            case 1:
                return addressCheck.getCheckStatus();
        }


        return "";
    }


    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return LocaliserUtils.getString("address");
        } else if (column == 1) {
            return LocaliserUtils.getString("tx_status_text");
        }
        return "";
    }
}
