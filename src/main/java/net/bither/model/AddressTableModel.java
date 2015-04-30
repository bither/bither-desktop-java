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
