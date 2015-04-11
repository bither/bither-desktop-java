package net.bither.model;

import net.bither.bitherj.core.AddressManager;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * Created by nn on 14/11/28.
 */
public class RCheckTableModel extends AbstractTableModel {
    private List<AddressCheck> addressCheckList;

    public RCheckTableModel(List<AddressCheck> addressCheckList) {
        this.addressCheckList = addressCheckList;

    }

    @Override
    public int getRowCount() {
        return AddressManager.getInstance().getPrivKeyAddresses().size();
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
}

