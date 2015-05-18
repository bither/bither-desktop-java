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

import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.HDAccount;
import net.bither.bitherj.utils.Utils;
import net.bither.utils.LocaliserUtils;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class AddressTableModel extends AbstractTableModel {

    private List<Address> addressList;
    private String selectAddress;

    public AddressTableModel(List<Address> addressList, String selectAddress) {
        this.addressList = addressList;
        this.selectAddress = selectAddress;
    }

    @Override
    public int getRowCount() {
        return this.addressList.size();
    }

    @Override
    public Object getValueAt(int i, int i2) {
        Address address = this.addressList.get(i);
        switch (i2) {
            case 0:
                if (address instanceof HDAccount) {
                    return LocaliserUtils.getString("add_hd_account_tab_hd");
                }
                return address.getAddress();
            case 1:

                return address.hasPrivKey();
            case 2:
                return Utils.compareString(address.getAddress(), selectAddress);

        }

        return "";
    }


    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int column) {
        return "";
    }
}
