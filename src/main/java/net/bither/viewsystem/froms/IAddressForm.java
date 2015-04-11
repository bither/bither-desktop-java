package net.bither.viewsystem.froms;

import net.bither.bitherj.core.Address;

import javax.swing.*;

/**
 * Created by nn on 14/11/26.
 */
public interface IAddressForm {
    public JPanel getPanel();

    public void updateFromModel();

    public Address getPerWalletModelData();

    public String getOnlyName();

    public void setSelected(boolean selected);


}
