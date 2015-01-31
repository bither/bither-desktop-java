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
                return addressCheck.getAddress().getAddress();
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
