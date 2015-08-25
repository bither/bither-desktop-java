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
import net.bither.bitherj.core.AbstractHD;
import net.bither.bitherj.crypto.PasswordSeed;
import net.bither.bitherj.db.imp.AbstractHDAccountProvider;
import net.bither.bitherj.db.imp.base.IDb;
import net.bither.bitherj.utils.Base58;
import net.bither.db.base.JavaDb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class HDAccountProvider extends AbstractHDAccountProvider {

    private static HDAccountProvider hdAccountProvider = new HDAccountProvider(ApplicationInstanceManager.addressDBHelper);

    public static HDAccountProvider getInstance() {
        return hdAccountProvider;
    }

    private AddressDBHelper helper;

    public HDAccountProvider(AddressDBHelper helper) {
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
    protected int insertHDAccountToDb(IDb db, String encryptedMnemonicSeed, String encryptSeed, String firstAddress, boolean isXrandom, byte[] externalPub, byte[] internalPub, AbstractHD.HDAccountType hdAccountType) {
        try {
            String sql = "insert into hd_account(encrypt_seed,encrypt_mnemonic_seed,is_xrandom,hd_address,external_pub,internal_pub,hd_account_type) values(?,?,?,?,?,?,?);";
            PreparedStatement stmt = ((JavaDb) db).getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, encryptSeed);
            stmt.setString(2, encryptedMnemonicSeed);
            stmt.setInt(3, isXrandom ? 1 : 0);
            stmt.setString(4, firstAddress);
            stmt.setString(5, Base58.encode(externalPub));
            stmt.setString(6, Base58.encode(internalPub));
            stmt.setInt(7, hdAccountType.getValue());
            stmt.executeUpdate();
            ResultSet tableKeys = stmt.getGeneratedKeys();
            tableKeys.next();
            stmt.close();
            return tableKeys.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    protected int insertMonitorHDAccountToDb(IDb db, String firstAddress, boolean isXrandom, byte[] externalPub, byte[] internalPub) {
        try {
            String sql = "insert into hd_account(is_xrandom,hd_address,external_pub,internal_pub,hd_account_type) values(?,?,?,?,?);";
            PreparedStatement stmt = ((JavaDb) db).getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, isXrandom ? 1 : 0);
            stmt.setString(2, firstAddress);
            stmt.setString(3, Base58.encode(externalPub));
            stmt.setString(4, Base58.encode(internalPub));
            stmt.setInt(5, AbstractHD.HDAccountType.HD_MONITOR.getValue());
            stmt.executeUpdate();
            ResultSet tableKeys = stmt.getGeneratedKeys();
            tableKeys.next();
            stmt.close();
            return tableKeys.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    protected int insertMonitorHDMAccountToDb(IDb db, String firstAddress, boolean isXrandom, byte[] externalPub, byte[] internalPub) {
        try {
            String sql = "insert into hd_account(is_xrandom,hd_address,external_pub,internal_pub,hd_account_type) values(?,?,?,?,?);";
            PreparedStatement stmt = ((JavaDb) db).getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, isXrandom ? 1 : 0);
            stmt.setString(2, firstAddress);
            stmt.setString(3, Base58.encode(externalPub));
            stmt.setString(4, Base58.encode(internalPub));
            stmt.setInt(5, AbstractHD.HDAccountType.HDM_MONITOR.getValue());
            stmt.executeUpdate();
            ResultSet tableKeys = stmt.getGeneratedKeys();
            tableKeys.next();
            stmt.close();
            return tableKeys.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    protected boolean hasPasswordSeed(IDb db) {
        return AddressProvider.getInstance().hasPasswordSeed(db);
    }

    @Override
    protected void addPasswordSeed(IDb db, PasswordSeed passwordSeed) {
        AddressProvider.getInstance().addPasswordSeed(db, passwordSeed);
    }
}
