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
import net.bither.bitherj.core.Address;
import net.bither.bitherj.db.imp.AbstractAddressProvider;
import net.bither.bitherj.db.imp.base.IDb;
import net.bither.bitherj.utils.Base58;
import net.bither.bitherj.utils.Utils;
import net.bither.db.base.JavaDb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Address2Provider extends AbstractAddressProvider {

    private static Address2Provider addressProvider = new Address2Provider(ApplicationInstanceManager.addressDBHelper);

    public static Address2Provider getInstance() {
        return addressProvider;
    }

    private AddressDBHelper helper;

    public Address2Provider(AddressDBHelper helper) {
        this.helper = helper;
    }

    @Override
    public IDb getReadDb() {
        return new JavaDb(this.helper.getConn());
    }

    @Override
    public IDb getWriteDb() {
        return new JavaDb(this.helper.getConn());
    }

    @Override
    protected int insertHDKeyToDb(IDb db, String encryptedMnemonicSeed, String encryptHdSeed, String firstAddress, boolean isXrandom) {
        String insertHDSeedSql = "insert into hd_seeds (encrypt_seed,encrypt_hd_seed,is_xrandom,hdm_address) values (?,?,?,?) ";
        String[] params = new String[]{encryptedMnemonicSeed, encryptHdSeed, Integer.toString(isXrandom ? 1 : 0), firstAddress};
        try {
            PreparedStatement stmt = ((JavaDb) db).getConnection().prepareStatement(insertHDSeedSql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setString(i + 1, params[i]);
                }
            }
            stmt.executeUpdate();
            ResultSet tableKeys = stmt.getGeneratedKeys();
            tableKeys.next();
            stmt.close();
            return tableKeys.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    protected int insertEnterpriseHDKeyToDb(IDb db, String encryptedMnemonicSeed, String encryptHdSeed, String firstAddress, boolean isXrandom) {
        return 0;
    }

    @Override
    protected void insertHDMAddressToDb(IDb db, String address, int hdSeedId, int index, byte[] pubKeysHot, byte[] pubKeysCold, byte[] pubKeysRemote, boolean isSynced) {
        String insertHDMAddressSql = "insert into hdm_addresses " +
                "(hd_seed_id,hd_seed_index,pub_key_hot,pub_key_cold,pub_key_remote,address,is_synced)" +
                " values (?,?,?,?,?,?,?) ";
        try {
            PreparedStatement stmt = ((JavaDb) db).getConnection().prepareStatement(insertHDMAddressSql);
            stmt.setString(1, Integer.toString(hdSeedId));
            stmt.setString(2, Integer.toString(index));
            stmt.setString(3, Base58.encode(pubKeysHot));
            stmt.setString(4, Base58.encode(pubKeysCold));
            stmt.setString(5, pubKeysRemote == null ? null : Base58.encode(pubKeysRemote));
            stmt.setString(6, Utils.isEmpty(address) ? null : address);
            stmt.setString(7, Integer.toString(isSynced ? 1 : 0));
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void insertAddressToDb(IDb db, Address address) {
        String insertAddressSql = "insert into addresses " +
                "(address,encrypt_private_key,pub_key,is_xrandom,is_trash,is_synced,sort_time)" +
                " values (?,?,?,?,?,?,?) ";
        String[] params = new String[]{address.getAddress(), address.hasPrivKey() ? address.getEncryptPrivKeyOfDb() : null, Base58.encode(address.getPubKey()),
                Integer.toString(address.isFromXRandom() ? 1 : 0), Integer.toString(address.isTrashed() ? 1 : 0), Integer.toString(address.isSyncComplete() ? 1 : 0), Long.toString(address.getSortTime())};
        try {
            PreparedStatement stmt = ((JavaDb) db).getConnection().prepareStatement(insertAddressSql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setString(i + 1, params[i]);
                }
            }
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
