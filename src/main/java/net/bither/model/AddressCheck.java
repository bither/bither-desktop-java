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
