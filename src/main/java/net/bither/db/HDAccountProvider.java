package net.bither.db;

import net.bither.ApplicationInstanceManager;
import net.bither.bitherj.core.AbstractHD;
import net.bither.bitherj.core.In;
import net.bither.bitherj.crypto.PasswordSeed;
import net.bither.bitherj.db.AbstractDb;
import net.bither.bitherj.db.IHDAccountProvider;
import net.bither.bitherj.exception.AddressFormatException;
import net.bither.bitherj.utils.Base58;
import net.bither.bitherj.utils.Utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class HDAccountProvider implements IHDAccountProvider {

    private static HDAccountProvider addressProvider = new HDAccountProvider(ApplicationInstanceManager.addressDBHelper);

    public static HDAccountProvider getInstance() {
        return addressProvider;
    }

    private AddressDBHelper mDb;

    public HDAccountProvider(AddressDBHelper db) {
        this.mDb = db;
    }

    @Override
    public String getHDFirstAddress(int hdSeedId) {
        String address = null;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select hd_address from hd_account where hd_account_id=?", new String[]{Integer.toString(hdSeedId)});
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
    public int addHDAccount(String encryptedMnemonicSeed, String encryptSeed, String firstAddress
            , boolean isXrandom, String addressOfPS, byte[] externalPub
            , byte[] internalPub, AbstractHD.HDAccountType hdAccountType) {
        int hdAccountId = -1;
        try {
            this.mDb.getConn().setAutoCommit(false);
            String sql = "insert into hd_account(encrypt_seed,encrypt_mnemonic_seed,is_xrandom,hd_address,external_pub,internal_pub,hd_account_type) values(?,?,?,?,?,?,?);";
            PreparedStatement stmt = this.mDb.getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, encryptSeed);
            stmt.setString(2, encryptedMnemonicSeed);
            stmt.setInt(3, isXrandom ? 1 : 0);
            stmt.setString(4, firstAddress);
            stmt.setString(5, Base58.encode(externalPub));
            stmt.setString(6, Base58.encode(internalPub));
            stmt.setInt(7, hdAccountType.getValue());
            stmt.executeUpdate();
            if (!AddressProvider.hasPasswordSeed(this.mDb.getConn()) && !Utils.isEmpty(addressOfPS)) {
                AddressProvider.addPasswordSeed(this.mDb.getConn(), new PasswordSeed(addressOfPS, encryptedMnemonicSeed));
            }
            ResultSet tableKeys = stmt.getGeneratedKeys();
            tableKeys.next();
            hdAccountId = tableKeys.getInt(1);
            this.mDb.getConn().commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hdAccountId;
    }

    @Override
    public int addMonitoredHDAccount(String firstAddress, boolean isXrandom, byte[] externalPub, byte[] internalPub) {
        int hdAccountId = -1;
        try {
            this.mDb.getConn().setAutoCommit(false);
            String sql = "insert into hd_account(is_xrandom,hd_address,external_pub,internal_pub,hd_account_type) values(?,?,?,?,?);";
            PreparedStatement stmt = this.mDb.getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, isXrandom ? 1 : 0);
            stmt.setString(2, firstAddress);
            stmt.setString(3, Base58.encode(externalPub));
            stmt.setString(4, Base58.encode(internalPub));
            stmt.setInt(5, AbstractHD.HDAccountType.HD_MONITOR.getValue());
            stmt.executeUpdate();
            ResultSet tableKeys = stmt.getGeneratedKeys();
            tableKeys.next();
            hdAccountId = tableKeys.getInt(1);
            this.mDb.getConn().commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hdAccountId;
    }

//    @Override
//    public boolean hasHDAccountCold() {
//        boolean result = false;
//        SQLiteDatabase db = this.mDb.getReadableDatabase();
//        String sql = "select count(hd_address) cnt from hd_account where encrypt_seed is not " +
//                "null and encrypt_mnemonic_seed is not null";
//        Cursor cursor = db.rawQuery(sql, null);
//        if (cursor.moveToNext()) {
//            int idColumn = cursor.getColumnIndex("cnt");
//            if (idColumn != -1) {
//                result = cursor.getInt(idColumn) > 0;
//            }
//        }
//        cursor.close();
//        return result;
//    }

    @Override
    public boolean hasMnemonicSeed(int hdAccountId) {
        boolean result = false;
        String sql = "select count(0) cnt from hd_account where encrypt_mnemonic_seed is not null and hd_account_id=?";
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{Integer.toString(hdAccountId)});
            ResultSet cursor = statement.executeQuery();
            if (cursor.next()) {
                int idColumn = cursor.findColumn("cnt");
                if (idColumn != -1) {
                    result = cursor.getInt(idColumn) > 0;
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
    public byte[] getExternalPub(int hdSeedId) {
        byte[] pub = null;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select external_pub from hd_account where hd_account_id=? ", new String[]{Integer.toString(hdSeedId)});
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
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        }

        return pub;
    }

    @Override
    public byte[] getInternalPub(int hdSeedId) {
        byte[] pub = null;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select internal_pub from hd_account where hd_account_id=? ", new String[]{Integer.toString(hdSeedId)});
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
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        }


        return pub;
    }


    @Override
    public String getHDAccountEncryptSeed(int hdSeedId) {
        String hdAccountEncryptSeed = null;

        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select " + AbstractDb.HDAccountColumns.ENCRYPT_SEED + " from hd_account where hd_account_id=? ", new String[]{Integer.toString(hdSeedId)});
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
    public String getHDAccountEncryptMnemonicSeed(int hdSeedId) {
        String hdAccountMnmonicEncryptSeed = null;

        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select " + AbstractDb.HDAccountColumns.ENCRYPT_MNMONIC_SEED + " from hd_account where hd_account_id=? ", new String[]{Integer.toString(hdSeedId)});
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
    public boolean hdAccountIsXRandom(int seedId) {
        boolean result = false;

        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select is_xrandom from hd_account where hd_account_id=?", new String[]{Integer.toString(seedId)});
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                int idColumn = c.findColumn(AbstractDb.HDAccountColumns.ENCRYPT_MNMONIC_SEED);
                if (idColumn != -1) {
                    result = c.getInt(idColumn) == 1;
                }
            }
            c.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<Integer> getHDAccountSeeds() {
        List<Integer> hdSeedIds = new ArrayList<Integer>();
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select " + AbstractDb.HDAccountColumns.HD_ACCOUNT_ID + " from " + AbstractDb.Tables.HD_ACCOUNT, null);
            ResultSet c = statement.executeQuery();
            while (c.next()) {
                hdSeedIds.add(c.getInt(1));
            }
            c.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hdSeedIds;
    }

    @Override
    public boolean isPubExist(byte[] externalPub, byte[] internalPub) {
        return false;
    }
}
