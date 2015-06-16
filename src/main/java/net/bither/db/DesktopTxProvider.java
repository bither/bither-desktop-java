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
import net.bither.bitherj.core.*;
import net.bither.bitherj.db.AbstractDb;
import net.bither.bitherj.db.IDesktopTxProvider;
import net.bither.bitherj.exception.AddressFormatException;
import net.bither.bitherj.utils.Base58;
import net.bither.bitherj.utils.Sha256Hash;
import net.bither.bitherj.utils.Utils;
import net.bither.utils.SystemUtil;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class DesktopTxProvider implements IDesktopTxProvider {

    private final static String queryTxHashOfHDAccount = " select  distinct txs.tx_hash from addresses_txs txs ,desktop_hdm_account_addresses hd where txs.address=hd.address";
    private final static String inQueryTxHashOfDesktopHDMKeychain = " (" + queryTxHashOfHDAccount + ")";

    private static DesktopTxProvider enDesktopTxProvider =
            new DesktopTxProvider(ApplicationInstanceManager.txDBHelper);

    private TxDBHelper mDb;

    public static DesktopTxProvider getInstance() {
        return enDesktopTxProvider;
    }

    private DesktopTxProvider(TxDBHelper db) {
        this.mDb = db;
    }


    private static final String insert_hdm_address_sql = "insert into desktop_hdm_account_addresses " +
            "(path_type,address_index,is_issued,address,pub_key_1,pub_key_2,pub_key_3,is_synced)" +
            " values (?,?,?,?,?,?,?,?) ";

    @Override
    public void addAddress(List<DesktopHDMAddress> addressList) {
        try {
            this.mDb.getConn().setAutoCommit(false);
            for (DesktopHDMAddress address : addressList) {
                PreparedStatement stmt = this.mDb.getConn().prepareStatement(insert_hdm_address_sql);
                String[] params = new String[]{Integer.toString(address.getPathType().getValue()),
                        Integer.toString(address.getIndex()), Integer.toString(address.isIssued() ? 1 : 0),
                        address.getAddress(), Base58.encode(address.getPubHot()), Base58.encode(address.getPubRemote())
                        , Base58.encode(address.getPubCold()), Integer.toString(address.isSyncComplete() ? 1 : 0)
                };
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        stmt.setString(i + 1, params[i]);
                    }
                }
                stmt.executeUpdate();
                stmt.close();
            }
            this.mDb.getConn().commit();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int maxHDMAddressPubIndex() {
        int maxIndex = -1;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select ifnull(max(address_index),-1)  address_index from desktop_hdm_account_addresses ", null);
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
    public String externalAddress() {
        String address = null;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select address from desktop_hdm_account_addresses where path_type=? and is_issued=? order by address_index asc limit 1 ",
                    new String[]{Integer.toString(AbstractHD.PathType.EXTERNAL_ROOT_PATH.getValue()), "0"});
            ResultSet cursor = statement.executeQuery();
            if (cursor.next()) {
                int idColumn = cursor.findColumn(AbstractDb.HDAccountAddressesColumns.ADDRESS);
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
    public boolean hasAddress() {
        boolean hasAddress = false;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select count(address) cnt from desktop_hdm_account_addresses  ",
                    null);
            ResultSet cursor = statement.executeQuery();
            if (cursor.next()) {
                int idColumn = cursor.findColumn("cnt");
                if (idColumn != -1) {
                    hasAddress = cursor.getInt(idColumn) > 0;
                }
            }
            cursor.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hasAddress;
    }

    @Override
    public long getHDAccountConfirmedBanlance(int hdSeedId) {
        long sum = 0;
        String unspendOutSql = "select ifnull(sum(a.out_value),0) sum from outs a,txs b where a.tx_hash=b.tx_hash " +
                "  and a.out_status=? and a.enterprise_hd_account_id=? and b.block_no is not null";
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(unspendOutSql,
                    new String[]{Integer.toString(Out.OutStatus.unspent.getValue()), Integer.toString(hdSeedId)});
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                int idColumn = c.findColumn("sum");
                if (idColumn != -1) {
                    sum = c.getLong(idColumn);
                }
            }
            c.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sum;
    }

    @Override
    public HashSet<String> getBelongAccountAddresses(List<String> addressList) {
        HashSet<String> addressSet = new HashSet<String>();

        List<String> temp = new ArrayList<String>();
        if (addressList != null) {
            for (String str : addressList) {
                temp.add(Utils.format("'%s'", str));
            }
        }
        try {
            String sql = Utils.format("select address from desktop_hdm_account_addresses where address in (%s) "
                    , Utils.joinString(temp, ","));
            PreparedStatement statement = this.mDb.getPreparedStatement(sql,
                    null);
            ResultSet cursor = statement.executeQuery();
            while (cursor.next()) {
                int idColumn = cursor.findColumn(AbstractDb.HDAccountAddressesColumns.ADDRESS);
                if (idColumn != -1) {
                    addressSet.add(cursor.getString(idColumn));
                }
            }
            cursor.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        SystemUtil.maxUsedSize();
        return addressSet;
    }

    @Override
    public void updateIssuedIndex(AbstractHD.PathType pathType, int index) {
        String sql = "update desktop_hdm_account_addresses set is_issued=? where path_type=? and address_index<=? ";
        Connection conn = this.mDb.getConn();
        try {
            String[] params = new String[]{
                    "1", Integer.toString(pathType.getValue()), Integer.toString(index)
            };
            PreparedStatement stmt = conn.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setString(i + 1, params[i]);
                }
            }
            stmt.executeUpdate();
            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int issuedIndex(AbstractHD.PathType pathType) {

        int issuedIndex = -1;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select ifnull(max(address_index),-1) address_index from " +
                            "desktop_hdm_account_addresses where path_type=? and is_issued=?  ",
                    new String[]{Integer.toString(pathType.getValue()), "1"});
            ResultSet cursor = statement.executeQuery();
            if (cursor.next()) {
                int idColumn = cursor.findColumn("address_index");
                if (idColumn != -1) {
                    issuedIndex = cursor.getInt(idColumn);
                }
            }
            cursor.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return issuedIndex;
    }

    @Override
    public int allGeneratedAddressCount(AbstractHD.PathType pathType) {
        int count = 0;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select ifnull(count(address),0) count from " +
                            "desktop_hdm_account_addresses  where path_type=? ",
                    new String[]{Integer.toString(pathType.getValue())});
            ResultSet cursor = statement.executeQuery();
            if (cursor.next()) {
                int idColumn = cursor.findColumn("count");
                if (idColumn != -1) {
                    count = cursor.getInt(idColumn);
                }
            }
            cursor.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    @Override
    public List<Tx> getHDAccountUnconfirmedTx() {
        List<Tx> txList = new ArrayList<Tx>();
        HashMap<Sha256Hash, Tx> txDict = new HashMap<Sha256Hash, Tx>();
        try {
            String sql = "select * from txs where tx_hash in" +
                    inQueryTxHashOfDesktopHDMKeychain +
                    " and  block_no is null " +
                    " order by block_no desc";
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, null);
            ResultSet c = statement.executeQuery();
            while (c.next()) {
                Tx txItem = TxHelper.applyCursor(c);
                txItem.setIns(new ArrayList<In>());
                txItem.setOuts(new ArrayList<Out>());
                txList.add(txItem);
                txDict.put(new Sha256Hash(txItem.getTxHash()), txItem);
            }
            c.close();
            statement.close();
            sql = "select b.* " +
                    " from ins b, txs c " +
                    " where c.tx_hash in " +
                    inQueryTxHashOfDesktopHDMKeychain +
                    " and b.tx_hash=c.tx_hash and c.block_no is null  " +
                    " order by b.tx_hash ,b.in_sn";
            statement = this.mDb.getPreparedStatement(sql, null);
            c = statement.executeQuery();
            while (c.next()) {
                In inItem = TxHelper.applyCursorIn(c);
                Tx tx = txDict.get(new Sha256Hash(inItem.getTxHash()));
                if (tx != null) {
                    tx.getIns().add(inItem);
                }
            }
            c.close();
            statement.close();

            sql = "select b.* " +
                    " from  outs b, txs c " +
                    " where c.tx_hash in" +
                    inQueryTxHashOfDesktopHDMKeychain +
                    " and b.tx_hash=c.tx_hash and c.block_no is null  " +
                    " order by b.tx_hash,b.out_sn";
            statement = this.mDb.getPreparedStatement(sql, null);
            c = statement.executeQuery();
            while (c.next()) {
                Out out = TxHelper.applyCursorOut(c);
                Tx tx = txDict.get(new Sha256Hash(out.getTxHash()));
                if (tx != null) {
                    tx.getOuts().add(out);
                }
            }
            c.close();
            statement.close();

        } catch (AddressFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return txList;
    }

    @Override
    public List<HDMAddress.Pubs> getPubs(AbstractHD.PathType pathType) {
        List<HDMAddress.Pubs> pubList = new ArrayList<HDMAddress.Pubs>();
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select address_index,address,pub_key_1,pub_key_2,pub_key_3 " +
                            "from desktop_hdm_account_addresses where path_type=? ",
                    new String[]{Integer.toString(pathType.getValue())});
            ResultSet cursor = statement.executeQuery();
            while (cursor.next()) {
                try {
                    HDMAddress.Pubs pubs = new HDMAddress.Pubs();
                    int idColumn = cursor.findColumn("pub_key_1");
                    if (idColumn != -1) {
                        pubs.hot = Base58.decode(cursor.getString(idColumn));
                    }
                    idColumn = cursor.findColumn("pub_key_2");
                    if (idColumn != -1) {
                        pubs.remote = Base58.decode(cursor.getString(idColumn));
                    }
                    idColumn = cursor.findColumn("pub_key_3");
                    if (idColumn != -1) {
                        pubs.cold = Base58.decode(cursor.getString(idColumn));
                    }
                    idColumn = cursor.findColumn("address_index");
                    if (idColumn != -1) {
                        pubs.index = cursor.getInt(idColumn);
                    }
                    pubList.add(pubs);
                } catch (AddressFormatException e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pubList;
    }

    @Override
    public int getUnspendOutCountByHDAccountWithPath(int hdAccountId, AbstractHD.PathType pathType) {
        int result = 0;
        String sql = "select count(tx_hash) cnt from outs where out_address in " +
                "(select address from desktop_hdm_account_addresses where path_type =? and out_status=?) " +
                "and hd_account_id=?";
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{Integer.toString(pathType.getValue())
                    , Integer.toString(Out.OutStatus.unspent.getValue())
                    , Integer.toString(hdAccountId)
            });
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                int idColumn = c.findColumn("cnt");
                if (idColumn != -1) {
                    result = c.getInt(idColumn);
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
    public List<Out> getUnspendOutByHDAccountWithPath(int hdAccountId, AbstractHD.PathType pathType) {
        List<Out> outList = new ArrayList<Out>();

        String sql = "select * from outs where out_address in " +
                "(select address from desktop_hdm_account_addresses where path_type =? and out_status=?) " +
                "and hd_account_id=?";
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{Integer.toString(pathType.getValue())
                    , Integer.toString(Out.OutStatus.unspent.getValue())
                    , Integer.toString(hdAccountId)
            });
            ResultSet c = statement.executeQuery();
            while (c.next()) {
                outList.add(TxHelper.applyCursorOut(c));
            }
            c.close();
            statement.close();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return outList;
    }


}
