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
import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.core.*;
import net.bither.bitherj.db.AbstractDb;
import net.bither.bitherj.db.IHDAccountProvider;
import net.bither.bitherj.exception.AddressFormatException;
import net.bither.bitherj.utils.Base58;
import net.bither.bitherj.utils.Sha256Hash;
import net.bither.bitherj.utils.Utils;
import net.bither.utils.StringUtil;
import net.bither.utils.SystemUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class HDAccountProvider implements IHDAccountProvider {

    private final static String queryTxHashOfHDAccount = " select  distinct txs.tx_hash from addresses_txs txs ,hd_account_addresses hd where txs.address=hd.address";
    private final static String inQueryTxHashOfHDAccount = " (" + queryTxHashOfHDAccount + ")";


    private static HDAccountProvider txProvider = new HDAccountProvider(ApplicationInstanceManager.txDBHelper);

    public static HDAccountProvider getInstance() {
        return txProvider;
    }

    private TxDBHelper mDb;

    public HDAccountProvider(TxDBHelper db) {
        this.mDb = db;
    }

    @Override
    public void addAddress(List<HDAccount.HDAccountAddress> hdAccountAddresses) {
        try {
            this.mDb.getConn().setAutoCommit(false);
            Connection conn = this.mDb.getConn();
            for (HDAccount.HDAccountAddress hdAccountAddress : hdAccountAddresses) {
                addAddress(conn, hdAccountAddress);
            }
            conn.commit();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    @Override
    public int issuedIndex(AbstractHD.PathType pathType) {
        int issuedIndex = -1;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select ifnull(max(address_index),-1) address_index from " + AbstractDb.Tables.HD_ACCOUNT_ADDRESS + " where path_type=? and is_issued=?  ",
                    new String[]{Integer.toString(pathType.getValue()), "1"});
            ResultSet cursor = statement.executeQuery();
            if (cursor.next()) {
                int idColumn = cursor.findColumn(AbstractDb.HDAccountAddressesColumns.ADDRESS_INDEX);
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
            PreparedStatement statement = this.mDb.getPreparedStatement("select ifnull(count(address),0) count from "
                            + AbstractDb.Tables.HD_ACCOUNT_ADDRESS + " where path_type=? ",
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
    public void updateIssuedIndex(AbstractHD.PathType pathType, int index) {
        String sql = "update hd_account_addresses set is_issued=? where path_type=? and address_index<=? ";
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
    public String externalAddress() {
        String address = null;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select address from " + AbstractDb.Tables.HD_ACCOUNT_ADDRESS
                            + " where path_type=? and is_issued=? order by address_index asc limit 1 ",
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
    public HashSet<String> getBelongAccountAddresses(List<String> addressList) {
        HashSet<String> addressSet = new HashSet<String>();

        List<String> temp = new ArrayList<String>();
        if (addressList != null) {
            for (String str : addressList) {
                temp.add(Utils.format("'%s'", str));
            }
        }
        try {
            String sql = Utils.format("select address from hd_account_addresses where address in (%s) "
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
    public HDAccount.HDAccountAddress addressForPath(AbstractHD.PathType type, int index) {
        HDAccount.HDAccountAddress accountAddress = null;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select address,pub,path_type,address_index,is_issued,is_synced from " +
                            AbstractDb.Tables.HD_ACCOUNT_ADDRESS + " where path_type=? and address_index=? ",
                    new String[]{Integer.toString(type.getValue()), Integer.toString(index)});
            ResultSet cursor = statement.executeQuery();

            if (cursor.next()) {
                accountAddress = formatAddress(cursor);
            }
            cursor.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accountAddress;
    }

    @Override
    public List<byte[]> getPubs(AbstractHD.PathType pathType) {
        List<byte[]> adressPubList = new ArrayList<byte[]>();
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select pub from hd_account_addresses where path_type=? ",
                    new String[]{Integer.toString(pathType.getValue())});
            ResultSet cursor = statement.executeQuery();
            while (cursor.next()) {
                try {
                    int idColumn = cursor.findColumn(AbstractDb.HDAccountAddressesColumns.PUB);
                    if (idColumn != -1) {
                        adressPubList.add(Base58.decode(cursor.getString(idColumn)));
                    }
                } catch (AddressFormatException e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return adressPubList;
    }

    @Override
    public List<HDAccount.HDAccountAddress> belongAccount(List<String> addresses) {
        List<HDAccount.HDAccountAddress> hdAccountAddressList = new ArrayList<HDAccount.HDAccountAddress>();
        List<String> temp = new ArrayList<String>();
        for (String str : addresses) {
            temp.add(Utils.format("'%s'", str));
        }
        String sql = "select address,pub,path_type,address_index,is_issued,is_synced from " + AbstractDb.Tables.HD_ACCOUNT_ADDRESS
                + " where address in (" + Utils.joinString(temp, ",") + ")";
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, null);
            ResultSet cursor = statement.executeQuery();
            while (cursor.next()) {
                hdAccountAddressList.add(formatAddress(cursor));

            }
            cursor.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hdAccountAddressList;
    }

    @Override
    public void updateSyncdComplete(HDAccount.HDAccountAddress address) {
        String sql = "update hd_account_addresses set is_synced=? where address=? ";
        Connection conn = this.mDb.getConn();
        try {
            String[] params = new String[]{
                    Integer.toString(address.isSyncedComplete() ? 1 : 0), address.getAddress()
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
    public void setSyncdNotComplete() {
        this.mDb.executeUpdate("update hd_account_addresses set is_synced=?", new String[]{"0"});
    }

    @Override
    public int unSyncedAddressCount() {
        int cnt = 0;
        try {
            String sql = "select count(address) cnt from hd_account_addresses where is_synced=? ";
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{"0"});
            ResultSet cursor = statement.executeQuery();
            if (cursor.next()) {
                int idColumn = cursor.findColumn("cnt");
                if (idColumn != -1) {
                    cnt = cursor.getInt(idColumn);
                }
            }
            cursor.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cnt;
    }

    @Override
    public void updateSyncdForIndex(AbstractHD.PathType pathType, int index) {
        this.mDb.executeUpdate("update hd_account_addresses set is_synced=? where path_type=? and address_index>? "
                , new String[]{"1", Integer.toString(pathType.getValue()), Integer.toString(index)});

    }

    @Override
    public List<HDAccount.HDAccountAddress> getSigningAddressesForInputs(List<In> inList) {

        List<HDAccount.HDAccountAddress> hdAccountAddressList =
                new ArrayList<HDAccount.HDAccountAddress>();
        ResultSet c;
        try {
            for (In in : inList) {
                String sql = "select a.*" +
                        " from hd_account_addresses a ,outs b" +
                        " where a.address=b.out_address" +
                        " and b.tx_hash=? and b.out_sn=?  ";
                OutPoint outPoint = in.getOutpoint();
                PreparedStatement statement = this.mDb.getPreparedStatement(sql,
                        new String[]{Base58.encode(in.getPrevTxHash()),
                                Integer.toString(outPoint.getOutSn())});
                c = statement.executeQuery();
                if (c.next()) {
                    hdAccountAddressList.add(formatAddress(c));
                }
                c.close();
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hdAccountAddressList;
    }

    @Override
    public int hdAccountTxCount() {
        int result = 0;
        try {
            String sql = "select count( distinct a.tx_hash) cnt from addresses_txs a ,hd_account_addresses b where a.address=b.address  ";
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, null);
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
    public long getHDAccountConfirmedBanlance(int hdAccountId) {
        long sum = 0;
        String unspendOutSql = "select ifnull(sum(a.out_value),0) sum from outs a,txs b where a.tx_hash=b.tx_hash " +
                "  and a.out_status=? and a.hd_account_id=? and b.block_no is not null";
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(unspendOutSql,
                    new String[]{Integer.toString(Out.OutStatus.unspent.getValue()), Integer.toString(hdAccountId)});
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
    public List<Tx> getHDAccountUnconfirmedTx() {
        List<Tx> txList = new ArrayList<Tx>();

        HashMap<Sha256Hash, Tx> txDict = new HashMap<Sha256Hash, Tx>();

        try {
            String sql = "select * from txs where tx_hash in" +
                    inQueryTxHashOfHDAccount +
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
                    inQueryTxHashOfHDAccount +
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
                    inQueryTxHashOfHDAccount +
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
    public long sentFromAccount(int hdAccountId, byte[] txHash) {
        String sql = "select  sum(o.out_value) out_value from ins i,outs o where" +
                " i.tx_hash=? and o.tx_hash=i.prev_tx_hash and i.prev_out_sn=o.out_sn and o.hd_account_id=?";
        long sum = 0;

        ResultSet cursor;

        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{Base58.encode(txHash),
                    Integer.toString(hdAccountId)});
            cursor = statement.executeQuery();
            if (cursor.next()) {
                int idColumn = cursor.findColumn(AbstractDb.OutsColumns.OUT_VALUE);
                if (idColumn != -1) {
                    sum = cursor.getLong(idColumn);
                }
            }
            cursor.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sum;
    }

    @Override
    public List<Tx> getTxAndDetailByHDAccount() {
        List<Tx> txItemList = new ArrayList<Tx>();

        HashMap<Sha256Hash, Tx> txDict = new HashMap<Sha256Hash, Tx>();

        try {
            String sql = "select * from txs where tx_hash in " +
                    inQueryTxHashOfHDAccount +
                    " order by" +
                    " ifnull(block_no,4294967295) desc ";
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, null);
            ResultSet c = statement.executeQuery();
            StringBuilder txsStrBuilder = new StringBuilder();
            while (c.next()) {
                Tx txItem = TxHelper.applyCursor(c);
                txItem.setIns(new ArrayList<In>());
                txItem.setOuts(new ArrayList<Out>());
                txItemList.add(txItem);
                txDict.put(new Sha256Hash(txItem.getTxHash()), txItem);
                txsStrBuilder.append("'").append(Base58.encode(txItem.getTxHash())).append("'").append(",");
            }
            c.close();
            statement.close();

            if (txsStrBuilder.length() > 1) {
                String txs = txsStrBuilder.substring(0, txsStrBuilder.length() - 1);
                sql = Utils.format("select b.* from ins b where b.tx_hash in (%s)" +
                        " order by b.tx_hash ,b.in_sn", txs);
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

                sql = Utils.format("select b.* from outs b where b.tx_hash in (%s)" +
                        " order by b.tx_hash,b.out_sn", txs);
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
            }
        } catch (AddressFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return txItemList;
    }

    @Override
    public List<Tx> getTxAndDetailByHDAccount(int page) {
        List<Tx> txItemList = new ArrayList<Tx>();

        HashMap<Sha256Hash, Tx> txDict = new HashMap<Sha256Hash, Tx>();


        try {
            String sql = "select * from txs where tx_hash in " +
                    inQueryTxHashOfHDAccount +
                    " order by" +
                    " ifnull(block_no,4294967295) desc limit ?,? ";
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{
                    Integer.toString((page - 1) * BitherjSettings.TX_PAGE_SIZE), Integer.toString(BitherjSettings.TX_PAGE_SIZE)
            });
            ResultSet c = statement.executeQuery();
            StringBuilder txsStrBuilder = new StringBuilder();
            while (c.next()) {
                Tx txItem = TxHelper.applyCursor(c);
                txItem.setIns(new ArrayList<In>());
                txItem.setOuts(new ArrayList<Out>());
                txItemList.add(txItem);
                txDict.put(new Sha256Hash(txItem.getTxHash()), txItem);
                txsStrBuilder.append("'").append(Base58.encode(txItem.getTxHash())).append("'").append(",");
            }
            c.close();
            statement.close();

            if (txsStrBuilder.length() > 1) {
                String txs = txsStrBuilder.substring(0, txsStrBuilder.length() - 1);
                sql = Utils.format("select b.* from ins b where b.tx_hash in (%s)" +
                        " order by b.tx_hash ,b.in_sn", txs);
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

                sql = Utils.format("select b.* from outs b where b.tx_hash in (%s)" +
                        " order by b.tx_hash,b.out_sn", txs);
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

            }
        } catch (AddressFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return txItemList;
    }

    @Override
    public List<Out> getUnspendOutByHDAccount(int hdAccountId) {
        List<Out> outItems = new ArrayList<Out>();
        String unspendOutSql = "select a.* from outs a,txs b where a.tx_hash=b.tx_hash " +
                " and a.out_status=? and a.hd_account_id=?";
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(unspendOutSql,
                    new String[]{Integer.toString(Out.OutStatus.unspent.getValue()), Integer.toString(hdAccountId)});
            ResultSet c = statement.executeQuery();
            while (c.next()) {
                outItems.add(TxHelper.applyCursorOut(c));
            }

            c.close();
            statement.close();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return outItems;
    }

    @Override
    public List<Tx> getRecentlyTxsByAccount(int greateThanBlockNo, int limit) {
        List<Tx> txItemList = new ArrayList<Tx>();

        String sql = "select * from txs  where  tx_hash in " +
                inQueryTxHashOfHDAccount +
                " and ((block_no is null) or (block_no is not null and block_no>?)) " +
                " order by ifnull(block_no,4294967295) desc, tx_time desc " +
                " limit ? ";
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql,
                    new String[]{Integer.toString(greateThanBlockNo), Integer.toString(limit)});
            ResultSet c = statement.executeQuery();
            while (c.next()) {
                Tx txItem = TxHelper.applyCursor(c);
                txItemList.add(txItem);
            }

            for (Tx item : txItemList) {
                TxHelper.addInsAndOuts(mDb, item);
            }
            c.close();
            statement.close();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return txItemList;
    }


    @Override
    public int getUnspendOutCountByHDAccountWithPath(int hdAccountId, AbstractHD.PathType pathType) {
        int result = 0;
        String sql = "select count(tx_hash) cnt from outs where out_address in " +
                "(select address from hd_account_addresses where path_type =? and out_status=?) " +
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
                "(select address from hd_account_addresses where path_type =? and out_status=?) " +
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

    private HDAccount.HDAccountAddress formatAddress(ResultSet c) throws SQLException {
        String address = null;
        byte[] pubs = null;
        AbstractHD.PathType ternalRootType = AbstractHD.PathType.EXTERNAL_ROOT_PATH;
        int index = 0;
        boolean isIssued = false;
        boolean isSynced = true;
        HDAccount.HDAccountAddress hdAccountAddress = null;
        try {
            int idColumn = c.findColumn(AbstractDb.HDAccountAddressesColumns.ADDRESS);
            if (idColumn != -1) {
                address = c.getString(idColumn);
            }
            idColumn = c.findColumn(AbstractDb.HDAccountAddressesColumns.PUB);
            if (idColumn != -1) {
                pubs = Base58.decode(c.getString(idColumn));
            }
            idColumn = c.findColumn(AbstractDb.HDAccountAddressesColumns.PATH_TYPE);
            if (idColumn != -1) {
                ternalRootType = AbstractHD.getTernalRootType(c.getInt(idColumn));

            }
            idColumn = c.findColumn(AbstractDb.HDAccountAddressesColumns.ADDRESS_INDEX);
            if (idColumn != -1) {
                index = c.getInt(idColumn);
            }
            idColumn = c.findColumn(AbstractDb.HDAccountAddressesColumns.IS_ISSUED);
            if (idColumn != -1) {
                isIssued = c.getInt(idColumn) == 1;
            }
            idColumn = c.findColumn(AbstractDb.HDAccountAddressesColumns.IS_SYNCED);
            if (idColumn != -1) {
                isSynced = c.getInt(idColumn) == 1;
            }
            hdAccountAddress = new HDAccount.HDAccountAddress(address, pubs,
                    ternalRootType, index, isIssued, isSynced);
        } catch (AddressFormatException e) {
            e.printStackTrace();
        }
        return hdAccountAddress;
    }

    private void addAddress(Connection conn, HDAccount.HDAccountAddress accountAddress) throws SQLException {
        String sql = "insert into hd_account_addresses(path_type,address_index" +
                ",is_issued,address,pub,is_synced) " +
                " values(?,?,?,?,?,?)";

        String[] params = new String[]{Integer.toString(accountAddress.getPathType().getValue())
                , Integer.toString(accountAddress.getIndex())
                , Integer.toString(accountAddress.isIssued() ? 1 : 0)
                , accountAddress.getAddress()
                , Base58.encode(accountAddress.getPub())
                , Integer.toString(accountAddress.isSyncedComplete() ? 1 : 0)
        };
        PreparedStatement stmt = conn.prepareStatement(sql);
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                stmt.setString(i + 1, params[i]);
            }
        }
        stmt.executeUpdate();
        stmt.close();
    }


}
