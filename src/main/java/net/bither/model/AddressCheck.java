package net.bither.model;

import net.bither.bitherj.core.Address;

public class AddressCheck {
    public enum CheckStatus {
        Prepare, Success, Failed;

    }

    private Address address;
    private CheckStatus checkStatus;

    private String dispalyName;

    public AddressCheck(Address address, CheckStatus checkStatus) {
        this.address = address;
        this.checkStatus = checkStatus;
        this.dispalyName = address.getAddress();
    }

    public AddressCheck(String dispalyName, CheckStatus checkStatus) {
        this.dispalyName = dispalyName;
        this.checkStatus = checkStatus;
    }

    public CheckStatus getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(CheckStatus checkStatus) {
        this.checkStatus = checkStatus;
    }


    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getDispalyName() {
        return dispalyName;
    }

    public boolean hasAddress() {
        return this.address != null;
    }


}
