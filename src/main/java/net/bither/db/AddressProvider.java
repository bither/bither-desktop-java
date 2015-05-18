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
import net.bither.bitherj.core.HDMAddress;
import net.bither.bitherj.core.HDMBId;
import net.bither.bitherj.core.HDMKeychain;
import net.bither.bitherj.crypto.EncryptedData;
import net.bither.bitherj.crypto.PasswordSeed;
import net.bither.bitherj.db.AbstractDb;
import net.bither.bitherj.db.IAddressProvider;
import net.bither.bitherj.exception.AddressFormatException;
import net.bither.bitherj.utils.Base58;
import net.bither.bitherj.utils.Utils;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressProvider implements IAddressProvider {

    private static AddressProvider addressProvider =
            new AddressProvider(ApplicationInstanceManager.addressDBHelper);

    private static final String insertHDSeedSql = "insert into hd_seeds " +
            "(encrypt_seed,encrypt_hd_seed,is_xrandom,hdm_address)" +
            " values (?,?,?,?) ";


    private static final String insertAddressSql = "insert into addresses " +
            "(address,encrypt_private_key,pub_key,is_xrandom,is_trash,is_synced,sort_time)" +
            " values (?,?,?,?,?,?,?) ";

    private static final String insertHDMBidSql = "insert into hdm_bid " +
            "(hdm_bid,encrypt_bither_password)" +
            " values (?,?) ";
    private static final String updateHDMBidSql = "update hdm_bid set " +
            " encrypt_bither_password=? where hdm_bid=?";


    public static AddressProvider getInstance() {
        return addressProvider;
    }

    private AddressDBHelper mDb;


    private AddressProvider(AddressDBHelper db) {
        this.mDb = db;
    }

    @Override
    public boolean changePassword(CharSequence oldPassword, CharSequence newPassword) {
        final HashMap<String, String> addressesPrivKeyHashMap = new HashMap<String, String>();
        String hdmEncryptPassword = null;
        PasswordSeed passwordSeed = null;
        final HashMap<Integer, String> encryptMnemonicSeedHashMap = new HashMap<Integer, String>();
        final HashMap<Integer, String> encryptHDSeedHashMap = new HashMap<Integer, String>();

        HashMap<Integer, String> hdEncryptSeedHashMap = new HashMap<Integer, String>();
        HashMap<Integer, String> hdEncryptMnemonicSeedHashMap = new HashMap<Integer, String>();


        HashMap<Integer, String> singularModeBackupHashMap = new HashMap<Integer, String>();
        try {
            String sql = "select address,encrypt_private_key,pub_key,is_xrandom from addresses where encrypt_private_key is not null";
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, null);
            ResultSet c = statement.executeQuery();
            while (c.next()) {
                String address = null;
                String encryptPrivKey = null;
                String pubKey = null;
                boolean isXRandom = false;
                int idColumn = c.findColumn(AbstractDb.AddressesColumns.ADDRESS);
                if (idColumn != -1) {
                    address = c.getString(idColumn);
                }
                idColumn = c.findColumn(AbstractDb.AddressesColumns.ENCRYPT_PRIVATE_KEY);
                if (idColumn != -1) {
                    encryptPrivKey = c.getString(idColumn);
                }
                idColumn = c.findColumn(AbstractDb.AddressesColumns.PUB_KEY);
                if (idColumn != -1) {
                    pubKey = c.getString(idColumn);
                }
                boolean isCompressed = true;
                try {
                    isCompressed = Base58.decode(pubKey).length == 33;
                } catch (AddressFormatException e) {
                    e.printStackTrace();
                }
                idColumn = c.findColumn(AbstractDb.AddressesColumns.PUB_KEY);
                if (idColumn != -1) {
                    isXRandom = c.getBoolean(idColumn);
                }
                addressesPrivKeyHashMap.put(address, new EncryptedData(encryptPrivKey).toEncryptedStringForQRCode(isCompressed, isXRandom));
            }
            c.close();
            statement.close();
            sql = "select encrypt_bither_password from hdm_bid limit 1";
            statement = this.mDb.getPreparedStatement(sql, null);
            c = statement.executeQuery();
            if (c.next()) {
                int idColumn = c.findColumn(AbstractDb.HDMBIdColumns.ENCRYPT_BITHER_PASSWORD);
                if (idColumn != -1) {
                    hdmEncryptPassword = c.getString(idColumn);
                }
            } else {
                hdmEncryptPassword = null;
            }
            c.close();
            statement.close();
            sql = "select hd_seed_id,encrypt_seed,encrypt_hd_seed,singular_mode_backup from hd_seeds where encrypt_seed!='RECOVER'";
            statement = this.mDb.getPreparedStatement(sql, null);
            c = statement.executeQuery();
            while (c.next()) {
                int idColumn = c.findColumn(AbstractDb.HDSeedsColumns.HD_SEED_ID);
                Integer hdSeedId = 0;
                if (idColumn != -1) {
                    hdSeedId = c.getInt(idColumn);
                }

                String encryptMnemonicSeed = null;
                idColumn = c.findColumn(AbstractDb.HDSeedsColumns.ENCRYPT_MNEMONIC_SEED);
                if (idColumn != -1) {
                    encryptMnemonicSeed = c.getString(idColumn);
                }
                idColumn = c.findColumn(AbstractDb.HDSeedsColumns.ENCRYPT_HD_SEED);
                if (idColumn != -1) {
                    String encryptHDSeed = c.getString(idColumn);
                    if (!Utils.isEmpty(encryptHDSeed)) {
                        encryptHDSeedHashMap.put(hdSeedId, encryptHDSeed);
                    }
                }
                idColumn = c.findColumn(AbstractDb.HDSeedsColumns.SINGULAR_MODE_BACKUP);
                if (idColumn != -1) {
                    String singularModeBackup = c.getString(idColumn);
                    if (!Utils.isEmpty(singularModeBackup)) {
                        singularModeBackupHashMap.put(hdSeedId, singularModeBackup);
                    }
                }
                encryptMnemonicSeedHashMap.put(hdSeedId, encryptMnemonicSeed);
            }
            c.close();
            statement.close();
            statement = this.mDb.getPreparedStatement("select hd_account_id,encrypt_seed,encrypt_mnemonic_seed from hd_account  ", null);
            c = statement.executeQuery();
            while (c.next()) {
                int idColumn = c.findColumn(AbstractDb.HDAccountColumns.HD_ACCOUNT_ID);
                Integer hdAccountId = 0;
                if (idColumn != -1) {
                    hdAccountId = c.getInt(idColumn);
                }
                idColumn = c.findColumn(AbstractDb.HDAccountColumns.ENCRYPT_SEED);
                if (idColumn != -1) {
                    String encryptSeed = c.getString(idColumn);
                    hdEncryptSeedHashMap.put(hdAccountId, encryptSeed);
                }
                idColumn = c.findColumn(AbstractDb.HDAccountColumns.ENCRYPT_MNMONIC_SEED);
                if (idColumn != -1) {
                    String encryptHDSeed = c.getString(idColumn);
                    hdEncryptMnemonicSeedHashMap.put(hdAccountId, encryptHDSeed);
                }

            }
            c.close();
            statement.close();

            sql = "select password_seed from password_seed limit 1";
            statement = this.mDb.getPreparedStatement(sql, null);
            c = statement.executeQuery();
            if (c.next()) {
                int idColumn = c.findColumn(AbstractDb.PasswordSeedColumns.PASSWORD_SEED);
                if (idColumn != -1) {
                    passwordSeed = new PasswordSeed(c.getString(idColumn));
                }
            }
            c.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        for (Map.Entry<String, String> kv : addressesPrivKeyHashMap.entrySet()) {
            kv.setValue(EncryptedData.changePwdKeepFlag(kv.getValue(), oldPassword, newPassword));
        }
        if (hdmEncryptPassword != null) {
            hdmEncryptPassword = EncryptedData.changePwd(hdmEncryptPassword, oldPassword, newPassword);
        }
        for (Map.Entry<Integer, String> kv : encryptMnemonicSeedHashMap.entrySet()) {
            kv.setValue(EncryptedData.changePwd(kv.getValue(), oldPassword, newPassword));
        }
        for (Map.Entry<Integer, String> kv : encryptHDSeedHashMap.entrySet()) {
            kv.setValue(EncryptedData.changePwd(kv.getValue(), oldPassword, newPassword));
        }

        for (Map.Entry<Integer, String> kv : singularModeBackupHashMap.entrySet()) {
            kv.setValue(EncryptedData.changePwd(kv.getValue(), oldPassword, newPassword));
        }

        for (Map.Entry<Integer, String> kv : hdEncryptSeedHashMap.entrySet()) {
            kv.setValue(EncryptedData.changePwd(kv.getValue(), oldPassword, newPassword));
        }
        for (Map.Entry<Integer, String> kv : hdEncryptMnemonicSeedHashMap.entrySet()) {
            kv.setValue(EncryptedData.changePwd(kv.getValue(), oldPassword, newPassword));
        }
        if (passwordSeed != null) {
            boolean result = passwordSeed.changePassword(oldPassword, newPassword);
            if (!result) {
                return false;
            }
        }
        final String finalHdmEncryptPassword = hdmEncryptPassword;
        final PasswordSeed finalPasswordSeed = passwordSeed;
        try {

            this.mDb.getConn().setAutoCommit(false);
            String sql = "update addresses set encrypt_private_key=? where  address=? ";
            for (Map.Entry<String, String> kv : addressesPrivKeyHashMap.entrySet()) {
                PreparedStatement stmt = this.mDb.getConn().prepareStatement(sql);
                stmt.setString(1, kv.getValue());
                stmt.setString(2, kv.getKey());
                stmt.executeUpdate();
                stmt.close();
            }
            sql = "update hdm_bid set encrypt_bither_password=?  ";
            if (finalHdmEncryptPassword != null) {
                PreparedStatement stmt = this.mDb.getConn().prepareStatement(sql);
                stmt.setString(1, finalHdmEncryptPassword);
                stmt.executeUpdate();
                stmt.close();

            }
            sql = "update hd_seeds set encrypt_seed=? %s %s where  hd_seed_id=? ";
            for (Map.Entry<Integer, String> kv : encryptMnemonicSeedHashMap.entrySet()) {
                String singularModeBackupStr = "";
                if (singularModeBackupHashMap.containsKey(kv.getKey())) {
                    singularModeBackupStr = ",singular_mode_backup='" + singularModeBackupHashMap.get(kv.getKey()) + "'";
                }
                if (encryptHDSeedHashMap.containsKey(kv.getKey())) {
                    sql = Utils.format(sql, ",encrypt_hd_seed='" + encryptHDSeedHashMap.get(kv.getKey()) + "'", singularModeBackupStr);
                }
                PreparedStatement stmt = this.mDb.getConn().prepareStatement(sql);
                stmt.setString(1, kv.getValue());
                stmt.setString(2, kv.getKey().toString());

                stmt.executeUpdate();
                stmt.close();
            }

            sql = "update hd_account set encrypt_mnemonic_seed=?,encrypt_seed=?  where  hd_account_id=? ";

            for (Map.Entry<Integer, String> kv : hdEncryptMnemonicSeedHashMap.entrySet()) {

                PreparedStatement stmt = this.mDb.getConn().prepareStatement(sql);
                stmt.setString(1, kv.getValue());
                stmt.setString(2, hdEncryptSeedHashMap.get(kv.getKey()));
                stmt.setString(3, kv.getKey().toString());


                stmt.executeUpdate();
                stmt.close();
            }
            if (finalPasswordSeed != null) {
                sql = "update password_seed set password_seed=?  ";
                PreparedStatement stmt = this.mDb.getConn().prepareStatement(sql);
                stmt.setString(1, finalPasswordSeed.toPasswordSeedString());
                stmt.executeUpdate();
                stmt.close();
            }
            this.mDb.getConn().commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public PasswordSeed getPasswordSeed() {
        PasswordSeed passwordSeed = null;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select password_seed from password_seed limit 1", null);
            ResultSet c = statement.executeQuery();

            if (c.next()) {
                int idColumn = c.findColumn(AbstractDb.PasswordSeedColumns.PASSWORD_SEED);
                if (idColumn != -1) {
                    passwordSeed = new PasswordSeed(c.getString(idColumn));
                }
            }
            c.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return passwordSeed;
    }

    @Override
    public boolean hasPasswordSeed() {
        boolean result = false;
        try {
            result = hasPasswordSeed(this.mDb.getConn());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    private boolean hasPasswordSeed(Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("select  count(0) cnt from password_seed  where  password_seed is not null ");
        ResultSet c = stmt.executeQuery();
        int count = 0;
        try {
            if (c.next()) {
                int idColumn = c.findColumn("cnt");
                if (idColumn != -1) {
                    count = c.getInt(idColumn);
                }
            }
            c.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count > 0;

    }

    @Override
    public List<Integer> getHDSeeds() {
        List<Integer> hdSeedIds = new ArrayList<Integer>();
        try {
            String sql = "select hd_seed_id from hd_seeds";
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, null);
            ResultSet c = statement.executeQuery();

            while (c.next()) {
                int idColumn = c.findColumn(AbstractDb.HDSeedsColumns.HD_SEED_ID);
                if (idColumn != 0) {
                    hdSeedIds.add(c.getInt(idColumn));
                }
            }
            c.close();
            statement.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

        }
        return hdSeedIds;
    }

    @Override
    public String getEncryptMnemonicSeed(int hdSeedId) {
        String encryptSeed = null;
        try {
            String sql = "select encrypt_seed from hd_seeds where hd_seed_id=?";
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{Integer.toString(hdSeedId)});
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                int idColumn = c.findColumn(AbstractDb.HDSeedsColumns.ENCRYPT_MNEMONIC_SEED);
                if (idColumn != -1) {
                    encryptSeed = c.getString(idColumn);
                }
            }
            c.close();
            statement.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return encryptSeed;
    }


    @Override
    public String getEncryptHDSeed(int hdSeedId) {
        String encryptHDSeed = null;

        try {
            String sql = "select encrypt_hd_seed from hd_seeds where hd_seed_id=?";
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{Integer.toString(hdSeedId)});
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                int idColumn = c.findColumn(AbstractDb.HDSeedsColumns.ENCRYPT_HD_SEED);
                if (idColumn != -1) {
                    encryptHDSeed = c.getString(idColumn);
                }
            }
            c.close();
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return encryptHDSeed;
    }

    @Override
    public void updateEncrypttMnmonicSeed(int hdSeedId, String encryptMnmonicSeed) {
        this.mDb.executeUpdate("update hd_seeds set encrypt_seed=? where hd_seed_id=?",
                new String[]{encryptMnmonicSeed, Integer.toString(hdSeedId)});
    }


    @Override
    public boolean isHDSeedFromXRandom(int hdSeedId) {
        boolean isXRandom = false;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select is_xrandom from hd_seeds where hd_seed_id=?"
                    , new String[]{Integer.toString(hdSeedId)});
            ResultSet cursor = statement.executeQuery();
            if (cursor.next()) {
                int idColumn = cursor.findColumn(AbstractDb.HDSeedsColumns.IS_XRANDOM);
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


    @Override
    public String getHDMFristAddress(int hdSeedId) {
        String address = null;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select hdm_address from hd_seeds where hd_seed_id=?"
                    , new String[]{Integer.toString(hdSeedId)});
            ResultSet cursor = statement.executeQuery();
            if (cursor.next()) {
                int idColumn = cursor.findColumn(AbstractDb.HDSeedsColumns.HDM_ADDRESS);
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
    public int addHDKey(final String encryptedMnemonicSeed, final String encryptHdSeed, final String firstAddress, final boolean isXrandom, final String addressOfPS) {
        int result = 0;
        try {
            this.mDb.getConn().setAutoCommit(false);
            String[] params = new String[]{encryptedMnemonicSeed, encryptHdSeed, Integer.toString(isXrandom ? 1 : 0), firstAddress};
            PreparedStatement stmt = this.mDb.getConn().prepareStatement(insertHDSeedSql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setString(i + 1, params[i]);
                }
            }
            stmt.executeUpdate();
            stmt.close();
            if (!hasPasswordSeed(this.mDb.getConn()) && !Utils.isEmpty(addressOfPS)) {
                addPasswordSeed(this.mDb.getConn(), new PasswordSeed(addressOfPS, encryptedMnemonicSeed));
            }
            this.mDb.getConn().commit();
            PreparedStatement statement = this.mDb.getPreparedStatement("select hd_seed_id from hd_seeds where encrypt_seed=? and encrypt_hd_seed=? and is_xrandom=? and hdm_address=?"
                    , new String[]{encryptedMnemonicSeed, encryptHdSeed, Integer.toString(isXrandom ? 1 : 0), firstAddress});
            ResultSet cursor = statement.executeQuery();

            if (cursor.next()) {
                int idColumn = cursor.findColumn(AbstractDb.HDSeedsColumns.HD_SEED_ID);
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

    @Override
    public HDMBId getHDMBId() {
        HDMBId hdmbId = null;
        ResultSet c = null;
        String address = null;
        String encryptBitherPassword = null;
        try {

            String sql = "select " + AbstractDb.HDMBIdColumns.HDM_BID + "," + AbstractDb.HDMBIdColumns.ENCRYPT_BITHER_PASSWORD + " from " +
                    AbstractDb.Tables.HDM_BID;
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, null);
            c = statement.executeQuery();
            if (c.next()) {
                int idColumn = c.findColumn(AbstractDb.HDMBIdColumns.HDM_BID);
                if (idColumn != -1) {
                    address = c.getString(idColumn);
                }
                idColumn = c.findColumn(AbstractDb.HDMBIdColumns.ENCRYPT_BITHER_PASSWORD);
                if (idColumn != -1) {
                    encryptBitherPassword = c.getString(idColumn);
                }

            }
            if (!Utils.isEmpty(address) && !Utils.isEmpty(encryptBitherPassword)) {
                hdmbId = new HDMBId(address, encryptBitherPassword);
            }
            c.close();
            statement.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return hdmbId;
    }

    @Override
    public void addAndUpdateHDMBId(final HDMBId bitherId, final String addressOfPS) {

        boolean isExist = true;

        try {
            String sql = "select count(0) cnt from " + AbstractDb.Tables.HDM_BID;
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, null);
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                int idColumn = c.findColumn("cnt");
                isExist = c.getInt(idColumn) > 0;
            }

            c.close();
            statement.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (!isExist) {
            try {
                this.mDb.getConn().setAutoCommit(false);
                String encryptedBitherPasswordString = bitherId.getEncryptedBitherPasswordString();
                PreparedStatement stmt = this.mDb.getConn().prepareStatement(insertHDMBidSql);
                stmt.setString(1, bitherId.getAddress());
                stmt.setString(2, encryptedBitherPasswordString);
                stmt.executeUpdate();
                if (!hasPasswordSeed(this.mDb.getConn()) && !Utils.isEmpty(addressOfPS)) {
                    addPasswordSeed(this.mDb.getConn(), new PasswordSeed(addressOfPS, encryptedBitherPasswordString));
                }
                this.mDb.getConn().commit();
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try {
                this.mDb.getConn().setAutoCommit(false);
                String encryptedBitherPasswordString = bitherId.getEncryptedBitherPasswordString();
                PreparedStatement stmt = this.mDb.getConn().prepareStatement(updateHDMBidSql);
                stmt.setString(1, encryptedBitherPasswordString);
                stmt.setString(2, bitherId.getAddress());
                stmt.executeUpdate();
                if (!hasPasswordSeed(this.mDb.getConn()) && !Utils.isEmpty(addressOfPS)) {
                    addPasswordSeed(this.mDb.getConn(), new PasswordSeed(addressOfPS, encryptedBitherPasswordString));
                }
                this.mDb.getConn().commit();
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<HDMAddress> getHDMAddressInUse(HDMKeychain keychain) {
        List<HDMAddress> addresses = new ArrayList<HDMAddress>();

        try {
            ResultSet c = null;

            String sql = "select hd_seed_index,pub_key_hot,pub_key_cold,pub_key_remote,address,is_synced " +
                    "from hdm_addresses " +
                    "where hd_seed_id=? and address is not null order by hd_seed_index";
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{Integer.toString(keychain.getHdSeedId())});
            c = statement.executeQuery();
            while (c.next()) {
                HDMAddress hdmAddress = applyHDMAddress(c, keychain);
                if (hdmAddress != null) {
                    addresses.add(hdmAddress);
                }
            }
            c.close();
            statement.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return addresses;
    }


    @Override
    public void prepareHDMAddresses(final int hdSeedId, final List<HDMAddress.Pubs> pubsList) {


        boolean isExist = false;
        try {
            for (HDMAddress.Pubs pubs : pubsList) {
                String sql = "select count(0) cnt from hdm_addresses where hd_seed_id=? and hd_seed_index=?";
                PreparedStatement stmt = this.mDb.getConn().prepareStatement(sql);
                stmt.setString(1, Integer.toString(hdSeedId));
                stmt.setString(2, Integer.toString(pubs.index));
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int idColumn = rs.findColumn("cnt");
                    if (idColumn != -1) {
                        isExist |= rs.getInt(idColumn) > 0;
                    }
                }
                rs.close();
                stmt.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            isExist = true;
        }
        try {
            if (!isExist) {
                this.mDb.getConn().setAutoCommit(false);
                for (int i = 0; i < pubsList.size(); i++) {
                    HDMAddress.Pubs pubs = pubsList.get(i);
                    applyHDMAddressContentValues(this.mDb.getConn(), null, hdSeedId, pubs.index, pubs.hot, pubs.cold, null, false);

                }
                this.mDb.getConn().commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public List<HDMAddress.Pubs> getUncompletedHDMAddressPubs(int hdSeedId, int count) {

        List<HDMAddress.Pubs> pubsList = new ArrayList<HDMAddress.Pubs>();
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select * from hdm_addresses where hd_seed_id=? and pub_key_remote is null limit ? ", new String[]{
                    Integer.toString(hdSeedId), Integer.toString(count)
            });
            ResultSet cursor = statement.executeQuery();
            try {
                while (cursor.next()) {
                    HDMAddress.Pubs pubs = applyPubs(cursor);
                    if (pubs != null) {
                        pubsList.add(pubs);
                    }
                }
            } catch (AddressFormatException e) {
                e.printStackTrace();
            }

            cursor.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pubsList;
    }

    @Override
    public int maxHDMAddressPubIndex(int hdSeedId) {
        int maxIndex = -1;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select ifnull(max(hd_seed_index),-1)  hd_seed_index from hdm_addresses where hd_seed_id=?  ", new String[]{
                    Integer.toString(hdSeedId)
            });
            ResultSet cursor = statement.executeQuery();

            if (cursor.next()) {
                int idColumn = cursor.findColumn(AbstractDb.HDMAddressesColumns.HD_SEED_INDEX);
                if (idColumn != -1) {
                    maxIndex = cursor.getInt(idColumn);
                }
            }
            cursor.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maxIndex;
    }

    @Override
    public int uncompletedHDMAddressCount(int hdSeedId) {

        int count = 0;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select count(0) cnt from hdm_addresses where hd_seed_id=?  and pub_key_remote is null "
                    , new String[]{
                    Integer.toString(hdSeedId)
            });
            ResultSet cursor = statement.executeQuery();
            if (cursor.next()) {
                int idColumn = cursor.findColumn("cnt");
                if (idColumn != -1) {
                    count = cursor.getInt(idColumn);
                }
            }
            cursor.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;

    }


    @Override
    public void completeHDMAddresses(final int hdSeedId, final List<HDMAddress> addresses) {
        try {
            boolean isExist = true;
            ResultSet c = null;
            try {
                for (HDMAddress address : addresses) {
                    String sql = "select count(0) cnt from hdm_addresses " +
                            "where hd_seed_id=? and hd_seed_index=? and address is null";
                    PreparedStatement stmt = this.mDb.getConn().prepareStatement(sql);
                    stmt.setString(1, Integer.toString(hdSeedId));
                    stmt.setString(2, Integer.toString(address.getIndex()));
                    c = stmt.executeQuery();
                    if (c.next()) {
                        int idColumn = c.findColumn("cnt");
                        if (idColumn != -1) {
                            isExist &= c.getInt(idColumn) > 0;
                        }
                    }
                    c.close();
                    stmt.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                isExist = false;
            } finally {
                if (c != null && !c.isClosed())
                    c.close();
            }
            if (isExist) {
                this.mDb.getConn().setAutoCommit(false);
                for (int i = 0; i < addresses.size(); i++) {
                    HDMAddress address = addresses.get(i);
                    String sql = "update hdm_addresses set pub_key_remote=?,address=? where hd_seed_id=? and hd_seed_index=?";
                    PreparedStatement stmt = this.mDb.getConn().prepareStatement(sql);
                    stmt.setString(1, Base58.encode(address.getPubRemote()));
                    stmt.setString(2, address.getAddress());
                    stmt.setString(3, Integer.toString(hdSeedId));
                    stmt.setString(4, Integer.toString(address.getIndex()));
                    stmt.executeUpdate();
                    stmt.close();
                }
                this.mDb.getConn().commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void setHDMPubsRemote(final int hdSeedId, final int index, final byte[] remote) {
        try {
            boolean isExist = true;
            ResultSet c = null;
            try {
                String sql = "select count(0) cnt from hdm_addresses " +
                        "where hd_seed_id=? and hd_seed_index=? and address is null";
                PreparedStatement stmt = this.mDb.getConn().prepareStatement(sql);
                stmt.setString(1, Integer.toString(hdSeedId));

                c = stmt.executeQuery();
                if (c.next()) {
                    int idColumn = c.findColumn("cnt");
                    if (idColumn != -1) {
                        isExist &= c.getInt(0) > 0;
                    }
                }
                c.close();
                stmt.close();

            } catch (Exception ex) {
                ex.printStackTrace();
                isExist = false;
            } finally {
                if (c != null && !c.isClosed())
                    c.close();
            }
            if (isExist) {
                String sql = "update hdm_addresses set pub_key_remote=? where hd_seed_id=? and hd_seed_index=?";
                PreparedStatement stmt = this.mDb.getConn().prepareStatement(sql);
                stmt.setString(1, Base58.encode(remote));
                stmt.setString(2, Integer.toString(hdSeedId));
                stmt.setString(3, Integer.toString(index));
                stmt.executeUpdate();
                stmt.close();
            }
            this.mDb.getConn().commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    private static final String insertHDMAddressSql = "insert into hdm_addresses " +
            "(hd_seed_id,hd_seed_index,pub_key_hot,pub_key_cold,pub_key_remote,address,is_synced)" +
            " values (?,?,?,?,?,?,?) ";

    @Override
    public void recoverHDMAddresses(final int hdSeedId, final List<HDMAddress> addresses) {
        try {
            this.mDb.getConn().setAutoCommit(false);
            for (int i = 0; i < addresses.size(); i++) {
                HDMAddress address = addresses.get(i);
                applyHDMAddressContentValues(this.mDb.getConn(), address.getAddress(), hdSeedId,
                        address.getIndex(), address.getPubHot(), address.getPubCold(), address.getPubRemote(), false);


            }
            this.mDb.getConn().commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void syncComplete(int hdSeedId, int hdSeedIndex) {
        this.mDb.executeUpdate("update hdm_addresses set is_synced=? where hd_seed_id=? and hd_seed_index=?"
                , new String[]{Integer.toString(1), Integer.toString(hdSeedId), Integer.toString(hdSeedIndex)});
    }

    //normal
    @Override
    public List<Address> getAddresses() {
        List<Address> addressList = new ArrayList<Address>();
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select address,encrypt_private_key,pub_key,is_xrandom,is_trash,is_synced,sort_time " +
                    "from addresses  order by sort_time desc", null);
            ResultSet c = statement.executeQuery();

            while (c.next()) {
                Address address = null;
                try {
                    address = applyAddressCursor(c);
                } catch (AddressFormatException e) {
                    e.printStackTrace();
                }
                if (address != null) {
                    addressList.add(address);
                }
            }
            c.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return addressList;
    }

    @Override
    public String getEncryptPrivateKey(String address) {
        String encryptPrivateKey = null;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select encrypt_private_key from addresses  where address=?", new String[]{address});
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                int idColumn = c.findColumn(AbstractDb.AddressesColumns.ENCRYPT_PRIVATE_KEY);
                if (idColumn != -1) {
                    encryptPrivateKey = c.getString(idColumn);
                }
            }
            c.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return encryptPrivateKey;
    }


    @Override
    public void addAddress(final Address address) {
        try {

            this.mDb.getConn().setAutoCommit(false);
            String[] params = new String[]{address.getAddress(), address.hasPrivKey() ? address.getEncryptPrivKeyOfDb() : null, Base58.encode(address.getPubKey()),
                    Integer.toString(address.isFromXRandom() ? 1 : 0), Integer.toString(address.isSyncComplete() ? 1 : 0), Integer.toString(address.isTrashed() ? 1 : 0), Long.toString(address.getSortTime())};
            PreparedStatement stmt = this.mDb.getConn().prepareStatement(insertAddressSql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setString(i + 1, params[i]);
                }
            }
            stmt.executeUpdate();
            if (address.hasPrivKey()) {
                if (!hasPasswordSeed(this.mDb.getConn())) {
                    PasswordSeed passwordSeed = new PasswordSeed(address.getAddress(), address.getFullEncryptPrivKeyOfDb());
                    addPasswordSeed(this.mDb.getConn(), passwordSeed);
                }
            }
            this.mDb.getConn().commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void updatePrivateKey(String address, String encryptPriv) {
        this.mDb.executeUpdate("update addresses set encrypt_private_key=? where address=?"
                , new String[]{encryptPriv, address});

    }

    @Override
    public void removeWatchOnlyAddress(Address address) {
        this.mDb.executeUpdate("delete from addresses where address=? and encrypt_private_key is null ",
                new String[]{address.getAddress()});

    }


    @Override
    public void trashPrivKeyAddress(Address address) {
        this.mDb.executeUpdate("update addresses set is_trash=1 where address=?"
                , new String[]{address.getAddress()});
    }

    @Override
    public void restorePrivKeyAddress(Address address) {
        this.mDb.executeUpdate("update addresses set is_trash=0 ,is_synced=0,sort_time=? where address=?"
                , new String[]{Long.toString(address.getSortTime()), address.getAddress()});
    }

    @Override
    public void updateSyncComplete(Address address) {

        this.mDb.executeUpdate("update addresses set is_synced=? where address=?"
                , new String[]{Integer.toString(address.isSyncComplete() ? 1 : 0), address.getAddress()});

    }


    @Override
    public String getAlias(String address) {

        String alias = null;

        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select alias from aliases where address=?", new String[]{address});
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                int idColumn = c.findColumn(AbstractDb.AliasColumns.ALIAS);
                if (idColumn != -1) {
                    alias = c.getString(idColumn);
                }
            }
            c.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return alias;
    }

    @Override
    public Map<String, String> getAliases() {
        Map<String, String> stringMap = new HashMap<String, String>();
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select * from aliases", null);
            ResultSet c = statement.executeQuery();
            while (c.next()) {
                int idColumn = c.findColumn(AbstractDb.AliasColumns.ADDRESS);
                String address = null;
                String alias = null;
                if (idColumn > -1) {
                    address = c.getString(idColumn);
                }
                idColumn = c.findColumn(AbstractDb.AliasColumns.ALIAS);
                if (idColumn > -1) {
                    alias = c.getString(idColumn);
                }
                stringMap.put(address, alias);

            }
            c.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stringMap;
    }

    @Override
    public void updateAlias(String address, @Nullable String alias) {
        if (alias == null) {
            this.mDb.executeUpdate("delete from aliases where address=?", new String[]{
                    address
            });

        } else {
            this.mDb.executeUpdate("insert or replace into aliases(address,alias) values(?,?)", new String[]{address, alias});

        }
    }

    @Override
    public int getVanityLen(String address) {
        int vanityLen = Address.VANITY_LEN_NO_EXSITS;

        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select vanity_len from vanity_address where address=?", new String[]{address});
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                int idColumn = c.findColumn(AbstractDb.VanityAddressColumns.VANITY_LEN);
                if (idColumn != -1) {
                    vanityLen = c.getInt(idColumn);
                }
            }
            c.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return vanityLen;
    }

    @Override
    public Map<String, Integer> getVanitylens() {
        Map<String, Integer> stringMap = new HashMap<String, Integer>();
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select * from vanity_address", null);
            ResultSet c = statement.executeQuery();
            while (c.next()) {
                int idColumn = c.findColumn(AbstractDb.VanityAddressColumns.ADDRESS);
                String address = null;
                int vanityLen = Address.VANITY_LEN_NO_EXSITS;
                if (idColumn > -1) {
                    address = c.getString(idColumn);
                }
                idColumn = c.findColumn(AbstractDb.VanityAddressColumns.VANITY_LEN);
                if (idColumn > -1) {
                    vanityLen = c.getInt(idColumn);
                }
                stringMap.put(address, vanityLen);

            }
            c.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stringMap;
    }

    @Override
    public void updateVaitylen(String address, int vanitylen) {
        if (vanitylen == Address.VANITY_LEN_NO_EXSITS) {
            this.mDb.executeUpdate("delete from vanity_address where address=?", new String[]{
                    address
            });

        } else {
            this.mDb.executeUpdate("insert or replace into vanity_address(address,vanity_len) values(?,?)",
                    new String[]{address, Integer.toString(vanitylen)});

        }
    }

    @Override
    public void setSingularModeBackup(int hdSeedId, String singularModeBackup) {
        this.mDb.executeUpdate("update  hd_seeds set singular_mode_backup=? where hd_seed_id=?", new String[]{singularModeBackup, Integer.toString(hdSeedId)});
    }

    @Override
    public String getSingularModeBackup(int hdSeedId) {

        String singularModeBackup = null;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select singular_mode_backup from hd_seeds where hd_seed_id=?"
                    , new String[]{Integer.toString(hdSeedId)});
            ResultSet cursor = statement.executeQuery();
            if (cursor.next()) {
                int idColumn = cursor.findColumn(AbstractDb.HDSeedsColumns.SINGULAR_MODE_BACKUP);
                if (idColumn > 1) {
                    singularModeBackup = cursor.getString(idColumn);
                }
            }
            cursor.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return singularModeBackup;
    }


    @Override
    public int addHDAccount(String encryptedMnemonicSeed, String encryptSeed,
                            String firstAddress, boolean isXrandom, String addressOfPS,
                            byte[] externalPub, byte[] internalPub) {
        int result = 0;
        try {

            String[] params = new String[]{encryptedMnemonicSeed, encryptSeed, firstAddress, Base58.encode(externalPub)
                    , Base58.encode(internalPub), Integer.toString(isXrandom ? 1 : 0),};
            String sql = "insert into hd_account(encrypt_mnemonic_seed,encrypt_seed" +
                    ",hd_address,external_pub,internal_pub,is_xrandom) " +
                    " values(?,?,?,?,?,?)";

            this.mDb.getConn().setAutoCommit(false);

            PreparedStatement stmt = this.mDb.getConn().prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setString(i + 1, params[i]);
                }
            }
            stmt.executeUpdate();
            stmt.close();
            if (!hasPasswordSeed(this.mDb.getConn()) && !Utils.isEmpty(addressOfPS)) {
                addPasswordSeed(this.mDb.getConn(), new PasswordSeed(addressOfPS, encryptedMnemonicSeed));
            }
            this.mDb.getConn().commit();
            stmt = this.mDb.getPreparedStatement("select hd_account_id from hd_account where encrypt_mnemonic_seed=? and encrypt_seed=? and is_xrandom=? and hd_address=?"
                    , new String[]{encryptedMnemonicSeed, encryptSeed, Integer.toString(isXrandom ? 1 : 0), firstAddress});
            ResultSet cursor = stmt.executeQuery();
            if (cursor.next()) {
                int idColumn = cursor.findColumn(AbstractDb.HDAccountColumns.HD_ACCOUNT_ID);
                if (idColumn != -1) {
                    result = cursor.getInt(idColumn);
                }

            }
            cursor.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;

    }

    @Override
    public String getHDFristAddress(int hdSeedId) {
        String address = null;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select hd_address from hd_account where hd_account_id=?"
                    , new String[]{Integer.toString(hdSeedId)});
            ResultSet cursor = statement.executeQuery();
            if (cursor.next()) {
                int idColumn = cursor.findColumn(AbstractDb.HDAccountColumns.HD_ADDRESS);
                if (idColumn != -1) {
                    address = cursor.getString(idColumn);
                }
            }
            cursor.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return address;
    }

    @Override
    public byte[] getExternalPub(int hdSeedId) {
        byte[] pub = null;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select external_pub from hd_account where hd_account_id=? "
                    , new String[]{Integer.toString(hdSeedId)});
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                int idColumn = c.findColumn(AbstractDb.HDAccountColumns.EXTERNAL_PUB);
                if (idColumn != -1) {
                    String pubStr = c.getString(idColumn);
                    pub = Base58.decode(pubStr);
                }
            }
            c.close();
            statement.close();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pub;
    }

    @Override
    public byte[] getInternalPub(int hdSeedId) {
        byte[] pub = null;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select internal_pub from hd_account where hd_account_id=? "
                    , new String[]{Integer.toString(hdSeedId)});
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                int idColumn = c.findColumn(AbstractDb.HDAccountColumns.INTERNAL_PUB);
                if (idColumn != -1) {
                    String pubStr = c.getString(idColumn);
                    pub = Base58.decode(pubStr);
                }
            }
            c.close();
            statement.close();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pub;
    }

    @Override
    public String getHDAccountEncryptSeed(int hdSeedId) {
        String hdAccountEncryptSeed = null;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select " + AbstractDb.HDAccountColumns.ENCRYPT_SEED + " from hd_account where hd_account_id=? "
                    , new String[]{Integer.toString(hdSeedId)});
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                int idColumn = c.findColumn(AbstractDb.HDAccountColumns.ENCRYPT_SEED);
                if (idColumn != -1) {
                    hdAccountEncryptSeed = c.getString(idColumn);
                }
            }
            c.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hdAccountEncryptSeed;

    }

    @Override
    public String getHDAccountEncryptMnmonicSeed(int hdSeedId) {
        String hdAccountMnmonicEncryptSeed = null;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select " + AbstractDb.HDAccountColumns.ENCRYPT_MNMONIC_SEED + " from hd_account where hd_account_id=? "
                    , new String[]{Integer.toString(hdSeedId)});
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                int idColumn = c.findColumn(AbstractDb.HDAccountColumns.ENCRYPT_MNMONIC_SEED);
                if (idColumn != -1) {
                    hdAccountMnmonicEncryptSeed = c.getString(idColumn);
                }
            }
            c.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hdAccountMnmonicEncryptSeed;


    }

    @Override
    public List<Integer> getHDAccountSeeds() {
        List<Integer> hdSeedIds = new ArrayList<Integer>();

        try {

            String sql = "select " + AbstractDb.HDAccountColumns.HD_ACCOUNT_ID + " from " + AbstractDb.Tables.HD_ACCOUNT;
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, null);
            ResultSet c = statement.executeQuery();
            while (c.next()) {
                int idColumn = c.findColumn(AbstractDb.HDAccountColumns.HD_ACCOUNT_ID);
                if (idColumn != -1) {
                    hdSeedIds.add(c.getInt(idColumn));
                }
            }
            c.close();
            statement.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return hdSeedIds;
    }

    @Override
    public boolean hdAccountIsXRandom(int seedId) {
        boolean result = false;
        String sql = "select is_xrandom from hd_account where hd_account_id=?";
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{Integer.toString(seedId)});
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int idColumn = rs.findColumn(AbstractDb.HDAccountColumns.IS_XRANDOM);
                if (idColumn != -1) {
                    result = rs.getBoolean(idColumn);
                }
            }
            rs.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void addPasswordSeed(Connection conn, PasswordSeed passwordSeed) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("insert into password_seed (password_seed)  values (?)");
        stmt.setString(1, passwordSeed.toPasswordSeedString());
        stmt.executeUpdate();
        stmt.close();
    }


    private HDMAddress applyHDMAddress(ResultSet c, HDMKeychain keychain) throws AddressFormatException, SQLException

    {
        HDMAddress hdmAddress;

        String address = null;
        boolean isSynced = false;

        int idColumn = c.findColumn(AbstractDb.HDMAddressesColumns.ADDRESS);
        if (idColumn != -1) {
            address = c.getString(idColumn);
        }
        idColumn = c.findColumn(AbstractDb.HDMAddressesColumns.IS_SYNCED);
        if (idColumn != -1) {
            isSynced = c.getInt(idColumn) == 1;
        }
        HDMAddress.Pubs pubs = applyPubs(c);
        hdmAddress = new HDMAddress(pubs, address, isSynced, keychain);
        return hdmAddress;

    }

    private HDMAddress.Pubs applyPubs(ResultSet c) throws AddressFormatException, SQLException {
        int hdSeedIndex = 0;
        byte[] hot = null;
        byte[] cold = null;
        byte[] remote = null;
        int idColumn = c.findColumn(AbstractDb.HDMAddressesColumns.HD_SEED_INDEX);
        if (idColumn != -1) {
            hdSeedIndex = c.getInt(idColumn);
        }
        idColumn = c.findColumn(AbstractDb.HDMAddressesColumns.PUB_KEY_HOT);
        if (idColumn != -1) {
            String str = c.getString(idColumn);
            if (!Utils.isEmpty(str)) {
                hot = Base58.decode(str);
            }
        }
        idColumn = c.findColumn(AbstractDb.HDMAddressesColumns.PUB_KEY_COLD);
        if (idColumn != -1) {
            String str = c.getString(idColumn);
            if (!Utils.isEmpty(str)) {
                cold = Base58.decode(str);
            }

        }
        idColumn = c.findColumn(AbstractDb.HDMAddressesColumns.PUB_KEY_REMOTE);
        if (idColumn != -1) {
            String str = c.getString(idColumn);
            if (!Utils.isEmpty(str)) {
                remote = Base58.decode(str);
            }

        }
        HDMAddress.Pubs pubs = new HDMAddress.Pubs(hot, cold, remote, hdSeedIndex);
        return pubs;

    }

    private Address applyAddressCursor(ResultSet c) throws AddressFormatException, SQLException {
        Address address;
        int idColumn = c.findColumn(AbstractDb.AddressesColumns.ADDRESS);
        String addressStr = null;
        String encryptPrivateKey = null;
        byte[] pubKey = null;
        boolean isXRandom = false;
        boolean isSynced = false;
        boolean isTrash = false;
        long sortTime = 0;

        if (idColumn != -1) {
            addressStr = c.getString(idColumn);
            if (!Utils.validBicoinAddress(addressStr)) {
                return null;
            }
        }
        idColumn = c.findColumn(AbstractDb.AddressesColumns.ENCRYPT_PRIVATE_KEY);
        if (idColumn != -1) {
            encryptPrivateKey = c.getString(idColumn);
        }
        idColumn = c.findColumn(AbstractDb.AddressesColumns.PUB_KEY);
        if (idColumn != -1) {
            pubKey = Base58.decode(c.getString(idColumn));
        }
        idColumn = c.findColumn(AbstractDb.AddressesColumns.IS_XRANDOM);
        if (idColumn != -1) {
            isXRandom = c.getInt(idColumn) == 1;
        }
        idColumn = c.findColumn(AbstractDb.AddressesColumns.IS_SYNCED);
        if (idColumn != -1) {
            isSynced = c.getInt(idColumn) == 1;
        }
        idColumn = c.findColumn(AbstractDb.AddressesColumns.IS_TRASH);
        if (idColumn != -1) {
            isTrash = c.getInt(idColumn) == 1;
        }
        idColumn = c.findColumn(AbstractDb.AddressesColumns.SORT_TIME);
        if (idColumn != -1) {
            sortTime = c.getLong(idColumn);
        }
        address = new Address(addressStr, pubKey, sortTime, isSynced, isXRandom, isTrash, encryptPrivateKey);

        return address;
    }

    private void applyHDMAddressContentValues(Connection conn, String address, int hdSeedId, int index, byte[] pubKeysHot,
                                              byte[] pubKeysCold, byte[] pubKeysRemote, boolean isSynced) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(insertHDMAddressSql);
        stmt.setString(1, Integer.toString(hdSeedId));
        stmt.setString(2, Integer.toString(index));
        stmt.setString(3, Base58.encode(pubKeysHot));
        stmt.setString(4, Base58.encode(pubKeysCold));
        stmt.setString(5, pubKeysRemote == null ? null : Base58.encode(pubKeysRemote));
        stmt.setString(6, Utils.isEmpty(address) ? null : address);
        stmt.setString(7, Integer.toString(isSynced ? 1 : 0));
        stmt.executeUpdate();
        stmt.close();
    }


}
