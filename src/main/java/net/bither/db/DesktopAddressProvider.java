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
import net.bither.bitherj.crypto.PasswordSeed;
import net.bither.bitherj.db.AbstractDb;
import net.bither.bitherj.db.IDesktopAddressProvider;
import net.bither.bitherj.exception.AddressFormatException;
import net.bither.bitherj.utils.Base58;
import net.bither.bitherj.utils.Utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DesktopAddressProvider implements IDesktopAddressProvider {

    private static final String insert_hd_seed_sql = "insert into enterprise_hdm_account " +
            "(encrypt_mnemonic_seed,encrypt_seed,is_xrandom,hd_address,external_pub,internal_pub)" +
            " values (?,?,?,?,?,?) ";

    private static DesktopAddressProvider addressProvider =
            new DesktopAddressProvider(ApplicationInstanceManager.addressDBHelper);

    public static DesktopAddressProvider getInstance() {
        return addressProvider;
    }

    private DesktopAddressProvider(AddressDBHelper db) {
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
            PreparedStatement stmt = this.mDb.getConn().prepareStatement(insert_hd_seed_sql);
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

    public void addHDMPub(List<byte[]> externalPubs, List<byte[]> internalPubs) {
        try {
            String hdSeedSql = "insert into enterprise_hdm_account " +
                    "(external_pub,internal_pub)" +
                    " values (?,?)";
            this.mDb.getConn().setAutoCommit(false);
            for (int i = 0; i < externalPubs.size(); i++) {
                PreparedStatement statement = this.mDb.getPreparedStatement(hdSeedSql, new String[]{
                        Base58.encode(externalPubs.get(i)), Base58.encode(internalPubs.get(i))
                });
                statement.close();
            }
            this.mDb.getConn().commit();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public List<byte[]> getExternalPubs() {

        List<byte[]> externalPubs = new ArrayList<byte[]>();
        try {
            String sql = "select external_pub from enterprise_hdm_account where encrypt_seed=null order by hd_account_id asc";
            PreparedStatement stmt = this.mDb.getConn().prepareStatement(sql);
            ResultSet cursor = stmt.executeQuery();
            while (cursor.next()) {
                int idColumn = cursor.findColumn("external_pub");
                if (idColumn != -1) {
                    String str = cursor.getString(idColumn);
                    externalPubs.add(Base58.decode(str));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        }
        return externalPubs;

    }

    public List<byte[]> getInternalPubs() {
        List<byte[]> internalPubs = new ArrayList<byte[]>();
        try {
            String sql = "select internal_pub from enterprise_hdm_account where encrypt_seed=null order by hd_account_id asc";
            PreparedStatement stmt = this.mDb.getConn().prepareStatement(sql);
            ResultSet cursor = stmt.executeQuery();
            while (cursor.next()) {
                int idColumn = cursor.findColumn("internal_pub");
                if (idColumn != -1) {
                    String str = cursor.getString(idColumn);
                    internalPubs.add(Base58.decode(str));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        }
        return internalPubs;

    }

    public boolean isHDSeedFromXRandom(int hdSeedId) {
        boolean isXRandom = false;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select is_xrandom from enterprise_hdm_account order by hd_seed_id asc limit 1"
                    , new String[]{Integer.toString(hdSeedId)});
            ResultSet cursor = statement.executeQuery();
            if (cursor.next()) {
                int idColumn = cursor.findColumn(AbstractDb.EnterpriseHDAccountColumns.IS_XRANDOM);
                if (idColumn != -1) {
                    isXRandom = cursor.getInt(idColumn) == 1;
                }
            }
            cursor.close();
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return isXRandom;

    }


    public String getEncryptMnemonicSeed(int hdSeedId) {
        String encryptMnemonicSeed = null;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select encrypt_mnemonic_seed from enterprise_hdm_account order by hd_seed_id asc limit 1"
                    , new String[]{Integer.toString(hdSeedId)});
            ResultSet cursor = statement.executeQuery();
            if (cursor.next()) {
                int idColumn = cursor.findColumn(AbstractDb.EnterpriseHDAccountColumns.ENCRYPT_MNEMONIC_SEED);
                if (idColumn != -1) {
                    encryptMnemonicSeed = cursor.getString(idColumn);
                }
            }
            cursor.close();
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return encryptMnemonicSeed;
    }

    public String getEncryptHDSeed(int hdSeedId) {
        String encryptSeed = null;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select encrypt_seed from enterprise_hdm_account order by hd_seed_id asc limit 1"
                    , new String[]{Integer.toString(hdSeedId)});
            ResultSet cursor = statement.executeQuery();
            if (cursor.next()) {
                int idColumn = cursor.findColumn(AbstractDb.EnterpriseHDAccountColumns.ENCRYPT_SEED);
                if (idColumn != -1) {
                    encryptSeed = cursor.getString(idColumn);
                }
            }
            cursor.close();
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return encryptSeed;
    }

    public String getHDMFristAddress(int hdSeedId) {
        String address = null;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select hd_address from enterprise_hdm_account order by hd_seed_id asc limit 1"
                    , new String[]{Integer.toString(hdSeedId)});
            ResultSet cursor = statement.executeQuery();
            if (cursor.next()) {
                int idColumn = cursor.findColumn(AbstractDb.EnterpriseHDAccountColumns.ENCRYPT_SEED);
                if (idColumn != -1) {
                    address = cursor.getString(idColumn);
                }
            }
            cursor.close();
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return address;
    }

    @Override
    public List<Integer> getDesktopKeyChainSeed() {
        List<Integer> seeds = new ArrayList<Integer>();
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select hd_account_id from enterprise_hdm_account where  encrypt_seed is not null order by hd_seed_id asc "
                    , null);

            ResultSet cursor = statement.executeQuery();
            if (cursor.next()) {
                int idColumn = cursor.findColumn(AbstractDb.EnterpriseHDAccountColumns.HD_ACCOUNT_ID);
                if (idColumn != -1) {
                    seeds.add(cursor.getInt(idColumn));
                }
            }
            cursor.close();
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return seeds;
    }


}
