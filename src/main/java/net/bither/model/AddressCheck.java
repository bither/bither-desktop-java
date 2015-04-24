package net.bither.model;

import net.bither.bitherj.core.Address;

public class AddressCheck {
    public enum CheckStatus {
        Prepare, Success, Failed

    }

    public enum CheckType {
        Address, HDMKeyChain, HDAccount
    }

    private Address address;
    private CheckStatus checkStatus;

    private String dispalyName;
    private CheckType checkType;

    public AddressCheck(Address address, CheckStatus checkStatus) {
        this.address = address;
        this.checkStatus = checkStatus;
        this.dispalyName = address.getAddress();
        this.checkType = CheckType.Address;

    }

    public AddressCheck(CheckType checkType, String dispalyName, CheckStatus checkStatus) {
        this.dispalyName = dispalyName;
        this.checkStatus = checkStatus;
        this.checkType = checkType;
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

    public CheckType getCheckType() {
        return this.checkType;
    }


}
