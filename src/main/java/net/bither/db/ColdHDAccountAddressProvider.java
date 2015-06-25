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
import net.bither.bitherj.db.IColdHDAccountAddressProvider;

import java.util.ArrayList;
import java.util.List;

public class ColdHDAccountAddressProvider implements IColdHDAccountAddressProvider {


    private static ColdHDAccountAddressProvider coldHDAccountAddressProvider =
            new ColdHDAccountAddressProvider(ApplicationInstanceManager.addressDBHelper);

    public static ColdHDAccountAddressProvider getInstance() {
        return coldHDAccountAddressProvider;
    }

    private AddressDBHelper mDb;


    private ColdHDAccountAddressProvider(AddressDBHelper db) {
        this.mDb = db;
    }

    @Override
    public int addHDAccount(String encryptedMnemonicSeed, String encryptSeed, String firstAddress, boolean isXrandom, String addressOfPS, byte[] externalPub, byte[] internalPub) {
        return 0;
    }

    @Override
    public int addMonitoredHDAccount(boolean isXrandom, byte[] externalPub, byte[] internalPub) {
        return 0;
    }

    @Override
    public String getHDFristAddress(int hdSeedId) {
        return null;
    }

    @Override
    public byte[] getExternalPub(int hdSeedId) {
        return new byte[0];
    }

    @Override
    public byte[] getInternalPub(int hdSeedId) {
        return new byte[0];
    }

    @Override
    public String getHDAccountEncryptSeed(int hdSeedId) {
        return null;
    }

    @Override
    public String getHDAccountEncryptMnmonicSeed(int hdSeedId) {
        return null;
    }

    @Override
    public boolean hdAccountIsXRandom(int seedId) {
        return false;
    }

    @Override
    public List<Integer> getHDAccountSeeds() {
        return new ArrayList<Integer>();
    }

    @Override
    public boolean hasHDAccountCold() {
        return false;
    }
}
