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
import net.bither.bitherj.core.HDMAddress;
import net.bither.bitherj.crypto.PasswordSeed;
import net.bither.bitherj.utils.Base58;
import net.bither.bitherj.utils.Utils;
import net.bither.core.EnDesktopHDMAddress;
import net.bither.core.EnDesktopHDMKeychain;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by nn on 15/6/10.
 */
public class EnDesktopProvider {

    private static final String insertHDSeedSql = "insert into enterprise_hdm_account " +
            "(encrypt_mnemonic_seed,encrypt_seed,is_xrandom,hd_address,external_pub,internal_pub)" +
            " values (?,?,?,?,?,?) ";

    private static EnDesktopProvider addressProvider =
            new EnDesktopProvider(ApplicationInstanceManager.addressDBHelper);

    public static EnDesktopProvider getInstance() {
        return addressProvider;
    }

    private EnDesktopProvider(AddressDBHelper db) {
        this.mDb = db;
    }

    private AddressDBHelper mDb;

    public int addHDKey(String encryptedMnemonicSeed, String encryptHdSeed,
                        String firstAddress, boolean isXrandom, String addressOfPS
            , byte[] externalPub, byte[] internalPub) {
        int result = 0;
        try {
            this.mDb.getConn().setAutoCommit(false);
            String[] params = new String[]{encryptedMnemonicSeed, encryptHdSeed, Integer.toString(isXrandom ? 1 : 0), firstAddress,
                    Base58.encode(externalPub), Base58.encode(internalPub)};
            PreparedStatement stmt = this.mDb.getConn().prepareStatement(insertHDSeedSql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setString(i + 1, params[i]);
                }
            }
            stmt.executeUpdate();
            stmt.close();
            if (!AddressProvider.getInstance().hasPasswordSeed(this.mDb.getConn()) && !Utils.isEmpty(addressOfPS)) {
                AddressProvider.getInstance().addPasswordSeed(this.mDb.getConn(), new PasswordSeed(addressOfPS, encryptedMnemonicSeed));
            }
            this.mDb.getConn().commit();
            PreparedStatement statement = this.mDb.getPreparedStatement("select hd_account_id from enterprise_hdm_account where encrypt_seed=? and encrypt_mnemonic_seed=? and is_xrandom=? and hd_address=?"
                    , new String[]{encryptedMnemonicSeed, encryptHdSeed, Integer.toString(isXrandom ? 1 : 0), firstAddress});
            ResultSet cursor = statement.executeQuery();

            if (cursor.next()) {
                int idColumn = cursor.findColumn("hd_account_id");
                if (idColumn != -1) {
                    result = cursor.getInt(idColumn);
                }

            }
            cursor.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void completeHDMAddresses(int hdSeedId, List<EnDesktopHDMAddress> addresses) {

    }

    public void prepareHDMAddresses(int hdSeedId, List<HDMAddress.Pubs> pubs) {

    }

    public int maxHDMAddressPubIndex(int hdSeedId) {
        return 0;
    }

    public List<EnDesktopHDMAddress> getHDMAddressInUse(EnDesktopHDMKeychain keychain) {
        return null;
    }

    public boolean isHDSeedFromXRandom(int hdSeedId) {
        return true;
    }


    public String getEncryptMnemonicSeed(int hdSeedId) {
        return null;
    }

    public String getEncryptHDSeed(int hdSeedId) {
        return null;
    }

    public String getHDMFristAddress(int hdSeedId) {
        return null;
    }

    public void addAddress(EnDesktopHDMAddress address) {


    }
}
