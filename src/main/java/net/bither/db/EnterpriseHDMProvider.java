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

package net.bither.db;

import net.bither.ApplicationInstanceManager;
import net.bither.bitherj.core.EnterpriseHDMAddress;
import net.bither.bitherj.core.EnterpriseHDMKeychain;
import net.bither.bitherj.db.IEnterpriseHDMProvider;

import java.util.List;

public class EnterpriseHDMProvider implements IEnterpriseHDMProvider {
    private static EnterpriseHDMProvider enterpriseHDMProvider =
            new EnterpriseHDMProvider(ApplicationInstanceManager.addressDBHelper);

    public static EnterpriseHDMProvider getInstance() {
        return enterpriseHDMProvider;
    }

    private EnterpriseHDMProvider(AddressDBHelper db) {
        this.mDb = db;
    }

    private AddressDBHelper mDb;

    @Override
    public String getEnterpriseEncryptMnemonicSeed(int hdSeedId) {
        return null;
    }

    @Override
    public String getEnterpriseEncryptHDSeed(int hdSeedId) {
        return null;
    }

    @Override
    public String getEnterpriseHDFristAddress(int hdSeedId) {
        return null;
    }

    @Override
    public boolean isEnterpriseHDMSeedFromXRandom(int hdSeedId) {
        return false;
    }

    @Override
    public void addEnterpriseHDMAddress(List<EnterpriseHDMAddress> enterpriseHDMAddressList) {

    }

    @Override
    public List<EnterpriseHDMAddress> getEnterpriseHDMAddress(EnterpriseHDMKeychain keychain) {
        return null;
    }

    @Override
    public void addMultiSignSet(int n, int m) {

    }

    @Override
    public void updateSyncComplete(EnterpriseHDMAddress enterpriseHDMAddress) {

    }

    @Override
    public List<Integer> getEnterpriseHDMKeychainIds() {
        return null;
    }

    @Override
    public int getEnterpriseHDMSeedId() {
        return 0;
    }
}
