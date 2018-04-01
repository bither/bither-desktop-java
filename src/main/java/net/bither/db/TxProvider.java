/*
* Copyright 2014 http://Bither.net
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package net.bither.db;

import net.bither.ApplicationInstanceManager;
import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.In;
import net.bither.bitherj.core.Out;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.db.AbstractDb;
import net.bither.bitherj.db.ITxProvider;
import net.bither.bitherj.exception.AddressFormatException;
import net.bither.bitherj.utils.Base58;
import net.bither.bitherj.utils.Sha256Hash;
import net.bither.bitherj.utils.Utils;
import net.bither.utils.LogUtil;
import net.bither.utils.StringUtil;
import net.bither.utils.SystemUtil;

import java.sql.*;
import java.util.*;

public class TxProvider implements ITxProvider {

    String txInsertSql = "insert into txs " +
            "(tx_hash,tx_ver,tx_locktime,tx_time,block_no,source)" +
            " values (?,?,?,?,?,?) ";
    String inInsertSql = "insert into ins " +
            "(tx_hash,in_sn,prev_tx_hash,prev_out_sn,in_signature,in_sequence)" +
            " values (?,?,?,?,?,?) ";

    String outInsertSql = "insert into outs " +
            "(tx_hash,out_sn,out_script,out_value,out_status,out_address,hd_account_id)" +
            " values (?,?,?,?,?,?,?) ";

    private static TxProvider txProvider = new TxProvider(ApplicationInstanceManager.txDBHelper);

    public static TxProvider getInstance() {
        return txProvider;
    }

    private TxDBHelper mDb;

    public TxProvider(TxDBHelper db) {
        this.mDb = db;
    }


    public List<Tx> getTxAndDetailByAddress(String address) {
        List<Tx> txItemList = new ArrayList<Tx>();
        HashMap<Sha256Hash, Tx> txDict = new HashMap<Sha256Hash, Tx>();

        try {
            String sql = "select b.* from addresses_txs a, txs b where a.tx_hash=b.tx_hash and a.address=? order by b.block_no ";
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{address});
            ResultSet c = statement.executeQuery();
            while (c.next()) {
                Tx txItem = TxHelper.applyCursor(c);
                txItem.setIns(new ArrayList<In>());
                txItem.setOuts(new ArrayList<Out>());
                txItemList.add(txItem);
                txDict.put(new Sha256Hash(txItem.getTxHash()), txItem);
            }
            c.close();
            statement.close();

            sql = "select b.* from addresses_txs a, ins b where a.tx_hash=b.tx_hash and a.address=? order by b.tx_hash ,b.in_sn";
            statement = this.mDb.getPreparedStatement(sql, new String[]{address});
            c = statement.executeQuery();
            while (c.next()) {
                In inItem = TxHelper.applyCursorIn(c);
                Tx tx = txDict.get(new Sha256Hash(inItem.getTxHash()));
                if (tx != null)
                    tx.getIns().add(inItem);
            }
            c.close();
            statement.close();

            sql = "select b.* from addresses_txs a, outs b where a.tx_hash=b.tx_hash and a.address=? order by b.tx_hash,b.out_sn";
            statement = this.mDb.getPreparedStatement(sql, new String[]{address});
            c = statement.executeQuery();
            while (c.next()) {
                Out out = TxHelper.applyCursorOut(c);
                Tx tx = txDict.get(new Sha256Hash(out.getTxHash()));
                if (tx != null)
                    tx.getOuts().add(out);
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
    public List<Tx> getTxAndDetailByAddress(String address, int page) {
        List<Tx> txItemList = new ArrayList<Tx>();
        HashMap<Sha256Hash, Tx> txDict = new HashMap<Sha256Hash, Tx>();
        try {
            String sql = "select b.* from addresses_txs a, txs b" +
                    " where a.tx_hash=b.tx_hash and a.address=? order by ifnull(b.block_no,4294967295) desc limit ?,? ";
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{
                    address, Integer.toString((page - 1) * BitherjSettings.TX_PAGE_SIZE), Integer.toString(BitherjSettings.TX_PAGE_SIZE)
            });
            ResultSet c = statement.executeQuery();
            while (c.next()) {
                Tx txItem = TxHelper.applyCursor(c);
                txItem.setIns(new ArrayList<In>());
                txItem.setOuts(new ArrayList<Out>());
                txItemList.add(txItem);
                txDict.put(new Sha256Hash(txItem.getTxHash()), txItem);
            }
            c.close();
            statement.close();
            addInForTxDetail(address, txDict);
            addOutForTxDetail(address, txDict);

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        }
        return txItemList;
    }

    private void addInForTxDetail(String address, HashMap<Sha256Hash, Tx> txDict) throws AddressFormatException, SQLException {
        String sql = "select b.* from addresses_txs a, ins b where a.tx_hash=b.tx_hash and a.address=? "
                + "order by b.tx_hash ,b.in_sn";
        PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{address});
        ResultSet c = statement.executeQuery();
        while (c.next()) {
            In inItem = TxHelper.applyCursorIn(c);
            Tx tx = txDict.get(new Sha256Hash(inItem.getTxHash()));
            if (tx != null) {
                tx.getIns().add(inItem);
            }
        }
        c.close();
        statement.close();
    }

    private void addOutForTxDetail(String address, HashMap<Sha256Hash, Tx> txDict) throws AddressFormatException, SQLException {
        String sql = "select b.* from addresses_txs a, outs b where a.tx_hash=b.tx_hash and a.address=? "
                + "order by b.tx_hash,b.out_sn";
        PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{address});
        ResultSet c = statement.executeQuery();
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


    public List<Tx> getPublishedTxs() {
        List<Tx> txItemList = new ArrayList<Tx>();
        HashMap<Sha256Hash, Tx> txDict = new HashMap<Sha256Hash, Tx>();

        String sql = "select * from txs where block_no is null or block_no =?";
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{Integer.toString(Tx.TX_UNCONFIRMED)});
            ResultSet c = statement.executeQuery();
            while (c.next()) {
                Tx txItem = TxHelper.applyCursor(c);
                txItem.setIns(new ArrayList<In>());
                txItem.setOuts(new ArrayList<Out>());
                txItemList.add(txItem);
                txDict.put(new Sha256Hash(txItem.getTxHash()), txItem);
            }
            c.close();
            statement.close();

            sql = "select b.* from txs a, ins b  where a.tx_hash=b.tx_hash  and ( a.block_no is null or a.block_no =?) order by b.tx_hash ,b.in_sn";
            statement = this.mDb.getPreparedStatement(sql, new String[]{Integer.toString(Tx.TX_UNCONFIRMED)});
            c = statement.executeQuery();
            while (c.next()) {
                In inItem = TxHelper.applyCursorIn(c);
                Tx tx = txDict.get(new Sha256Hash(inItem.getTxHash()));
                tx.getIns().add(inItem);
            }
            c.close();
            statement.close();

            sql = "select b.* from txs a, outs b where a.tx_hash=b.tx_hash and ( a.block_no is null or a.block_no = ? )order by b.tx_hash,b.out_sn";
            statement = this.mDb.getPreparedStatement(sql, new String[]{Integer.toString(Tx.TX_UNCONFIRMED)});
            c = statement.executeQuery();
            while (c.next()) {
                Out out = TxHelper.applyCursorOut(c);
                Tx tx = txDict.get(new Sha256Hash(out.getTxHash()));
                tx.getOuts().add(out);
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

    public List<In> getRelatedIn(String address) {
        List<In> list = new ArrayList<In>();
        String sql = "select ins.* from ins,addresses_txs " +
                "where ins.tx_hash=addresses_txs.tx_hash and addresses_txs.address=?";
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{address});
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                list.add(TxHelper.applyCursorIn(rs));
            }
            rs.close();
            statement.close();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Tx getTxDetailByTxHash(byte[] txHash) {
        Tx txItem = null;
        String txHashStr = Base58.encode(txHash);

        String sql = "select * from txs where tx_hash=?";

        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{txHashStr});
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                txItem = TxHelper.applyCursor(c);
            }

            if (txItem != null) {
                TxHelper.addInsAndOuts(this.mDb, txItem);

            }
            c.close();
            statement.close();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return txItem;
    }

    @Override
    public long sentFromAddress(byte[] txHash, String address) {
        String sql = "select  sum(o.out_value) out_value from ins i,outs o where" +
                " i.tx_hash=? and o.tx_hash=i.prev_tx_hash and i.prev_out_sn=o.out_sn and o.out_address=?";
        long sum = 0;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{Base58.encode(txHash),
                    address});
            ResultSet cursor = statement.executeQuery();
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


    public boolean isExist(byte[] txHash) {
        boolean result = false;
        try {
            String sql = "select count(0) cnt from txs where tx_hash=?";
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{Base58.encode(txHash)});
            ResultSet c = statement.executeQuery();

            if (c.next()) {
                int columnIndex = c.findColumn("cnt");
                if (columnIndex != -1) {
                    result = c.getInt(columnIndex) > 0;
                }
            }
            c.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void add(final Tx txItem) {
        try {
            this.mDb.getConn().setAutoCommit(false);
            addTxToDb(this.mDb.getConn(), txItem);
            this.mDb.getConn().commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addTxs(List<Tx> txItems) {
        try {
            Connection connection = this.mDb.getConn();
            connection.setAutoCommit(false);
            for (Tx txItem : txItems) {
                addTxToDb(connection, txItem);
            }
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addTxToDb(Connection conn, Tx txItem) throws SQLException {
        HashSet<String> addressSet = AbstractDb.hdAccountProvider.
                getBelongAccountAddresses(txItem.getOutAddressList());
        for (Out out : txItem.getOuts()) {
            if (addressSet.contains(out.getOutAddress())) {
                out.setHDAccountId(AddressManager.getInstance().getHdAccount().getHdSeedId());
            }
        }
        insertTx(conn, txItem);
        List<AddressTx> addressesTxsRels = new ArrayList<AddressTx>();
        List<AddressTx> temp = insertIn(conn, txItem);
        if (temp != null && temp.size() > 0) {
            addressesTxsRels.addAll(temp);
        }
        temp = insertOut(conn, txItem);
        if (temp != null && temp.size() > 0) {
            addressesTxsRels.addAll(temp);
        }
        PreparedStatement statement;
        for (AddressTx addressTx : addressesTxsRels) {
            String sql = "insert or ignore into addresses_txs(address, tx_hash) values(?,?)";
            statement = conn.prepareStatement(sql);
            statement.setString(1, addressTx.getAddress());
            statement.setString(2, addressTx.getTxHash());
            statement.executeUpdate();
            statement.close();

        }

    }

    private void insertTx(Connection conn, Tx txItem) throws SQLException {
        String existSql = "select count(0) cnt from txs where tx_hash=?";
        PreparedStatement preparedStatement = conn.prepareStatement(existSql);
        preparedStatement.setString(1, Base58.encode(txItem.getTxHash()));
        ResultSet c = preparedStatement.executeQuery();
        int cnt = 0;
        if (c.next()) {
            int idColumn = c.findColumn("cnt");
            if (idColumn != -1) {
                cnt = c.getInt(idColumn);
            }
        }
        c.close();
        preparedStatement.close();
        if (cnt == 0) {
            String blockNoString = null;
            if (txItem.getBlockNo() != Tx.TX_UNCONFIRMED) {
                blockNoString = Integer.toString(txItem.getBlockNo());
            }
            preparedStatement = conn.prepareStatement(txInsertSql);
            preparedStatement.setString(1, Base58.encode(txItem.getTxHash()));
            preparedStatement.setLong(2, txItem.getTxVer());
            preparedStatement.setLong(3, txItem.getTxLockTime());
            preparedStatement.setLong(4, txItem.getTxTime());
            preparedStatement.setString(5, blockNoString);
            preparedStatement.setInt(6, txItem.getSource());
            preparedStatement.executeUpdate();
            preparedStatement.close();
        }

    }

    private List<AddressTx> insertOut(Connection conn, Tx txItem) throws SQLException {
        ResultSet c;
        String sql;

        PreparedStatement preparedStatement;
        List<AddressTx> addressTxes = new ArrayList<AddressTx>();
        for (Out outItem : txItem.getOuts()) {
            String existSql = "select count(0) cnt from outs where tx_hash=? and out_sn=?";
            preparedStatement = conn.prepareStatement(existSql);
            preparedStatement.setString(1, Base58.encode(outItem.getTxHash()));
            preparedStatement.setString(2, Integer.toString(outItem.getOutSn()));
            c = preparedStatement.executeQuery();
            int cnt = 0;
            if (c.next()) {
                int idColumn = c.findColumn("cnt");
                if (idColumn != -1) {
                    cnt = c.getInt(idColumn);
                }
            }
            c.close();
            preparedStatement.close();
            if (cnt == 0) {
                String outAddress = null;
                if (!Utils.isEmpty(outItem.getOutAddress())) {
                    outAddress = outItem.getOutAddress();
                }
                preparedStatement = conn.prepareStatement(outInsertSql);
                preparedStatement.setString(1, Base58.encode(outItem.getTxHash()));
                preparedStatement.setInt(2, outItem.getOutSn());
                preparedStatement.setString(3, Base58.encode(outItem.getOutScript()));
                preparedStatement.setLong(4, outItem.getOutValue());
                preparedStatement.setInt(5, outItem.getOutStatus().getValue());
                preparedStatement.setString(6, outAddress);
                preparedStatement.setInt(7, outItem.getHDAccountId());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } else {
                if (outItem.getHDAccountId() > -1) {
                    preparedStatement = conn.prepareStatement("update outs set hd_account_id=? where tx_hash=? and out_sn=?");
                    preparedStatement.setString(1, Integer.toString(outItem.getHDAccountId()));
                    preparedStatement.setString(2, Base58.encode(txItem.getTxHash()));
                    preparedStatement.setString(3, Integer.toString(outItem.getOutSn()));
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                }
            }
            if (!Utils.isEmpty(outItem.getOutAddress())) {
                addressTxes.add(new AddressTx(outItem.getOutAddress(), Base58.encode(txItem.getTxHash())));
            }
            sql = "select tx_hash from ins where prev_tx_hash=? and prev_out_sn=?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, Base58.encode(txItem.getTxHash()));
            preparedStatement.setString(2, Integer.toString(outItem.getOutSn()));
            c = preparedStatement.executeQuery();

            boolean isSpentByExistTx = false;
            if (c.next()) {
                int idColumn = c.findColumn("tx_hash");
                if (idColumn != -1) {
                    addressTxes.add(new AddressTx(outItem.getOutAddress(), c.getString(idColumn)));
                }
                isSpentByExistTx = true;
            }
            c.close();
            preparedStatement.close();
            if (isSpentByExistTx) {
                sql = "update outs set out_status=? where tx_hash=? and out_sn=?";
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, Integer.toString(Out.OutStatus.spent.getValue()));
                preparedStatement.setString(2, Base58.encode(txItem.getTxHash()));
                preparedStatement.setString(3, Integer.toString(outItem.getOutSn()));
                preparedStatement.executeUpdate();
                preparedStatement.close();

            }

        }
        return addressTxes;
    }

    private List<AddressTx> insertIn(Connection conn, Tx txItem) throws SQLException {
        ResultSet c;
        String sql;
        PreparedStatement preparedStatement;
        List<AddressTx> addressTxes = new ArrayList<AddressTx>();
        for (In inItem : txItem.getIns()) {
            String existSql = "select count(0) cnt from ins where tx_hash=? and in_sn=?";
            preparedStatement = conn.prepareStatement(existSql);
            preparedStatement.setString(1, Base58.encode(inItem.getTxHash()));
            preparedStatement.setString(2, Integer.toString(inItem.getInSn()));
            c = preparedStatement.executeQuery();
            int cnt = 0;
            if (c.next()) {
                int idColumn = c.findColumn("cnt");
                if (idColumn != -1) {
                    cnt = c.getInt(idColumn);
                }
            }
            c.close();
            preparedStatement.close();

            if (cnt == 0) {
                String signatureString = null;
                if (inItem.getInSignature() != null) {
                    signatureString = Base58.encode(inItem.getInSignature());
                }
                preparedStatement = conn.prepareStatement(inInsertSql);
                preparedStatement.setString(1, Base58.encode(inItem.getTxHash()));
                preparedStatement.setInt(2, inItem.getInSn());
                preparedStatement.setString(3, Base58.encode(inItem.getPrevTxHash()));
                preparedStatement.setInt(4, inItem.getPrevOutSn());
                preparedStatement.setString(5, signatureString);
                preparedStatement.setLong(6, inItem.getInSequence());
                preparedStatement.executeUpdate();
                preparedStatement.close();


            }

            sql = "select out_address from outs where tx_hash=? and out_sn=?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, Base58.encode(inItem.getPrevTxHash()));
            preparedStatement.setString(2, Integer.toString(inItem.getPrevOutSn()));
            c = preparedStatement.executeQuery();
            while (c.next()) {
                int idColumn = c.findColumn("out_address");
                if (idColumn != -1) {
                    addressTxes.add(new AddressTx(c.getString(idColumn), Base58.encode(txItem.getTxHash())));
                }
            }
            c.close();
            preparedStatement.close();

            sql = "update outs set out_status=? where tx_hash=? and out_sn=?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, Integer.toString(Out.OutStatus.spent.getValue()));
            preparedStatement.setString(2, Base58.encode(inItem.getPrevTxHash()));
            preparedStatement.setString(3, Integer.toString(inItem.getPrevOutSn()));
            preparedStatement.executeUpdate();
            preparedStatement.close();

        }
        return addressTxes;

    }


    public void remove(byte[] txHash) {
        String txHashStr = Base58.encode(txHash);
        List<String> txHashes = new ArrayList<String>();
        final List<String> needRemoveTxHashes = new ArrayList<String>();
        txHashes.add(txHashStr);
        while (txHashes.size() > 0) {
            String thisHash = txHashes.get(0);
            txHashes.remove(0);
            needRemoveTxHashes.add(thisHash);
            List<String> temp = getRelayTx(thisHash);
            txHashes.addAll(temp);
        }
        try {
            this.mDb.getConn().setAutoCommit(false);
            for (String str : needRemoveTxHashes) {
                removeSingleTx(this.mDb.getConn(), str);
            }
            this.mDb.getConn().commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void removeSingleTx(Connection conn, String tx) throws SQLException {
        Statement stmt = conn.createStatement();
        String deleteTx = "delete from txs where tx_hash='" + tx + "'";
        String deleteIn = "delete from ins where tx_hash='" + tx + "'";
        String deleteOut = "delete from outs where tx_hash='" + tx + "'";
        String deleteAddressesTx = "delete from addresses_txs where tx_hash='" + tx + "'";
        String inSql = "select prev_tx_hash,prev_out_sn from ins where tx_hash='" + tx + "'";
        String existOtherIn = "select count(0) cnt from ins where prev_tx_hash=? and prev_out_sn=?";
        String updatePrevOut = "update outs set out_status=? where tx_hash=? and out_sn=?";
        ResultSet c = stmt.executeQuery(inSql);
        List<Object[]> needUpdateOuts = new ArrayList<Object[]>();
        while (c.next()) {
            int idColumn = c.findColumn(AbstractDb.InsColumns.PREV_TX_HASH);
            String prevTxHash = null;
            int prevOutSn = 0;
            if (idColumn != -1) {
                prevTxHash = c.getString(idColumn);
            }
            idColumn = c.findColumn(AbstractDb.InsColumns.PREV_OUT_SN);
            if (idColumn != -1) {
                prevOutSn = c.getInt(idColumn);
            }
            needUpdateOuts.add(new Object[]{prevTxHash, prevOutSn});

        }
        c.close();


        stmt.executeUpdate(deleteAddressesTx);
        stmt.executeUpdate(deleteOut);
        stmt.executeUpdate(deleteIn);
        stmt.executeUpdate(deleteTx);
        for (Object[] array : needUpdateOuts) {
            PreparedStatement statement = this.mDb.getPreparedStatement(existOtherIn, new String[]{array[0].toString(), array[1].toString()});
            c = statement.executeQuery();
            while (c.next()) {
                int columnIndex = c.findColumn("cnt");
                if (columnIndex != -1 && c.getInt(columnIndex) == 0) {

                    stmt.executeUpdate(updatePrevOut, new String[]{
                            Integer.toString(Out.OutStatus.unspent.getValue()), array[0].toString(), array[1].toString()});
                }

            }
            c.close();
            statement.close();

        }
        stmt.close();
    }

    private List<String> getRelayTx(String txHash) {
        List<String> relayTxHashes = new ArrayList<String>();
        try {
            String relayTx = "select distinct tx_hash from ins where prev_tx_hash=?";
            PreparedStatement statement = this.mDb.getPreparedStatement(relayTx, new String[]{txHash});
            ResultSet c = statement.executeQuery();
            while (c.next()) {
                relayTxHashes.add(c.getString(0));
            }
            c.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return relayTxHashes;
    }

    public boolean isAddress(String address, Tx txItem) {
        boolean result = false;
        String sql = "select count(0) cnt from ins a, txs b where a.tx_hash=b.tx_hash and" +
                " b.block_no is not null and a.prev_tx_hash=? and a.prev_out_sn=?";

        ResultSet c;
        try {
            for (In inItem : txItem.getIns()) {
                PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{Base58.encode(inItem.getPrevTxHash()), Integer.toString(inItem.getPrevOutSn())});
                c = statement.executeQuery();
                if (c.next()) {
                    int columnIndex = c.findColumn("cnt");
                    if (columnIndex != -1 && c.getInt(columnIndex) > 0) {
                        c.close();
                        return false;
                    }
                }
                c.close();
                statement.close();

            }
            String addressSql = "select count(0) cnt from addresses_txs where tx_hash=? and address=?";
            PreparedStatement statement = this.mDb.getPreparedStatement(addressSql, new String[]{Base58.encode(txItem.getTxHash()), address});
            c = statement.executeQuery();
            int count = 0;
            if (c.next()) {
                int columnIndex = c.findColumn("cnt");
                if (columnIndex != -1) {
                    count = c.getInt(columnIndex);
                }
            }
            c.close();
            statement.close();

            if (count > 0) {
                return true;
            }
            String outsCountSql = "select count(0) cnt from outs where tx_hash=? and out_sn=? and out_address=?";
            for (In inItem : txItem.getIns()) {
                statement = this.mDb.getPreparedStatement(outsCountSql, new String[]{Base58.encode(inItem.getPrevTxHash())
                        , Integer.toString(inItem.getPrevOutSn()), address});
                c = statement.executeQuery();
                count = 0;
                int columnIndex = c.findColumn("cnt");
                if (c.next()) {
                    if (columnIndex != -1) {
                        count = c.getInt(columnIndex);
                    }
                }
                c.close();
                statement.close();
                if (count > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void confirmTx(final int blockNo, final List<byte[]> txHashes) {
        if (blockNo == Tx.TX_UNCONFIRMED || txHashes == null) {
            return;
        }

        final String sql = "update txs set block_no=%d where tx_hash='%s'";
        final String existSql = "select count(0) cnt from txs where block_no=" + Integer.toString(blockNo) + " and tx_hash='%s'";
        final String doubleSpendSql = "select a.tx_hash from ins a, ins b where a.prev_tx_hash=b.prev_tx_hash " +
                "and a.prev_out_sn=b.prev_out_sn and a.tx_hash<>b.tx_hash and b.tx_hash='%s'";
        final String blockTimeSql = "select block_time from blocks where block_no=%d";
        final String updateTxTimeThatMoreThanBlockTime = "update txs set tx_time=%d where block_no=%d and tx_time>%d";
        try {


            ResultSet c;
            Statement stmt = this.mDb.getConn().createStatement();
            for (byte[] txHash : txHashes) {
                c = stmt.executeQuery(String.format(Locale.US, existSql, Base58.encode(txHash)));
                if (c.next()) {
                    int columnIndex = c.findColumn("cnt");
                    int cnt = 0;
                    if (columnIndex != -1) {
                        cnt = c.getInt(columnIndex);
                    }
                    c.close();
                    if (cnt > 0) {
                        continue;
                    }
                } else {
                    c.close();
                }
                String updateSql = Utils.format(sql, blockNo, Base58.encode(txHash));
                stmt.execute(updateSql);
                c = stmt.executeQuery(Utils.format(doubleSpendSql, Base58.encode(txHash)));
                List<String> txHashes1 = new ArrayList<String>();
                while (c.next()) {
                    int idColumn = c.findColumn("tx_hash");
                    if (idColumn != -1) {
                        txHashes1.add(c.getString(idColumn));
                    }
                }
                c.close();
                List<String> needRemoveTxHashes = new ArrayList<String>();
                while (txHashes1.size() > 0) {
                    String thisHash = txHashes1.get(0);
                    txHashes1.remove(0);
                    needRemoveTxHashes.add(thisHash);
                    List<String> temp = getRelayTx(thisHash);
                    txHashes1.addAll(temp);
                }
                this.mDb.getConn().setAutoCommit(false);
                for (String each : needRemoveTxHashes) {
                    removeSingleTx(this.mDb.getConn(), each);
                }
                this.mDb.getConn().commit();

            }

            c = stmt.executeQuery(Utils.format(blockTimeSql, blockNo));
            if (c.next())

            {
                int idColumn = c.findColumn("block_time");
                if (idColumn != -1) {
                    int blockTime = c.getInt(idColumn);
                    c.close();
                    String sqlTemp = Utils.format(updateTxTimeThatMoreThanBlockTime, blockTime, blockNo, blockTime);
                    stmt.executeUpdate(sqlTemp);
                }
            } else {
                c.close();
            }
            this.mDb.getConn().commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void unConfirmTxByBlockNo(int blockNo) {
        String sql = "update txs set block_no=null where block_no>=" + blockNo;
        mDb.executeUpdate(sql, null);
    }

    public List<Tx> getUnspendTxWithAddress(String address) {
        String unspendOutSql = "select a.*,b.tx_ver,b.tx_locktime,b.tx_time,b.block_no,b.source,ifnull(b.block_no,0)*a.out_value coin_depth " +
                "from outs a,txs b where a.tx_hash=b.tx_hash" +
                " and a.out_address=? and a.out_status=?";
        List<Tx> txItemList = new ArrayList<Tx>();

        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(unspendOutSql, new String[]{address, Integer.toString(Out.OutStatus.unspent.getValue())});
            ResultSet c = statement.executeQuery();
            while (c.next()) {
                int idColumn = c.findColumn("coin_depth");

                Tx txItem = TxHelper.applyCursor(c);
                Out outItem = TxHelper.applyCursorOut(c);
                if (idColumn != -1) {
                    outItem.setCoinDepth(c.getLong(idColumn));
                }
                outItem.setTx(txItem);
                txItem.setOuts(new ArrayList<Out>());
                txItem.getOuts().add(outItem);
                txItemList.add(txItem);

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

    public List<Out> getUnspendOutWithAddress(String address) {
        List<Out> outItems = new ArrayList<Out>();
        String unspendOutSql = "select a.* from outs a,txs b where a.tx_hash=b.tx_hash " +
                "and b.block_no is null and a.out_address=? and a.out_status=?";

        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(unspendOutSql, new String[]{address, Integer.toString(Out.OutStatus.unspent.getValue())});
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
    public long getConfirmedBalanceWithAddress(String address) {
        long sum = 0;
        try {

            String unspendOutSql = "select ifnull(sum(a.out_value),0) sum from outs a,txs b where a.tx_hash=b.tx_hash " +
                    " and a.out_address=? and a.out_status=? and b.block_no is not null";
            PreparedStatement statement = this.mDb.getPreparedStatement(unspendOutSql,
                    new String[]{address, Integer.toString(Out.OutStatus.unspent.getValue())});
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
    public List<Tx> getUnconfirmedTxWithAddress(String address) {
        List<Tx> txList = new ArrayList<Tx>();

        HashMap<Sha256Hash, Tx> txDict = new HashMap<Sha256Hash, Tx>();

        try {
            String sql = "select b.* from addresses_txs a, txs b " +
                    "where a.tx_hash=b.tx_hash and a.address=? and b.block_no is null " +
                    "order by b.block_no desc";
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{address});
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
            sql = "select b.tx_hash,b.in_sn,b.prev_tx_hash,b.prev_out_sn,b.in_signature,b.in_sequence" +
                    " from addresses_txs a, ins b, txs c " +
                    " where a.tx_hash=b.tx_hash and b.tx_hash=c.tx_hash and c.block_no is null and a.address=? "
                    + " order by b.tx_hash ,b.in_sn";
            statement = this.mDb.getPreparedStatement(sql, new String[]{address});
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

            sql = "select b.tx_hash,b.out_sn,b.out_value,b.out_address,b.out_script,b.out_status " +
                    "from addresses_txs a, outs b, txs c " +
                    "where a.tx_hash=b.tx_hash and b.tx_hash=c.tx_hash and c.block_no is null and a.address=? "
                    + "order by b.tx_hash,b.out_sn";
            statement = this.mDb.getPreparedStatement(sql, new String[]{address});
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

    public List<Out> getUnSpendOutCanSpendWithAddress(String address) {
        List<Out> outItems = new ArrayList<Out>();
        String confirmedOutSql = "select a.*,b.block_no*a.out_value coin_depth from outs a,txs b" +
                " where a.tx_hash=b.tx_hash and b.block_no is not null and a.out_address=? and a.out_status=?";
        String selfOutSql = "select a.* from outs a,txs b where a.tx_hash=b.tx_hash and b.block_no" +
                " is null and a.out_address=? and a.out_status=? and b.source>=1";


        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(confirmedOutSql, new String[]{address, Integer.toString(Out.OutStatus.unspent.getValue())});
            ResultSet c = statement.executeQuery();
            while (c.next()) {
                Out outItem = TxHelper.applyCursorOut(c);
                int idColumn = c.findColumn("coin_depth");
                if (idColumn != -1) {
                    outItem.setCoinDepth(c.getLong(idColumn));
                }
                outItems.add(outItem);
            }
            c.close();
            statement.close();
            statement = this.mDb.getPreparedStatement(selfOutSql, new String[]{address, Integer.toString(Out.OutStatus.unspent.getValue())});
            c = statement.executeQuery();
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

    public List<Out> getUnSpendOutButNotConfirmWithAddress(String address) {
        List<Out> outItems = new ArrayList<Out>();
        String selfOutSql = "select a.* from outs a,txs b where a.tx_hash=b.tx_hash and b.block_no" +
                " is null and a.out_address=? and a.out_status=? and b.source=0";

        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(selfOutSql, new String[]{address, Integer.toString(Out.OutStatus.unspent.getValue())});
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

    public int txCount(String address) {
        int result = 0;
        try {
            String sql = "select count(*) cnt from addresses_txs  where address=?";
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{address});
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
    public long totalReceive(String address) {
        long result = 0;
        try {
            String sql = "select sum(aa.receive-ifnull(bb.send,0)) sum" +
                    "  from (select a.tx_hash,sum(a.out_value) receive " +
                    "    from outs a where a.out_address=?" +
                    "    group by a.tx_hash) aa LEFT OUTER JOIN " +
                    "  (select b.tx_hash,sum(a.out_value) send" +
                    "    from outs a, ins b" +
                    "    where a.tx_hash=b.prev_tx_hash and a.out_sn=b.prev_out_sn and a.out_address=?" +
                    "    group by b.tx_hash) bb on aa.tx_hash=bb.tx_hash " +
                    "  where aa.receive>ifnull(bb.send, 0)";
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{address, address});
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                int idColumn = c.findColumn("sum");
                if (idColumn != -1) {
                    result = c.getLong(idColumn);
                }
            }
            c.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    public void txSentBySelfHasSaw(byte[] txHash) {
        String sql = "update txs set source=source+1 where tx_hash=? and source>=1";
        mDb.executeUpdate(sql, new String[]{Base58.encode(txHash)});
    }

    public List<Out> getOuts() {
        List<Out> outItemList = new ArrayList<Out>();
        String sql = "select * from outs ";
        try {
            PreparedStatement preparedStatement = this.mDb.getPreparedStatement(sql, null);
            ResultSet c = preparedStatement.executeQuery();

            while (c.next()) {
                outItemList.add(TxHelper.applyCursorOut(c));
            }
            c.close();

            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        }
        return outItemList;
    }

    public List<Tx> getRecentlyTxsByAddress(String address, int greateThanBlockNo, int limit) {
        List<Tx> txItemList = new ArrayList<Tx>();

        String sql = "select b.* from addresses_txs a, txs b where a.tx_hash=b.tx_hash and a.address=? " +
                "and ((b.block_no is null) or (b.block_no is not null and b.block_no>?)) " +
                "order by ifnull(b.block_no,4294967295) desc, b.tx_time desc " +
                "limit ? ";

        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{address, Integer.toString(greateThanBlockNo), Integer.toString(limit)});
            ResultSet c = statement.executeQuery();
            while (c.next()) {
                Tx txItem = TxHelper.applyCursor(c);
                txItemList.add(txItem);
            }
            for (Tx item : txItemList) {
                TxHelper.addInsAndOuts(this.mDb, item);
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

    public List<Long> txInValues(byte[] txHash) {
        List<Long> inValues = new ArrayList<Long>();
        try {
            String sql = "select b.out_value " +
                    "from ins a left outer join outs b on a.prev_tx_hash=b.tx_hash and a.prev_out_sn=b.out_sn " +
                    "where a.tx_hash=?";
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{Base58.encode(txHash)});
            ResultSet c = statement.executeQuery();
            while (c.next()) {
                int idColumn = c.findColumn("out_value");
                if (idColumn != -1) {
                    inValues.add(c.getLong(idColumn));
                } else {
                    inValues.add(null);
                }
            }
            c.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inValues;
    }

    public HashMap<Sha256Hash, Tx> getTxDependencies(Tx txItem) {
        HashMap<Sha256Hash, Tx> result = new HashMap<Sha256Hash, Tx>();
        try {
            for (In inItem : txItem.getIns()) {
                Tx tx;
                String txHashStr = Base58.encode(inItem.getTxHash());
                String sql = "select * from txs where tx_hash=?";
                PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{txHashStr});
                ResultSet c = statement.executeQuery();
                if (c.next()) {
                    tx = TxHelper.applyCursor(c);
                    c.close();
                    statement.close();
                } else {
                    c.close();
                    statement.close();
                    continue;
                }
                TxHelper.addInsAndOuts(this.mDb, tx);
                result.put(new Sha256Hash(tx.getTxHash()), tx);

            }
        } catch (AddressFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean isTxDoubleSpendWithConfirmedTx(Tx tx) {
        String sql = "select count(0) cnt from ins a, txs b where a.tx_hash=b.tx_hash and" +
                " b.block_no is not null and a.prev_tx_hash=? and a.prev_out_sn=?";
        ResultSet rs;
        try {
            for (In inItem : tx.getIns()) {
                PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{Base58.encode(inItem.getPrevTxHash()), Integer.toString(inItem.getPrevOutSn())});
                rs = statement.executeQuery();
                if (rs.next()) {
                    int columnIndex = rs.findColumn("cnt");
                    if (columnIndex != -1 && rs.getInt(columnIndex) > 0) {
                        rs.close();
                        statement.close();
                        return true;
                    }
                }
                rs.close();
                statement.close();

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getInAddresses(Tx tx) {
        List<String> result = new ArrayList<String>();
        String sql = "select out_address from outs where tx_hash=? and out_sn=?";
        ResultSet c;
        try {
            for (In inItem : tx.getIns()) {
                PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{Base58.encode(inItem.getPrevTxHash()), Integer.toString(inItem.getPrevOutSn())});
                c = statement.executeQuery();
                if (c.next()) {
                    int column = c.findColumn("out_address");
                    if (column != -1) {
                        result.add(c.getString(column));
                    }
                }
                c.close();
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void completeInSignature(final List<In> ins) {
        try {
            this.mDb.getConn().setAutoCommit(false);
            String sql = "update ins set in_signature=? where tx_hash=? and in_sn=? and ifnull(in_signature,'')=''";
            for (In in : ins) {
                PreparedStatement preparedStatement = this.mDb.getConn().prepareStatement(sql);
                preparedStatement.setString(1, Base58.encode(in.getInSignature()));
                preparedStatement.setString(2, Base58.encode(in.getTxHash()));
                preparedStatement.setInt(3, in.getInSn());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
            this.mDb.getConn().commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public int needCompleteInSignature(String address) {
        int result = 0;
        String sql = "select max(txs.block_no) as block_no from outs,ins,txs where outs.out_address='" + address +
                "' and ins.prev_tx_hash=outs.tx_hash and ins.prev_out_sn=outs.out_sn " +
                " and ifnull(ins.in_signature,'')='' and txs.tx_hash=ins.tx_hash";
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, null);
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                int index = c.findColumn("block_no");
                if (index != -1) {
                    result = c.getInt(index);
                }
            }
            c.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    public List<Out> getUnSpentOuts() {
        List<Out> outItemList = new ArrayList<Out>();

        String sql = "select * from outs where out_status=0";

        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, null);
            ResultSet c = statement.executeQuery();
            while (c.next()) {
                outItemList.add(TxHelper.applyCursorOut(c));
            }
            c.close();
            statement.close();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return outItemList;
    }

    public boolean isAddressContainsTx(String address, Tx txItem) {
        boolean result = false;
        try {


            String sql = "select count(0) cnt from ins a, txs b where a.tx_hash=b.tx_hash and" +
                    " b.block_no is not null and a.prev_tx_hash=? and a.prev_out_sn=?";
            ResultSet c;
            for (In inItem : txItem.getIns()) {
                PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{Base58.encode(inItem.getPrevTxHash()), Integer.toString(inItem.getPrevOutSn())});
                c = statement.executeQuery();
                if (c.next()) {
                    int columnIndex = c.findColumn("cnt");
                    if (columnIndex != -1 && c.getInt(columnIndex) > 0) {
                        c.close();
                        statement.close();
                        return false;
                    }
                }
                c.close();
                statement.close();

            }
            sql = "select count(0) cnt from addresses_txs where tx_hash=? and address=?";
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{
                    Base58.encode(txItem.getTxHash()), address
            });
            c = statement.executeQuery();
            int count = 0;

            if (c.next()) {
                int columnIndex = c.findColumn("cnt");
                if (columnIndex != -1) {
                    count = c.getInt(columnIndex);
                }
            }
            c.close();
            statement.close();
            if (count > 0) {
                return true;
            }
            sql = "select count(0) cnt from outs where tx_hash=? and out_sn=? and out_address=?";
            for (In inItem : txItem.getIns()) {
                statement = this.mDb.getPreparedStatement(sql, new String[]{Base58.encode(inItem.getPrevTxHash())
                        , Integer.toString(inItem.getPrevOutSn()), address});
                c = statement.executeQuery();
                count = 0;
                if (c.next()) {
                    int columnIndex = c.findColumn("cnt");
                    if (columnIndex != -1) {
                        count = c.getInt(columnIndex);
                    }
                }
                c.close();
                statement.close();
                if (count > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    public void clearAllTx() {
        this.mDb.rebuildTx();

    }

    @Override
    public byte[] isIdentify(Tx tx) {
        HashSet<String> result = new HashSet<String>();

        for (In in : tx.getIns()) {
            String queryPrevTxHashSql = "select tx_hash from ins where prev_tx_hash=? and prev_out_sn=?";
            final HashSet<String> each = new HashSet<String>();
            try {
                PreparedStatement statement = this.mDb.getPreparedStatement(queryPrevTxHashSql, new String[]{Base58.encode(in.getPrevTxHash())
                        , Integer.toString(in.getPrevOutSn())});
                ResultSet c = statement.executeQuery();
                while (c.next()) {
                    each.add(c.getString(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            each.remove(Base58.encode(tx.getTxHash()));
            result.retainAll(each);
            if (result.size() == 0) {
                break;
            }
        }
        if (result.size() == 0) {
            return new byte[0];
        } else {
            try {
                return Base58.decode((String) result.toArray()[0]);
            } catch (AddressFormatException e) {
                e.printStackTrace();
                return new byte[0];
            }
        }
    }


    private static class AddressTx {
        private String address;
        private String txHash;

        public AddressTx(String address, String txHash) {
            this.address = address;
            this.txHash = txHash;

        }

        public String getTxHash() {
            return txHash;
        }

        public void setTxHash(String txHash) {
            this.txHash = txHash;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }


    }
}


































