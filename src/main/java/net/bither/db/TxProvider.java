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
import net.bither.bitherj.core.In;
import net.bither.bitherj.core.Out;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.db.AbstractDb;
import net.bither.bitherj.db.ITxProvider;
import net.bither.bitherj.exception.AddressFormatException;
import net.bither.bitherj.utils.Base58;
import net.bither.bitherj.utils.Sha256Hash;
import net.bither.bitherj.utils.Utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class TxProvider implements ITxProvider {

    String txInsertSql = "insert into txs " +
            "(tx_hash,tx_ver,tx_locktime,tx_time,block_no,source)" +
            " values (?,?,?,?,?,?) ";
    String inInsertSql = "insert into ins " +
            "(tx_hash,in_sn,prev_tx_hash,prev_out_sn,in_signature,in_sequence)" +
            " values (?,?,?,?,?,?) ";

    String outInsertSql = "insert into outs " +
            "(tx_hash,out_sn,out_script,out_value,out_status,out_address)" +
            " values (?,?,?,?,?,?) ";

    private static TxProvider txProvider = new TxProvider(ApplicationInstanceManager.mDBHelper);

    public static TxProvider getInstance() {
        return txProvider;
    }

    private BitherDBHelper mDb;

    public TxProvider(BitherDBHelper db) {
        this.mDb = db;
    }


    public List<Tx> getTxAndDetailByAddress(String address) {
        List<Tx> txItemList = new ArrayList<Tx>();
        HashMap<Sha256Hash, Tx> txDict = new HashMap<Sha256Hash, Tx>();

        try {
            String sql = "select b.* from addresses_txs a, txs b where a.tx_hash=b.tx_hash and a.address=? order by b.block_no ";
            ResultSet c = mDb.query(sql, new String[]{address});
            while (c.next()) {
                Tx txItem = applyCursor(c);
                txItem.setIns(new ArrayList<In>());
                txItem.setOuts(new ArrayList<Out>());
                txItemList.add(txItem);
                txDict.put(new Sha256Hash(txItem.getTxHash()), txItem);
            }
            c.close();

            sql = "select b.* from addresses_txs a, ins b where a.tx_hash=b.tx_hash and a.address=? order by b.tx_hash ,b.in_sn";
            c = mDb.query(sql, new String[]{address});
            while (c.next()) {
                In inItem = applyCursorIn(c);
                Tx tx = txDict.get(new Sha256Hash(inItem.getTxHash()));
                if (tx != null)
                    tx.getIns().add(inItem);
            }
            c.close();

            sql = "select b.* from addresses_txs a, outs b where a.tx_hash=b.tx_hash and a.address=? order by b.tx_hash,b.out_sn";
            c = mDb.query(sql, new String[]{address});
            while (c.next()) {
                Out out = applyCursorOut(c);
                Tx tx = txDict.get(new Sha256Hash(out.getTxHash()));
                if (tx != null)
                    tx.getOuts().add(out);
            }
            c.close();

        } catch (AddressFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return txItemList;
    }

    @Override
    public List<Tx> getTxAndDetailByAddress(String address, int page) {
        return null;
    }

    public List<Tx> getPublishedTxs() {
        List<Tx> txItemList = new ArrayList<Tx>();
        HashMap<Sha256Hash, Tx> txDict = new HashMap<Sha256Hash, Tx>();

        String sql = "select * from txs where block_no is null or block_no =?";
        try {
            ResultSet c = mDb.query(sql, new String[]{Integer.toString(Tx.TX_UNCONFIRMED)});
            while (c.next()) {
                Tx txItem = applyCursor(c);
                txItem.setIns(new ArrayList<In>());
                txItem.setOuts(new ArrayList<Out>());
                txItemList.add(txItem);
                txDict.put(new Sha256Hash(txItem.getTxHash()), txItem);
            }
            c.close();

            sql = "select b.* from txs a, ins b  where a.tx_hash=b.tx_hash  and ( a.block_no is null or a.block_no =?) order by b.tx_hash ,b.in_sn";
            c = mDb.query(sql, new String[]{Integer.toString(Tx.TX_UNCONFIRMED)});
            while (c.next()) {
                In inItem = applyCursorIn(c);
                Tx tx = txDict.get(new Sha256Hash(inItem.getTxHash()));
                tx.getIns().add(inItem);
            }
            c.close();

            sql = "select b.* from txs a, outs b where a.tx_hash=b.tx_hash and ( a.block_no is null or a.block_no = ? )order by b.tx_hash,b.out_sn";
            c = mDb.query(sql, new String[]{Integer.toString(Tx.TX_UNCONFIRMED)});
            while (c.next()) {
                Out out = applyCursorOut(c);
                Tx tx = txDict.get(new Sha256Hash(out.getTxHash()));
                tx.getOuts().add(out);
            }
            c.close();

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
        ResultSet rs = this.mDb.query(sql, new String[]{address});
        try {
            while (rs.next()) {
                list.add(applyCursorIn(rs));
            }
            rs.close();
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
        ResultSet c = mDb.query(sql, new String[]{txHashStr});
        try {
            if (c.next()) {
                txItem = applyCursor(c);
            }

            if (txItem != null) {
                addInsAndOuts(txItem);

            }
            c.close();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return txItem;
    }

    @Override
    public long sentFromAddress(byte[] txHash, String address) {
        return 0;
    }

    private void addInsAndOuts(Tx txItem) throws AddressFormatException, SQLException {
        String txHashStr = Base58.encode(txItem.getTxHash());
        txItem.setOuts(new ArrayList<Out>());
        txItem.setIns(new ArrayList<In>());
        String sql = "select * from ins where tx_hash=? order by in_sn";
        ResultSet c = mDb.query(sql, new String[]{txHashStr});
        while (c.next()) {
            In inItem = applyCursorIn(c);
            inItem.setTx(txItem);
            txItem.getIns().add(inItem);
        }
        c.close();

        sql = "select * from outs where tx_hash=? order by out_sn";
        c = mDb.query(sql, new String[]{txHashStr});
        while (c.next()) {
            Out outItem = applyCursorOut(c);
            outItem.setTx(txItem);
            txItem.getOuts().add(outItem);
        }
        c.close();
    }

    public boolean isExist(byte[] txHash) {
        boolean result = false;
        try {
            String sql = "select count(0) cnt from txs where tx_hash=?";
            ResultSet c = mDb.query(sql, new String[]{Base58.encode(txHash)});

            if (c.next()) {
                int columnIndex = c.findColumn("cnt");
                if (columnIndex != -1) {
                    result = c.getInt(columnIndex) > 0;
                }
            }
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void add(final Tx txItem) {
        mDb.executeUpdate(new BitherDBHelper.IExecuteDB() {
            @Override
            public void execute(Connection conn) throws SQLException {
                addTxToDb(conn, txItem);
            }
        });

    }

    public void addTxs(List<Tx> txItems) {
        try {
            final List<Tx> addTxItems = new ArrayList<Tx>();
            String existSql = "select count(0) cnt from txs where tx_hash=?";
            ResultSet c;
            for (Tx txItem : txItems) {
                c = mDb.query(existSql, new String[]{Base58.encode(txItem.getTxHash())});
                int cnt = 0;
                if (c.next()) {
                    int idColumn = c.findColumn("cnt");
                    if (idColumn != -1) {
                        cnt = c.getInt(idColumn);
                    }
                }
                if (cnt == 0) {
                    addTxItems.add(txItem);
                }
                c.close();
            }
            if (addTxItems.size() > 0) {
                mDb.executeUpdate(new BitherDBHelper.IExecuteDB() {
                    @Override
                    public void execute(Connection conn) throws SQLException {
                        for (Tx txItem : addTxItems) {
                            //LogUtil.d("txDb", Base58.encode(txItem.getTxHash()) + "," + Utils.bytesToHexString(txItem.getTxHash()));
                            addTxToDb(conn, txItem);
                            //List<Tx> txList = getTxAndDetailByAddress("1B5XuAJNTN2Upi7AXs7tJCxvFGjhPna6Q5");
                        }
                    }
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void addTxToDb(Connection conn, Tx txItem) throws SQLException {
        String blockNoString = null;
        if (txItem.getBlockNo() != Tx.TX_UNCONFIRMED) {
            blockNoString = Integer.toString(txItem.getBlockNo());
        }
        PreparedStatement preparedStatement = conn.prepareStatement(txInsertSql);
        preparedStatement.setString(1, Base58.encode(txItem.getTxHash()));
        preparedStatement.setLong(2, txItem.getTxVer());
        preparedStatement.setLong(3, txItem.getTxLockTime());
        preparedStatement.setLong(4, txItem.getTxTime());
        preparedStatement.setString(5, blockNoString);
        preparedStatement.setInt(6, txItem.getSource());
        preparedStatement.executeUpdate();

        ResultSet c;
        String sql;
        Statement stmt = conn.createStatement();
        List<Object[]> addressesTxsRels = new ArrayList<Object[]>();
        try {
            for (In inItem : txItem.getIns()) {
                sql = "select out_address from outs where tx_hash='"
                        + Base58.encode(inItem.getPrevTxHash()) + "' and out_sn=" + inItem.getPrevOutSn();
                c = stmt.executeQuery(sql);
                while (c.next()) {
                    int idColumn = c.findColumn("out_address");
                    if (idColumn != -1) {
                        addressesTxsRels.add(new Object[]{c.getString(idColumn), txItem.getTxHash()});
                    }
                }
                c.close();

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
                sql = "update outs set out_status=" + Out.OutStatus.spent.getValue() +
                        " where tx_hash='" + Base58.encode(inItem.getPrevTxHash()) + "' and out_sn=" + inItem.getPrevOutSn();
                stmt.executeUpdate(sql);
            }
            for (Out outItem : txItem.getOuts()) {

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
                preparedStatement.executeUpdate();
                if (!Utils.isEmpty(outItem.getOutAddress())) {
                    addressesTxsRels.add(new Object[]{outItem.getOutAddress(), txItem.getTxHash()});
                }
                sql = "select tx_hash from ins where prev_tx_hash='" + Base58.encode(txItem.getTxHash())
                        + "' and prev_out_sn=" + outItem.getOutSn();
                c = stmt.executeQuery(sql);
                boolean isSpentByExistTx = false;
                if (c.next()) {
                    int idColumn = c.findColumn("tx_hash");
                    if (idColumn != -1) {
                        addressesTxsRels.add(new Object[]{outItem.getOutAddress(), Base58.decode(c.getString(idColumn))});
                    }
                    isSpentByExistTx = true;
                }
                c.close();
                if (isSpentByExistTx) {
                    sql = "update outs set out_status=" + Out.OutStatus.spent.getValue() +
                            " where tx_hash='" + Base58.encode(txItem.getTxHash()) + "' and out_sn=" + outItem.getOutSn();

                    stmt.executeUpdate(sql);
                }

            }
            for (Object[] array : addressesTxsRels) {
                sql = "insert or ignore into addresses_txs(address, tx_hash) values('"
                        + array[0] + "','" + Base58.encode((byte[]) array[1]) + "')";
                stmt.executeUpdate(sql);
            }

        } catch (AddressFormatException e) {
            e.printStackTrace();
        }

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
        mDb.executeUpdate(new BitherDBHelper.IExecuteDB() {
            @Override
            public void execute(Connection conn) throws SQLException {
                for (String str : needRemoveTxHashes) {
                    removeSingleTx(conn, str);
                }
            }
        });

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

            c = mDb.query(existOtherIn, new String[]{array[0].toString(), array[1].toString()});
            while (c.next()) {
                int columnIndex = c.findColumn("cnt");
                if (columnIndex != -1 && c.getInt(columnIndex) == 0) {

                    stmt.executeUpdate(updatePrevOut, new String[]{
                            Integer.toString(Out.OutStatus.unspent.getValue()), array[0].toString(), array[1].toString()});
                }

            }
            c.close();

        }
    }

    private List<String> getRelayTx(String txHash) {
        List<String> relayTxHashes = new ArrayList<String>();
        try {
            String relayTx = "select distinct tx_hash from ins where prev_tx_hash=?";
            ResultSet c = mDb.query(relayTx, new String[]{txHash});
            while (c.next()) {
                relayTxHashes.add(c.getString(0));
            }
            c.close();
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
                c = mDb.query(sql, new String[]{Base58.encode(inItem.getPrevTxHash()), Integer.toString(inItem.getPrevOutSn())});
                if (c.next()) {
                    int columnIndex = c.findColumn("cnt");
                    if (columnIndex != -1 && c.getInt(columnIndex) > 0) {
                        c.close();
                        return false;
                    }
                }
                c.close();

            }
            String addressSql = "select count(0) cnt from addresses_txs where tx_hash=? and address=?";
            c = mDb.query(addressSql, new String[]{Base58.encode(txItem.getTxHash()), address});
            int count = 0;
            if (c.next()) {
                int columnIndex = c.findColumn("cnt");
                if (columnIndex != -1) {
                    count = c.getInt(columnIndex);
                }
            }
            c.close();
            if (count > 0) {
                return true;
            }
            String outsCountSql = "select count(0) cnt from outs where tx_hash=? and out_sn=? and out_address=?";
            for (In inItem : txItem.getIns()) {
                c = mDb.query(outsCountSql, new String[]{Base58.encode(inItem.getPrevTxHash())
                        , Integer.toString(inItem.getPrevOutSn()), address});
                count = 0;
                int columnIndex = c.findColumn("cnt");
                if (c.next()) {
                    if (columnIndex != -1) {
                        count = c.getInt(columnIndex);
                    }
                }
                c.close();
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
        mDb.executeUpdate(new BitherDBHelper.IExecuteDB() {
            @Override
            public void execute(Connection conn) throws SQLException {
                ResultSet c;
                Statement stmt = conn.createStatement();
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
                    for (String each : needRemoveTxHashes) {
                        removeSingleTx(conn, each);
                    }

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
                } else

                {
                    c.close();
                }
            }
        });

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
        ResultSet c = mDb.query(unspendOutSql, new String[]{address, Integer.toString(Out.OutStatus.unspent.getValue())});
        try {
            while (c.next()) {
                int idColumn = c.findColumn("coin_depth");

                Tx txItem = applyCursor(c);
                Out outItem = applyCursorOut(c);
                if (idColumn != -1) {
                    outItem.setCoinDepth(c.getLong(idColumn));
                }
                outItem.setTx(txItem);
                txItem.setOuts(new ArrayList<Out>());
                txItem.getOuts().add(outItem);
                txItemList.add(txItem);

            }
            c.close();
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
        ResultSet c = mDb.query(unspendOutSql, new String[]{address, Integer.toString(Out.OutStatus.unspent.getValue())});
        try {
            while (c.next()) {
                outItems.add(applyCursorOut(c));
            }
            c.close();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return outItems;
    }

    @Override
    public long getConfirmedBalanceWithAddress(String address) {
        return 0;
    }

    @Override
    public List<Tx> getUnconfirmedTxWithAddress(String address) {
        return null;
    }

    public List<Out> getUnSpendOutCanSpendWithAddress(String address) {
        List<Out> outItems = new ArrayList<Out>();
        String confirmedOutSql = "select a.*,b.block_no*a.out_value coin_depth from outs a,txs b" +
                " where a.tx_hash=b.tx_hash and b.block_no is not null and a.out_address=? and a.out_status=?";
        String selfOutSql = "select a.* from outs a,txs b where a.tx_hash=b.tx_hash and b.block_no" +
                " is null and a.out_address=? and a.out_status=? and b.source>=1";

        ResultSet c = mDb.query(confirmedOutSql, new String[]{address, Integer.toString(Out.OutStatus.unspent.getValue())});
        try {
            while (c.next()) {
                Out outItem = applyCursorOut(c);
                int idColumn = c.findColumn("coin_depth");
                if (idColumn != -1) {
                    outItem.setCoinDepth(c.getLong(idColumn));
                }
                outItems.add(outItem);
            }
            c.close();
            c = mDb.query(selfOutSql, new String[]{address, Integer.toString(Out.OutStatus.unspent.getValue())});
            while (c.next()) {
                outItems.add(applyCursorOut(c));
            }
            c.close();
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
        ResultSet c = mDb.query(selfOutSql, new String[]{address, Integer.toString(Out.OutStatus.unspent.getValue())});
        try {
            while (c.next()) {
                outItems.add(applyCursorOut(c));

            }
            c.close();
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
            String sql = "select count(*) from addresses_txs  where address=?";
            ResultSet c = mDb.query(sql, new String[]{address});
            if (c.next()) {
                result = c.getInt(0);
            }
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public long totalReceive(String address) {
        return 0;
    }


    public void txSentBySelfHasSaw(byte[] txHash) {
        String sql = "update txs set source=source+1 where tx_hash=? and source>=1";
        mDb.executeUpdate(sql, new String[]{Base58.encode(txHash)});
    }

    public List<Out> getOuts() {
        List<Out> outItemList = new ArrayList<Out>();
        String sql = "select * from outs ";
        try {
            ResultSet c = mDb.query(sql, null);
            try {
                while (c.next()) {
                    outItemList.add(applyCursorOut(c));
                }
            } catch (AddressFormatException e) {
                e.printStackTrace();
            } finally {
                c.close();
            }
        } catch (SQLException e) {
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
        ResultSet c = mDb.query(sql, new String[]{address, Integer.toString(greateThanBlockNo), Integer.toString(limit)});
        try {
            while (c.next()) {
                Tx txItem = applyCursor(c);
                txItemList.add(txItem);
            }

            for (Tx item : txItemList) {
                addInsAndOuts(item);
            }
            c.close();
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
            ResultSet c = mDb.query(sql, new String[]{Base58.encode(txHash)});
            while (c.next()) {
                int idColumn = c.findColumn("out_value");
                if (idColumn != -1) {
                    inValues.add(c.getLong(idColumn));
                } else {
                    inValues.add(null);
                }
            }
            c.close();
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
                ResultSet c = mDb.query(sql, new String[]{txHashStr});
                if (c.next()) {
                    tx = applyCursor(c);
                    c.close();
                } else {
                    c.close();
                    continue;
                }
                addInsAndOuts(tx);
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

                rs = this.mDb.query(sql, new String[]{Base58.encode(inItem.getPrevTxHash()), Integer.toString(inItem.getPrevOutSn())});
                if (rs.next()) {
                    int columnIndex = rs.findColumn("cnt");
                    if (columnIndex != -1 && rs.getInt(columnIndex) > 0) {
                        rs.close();
                        return true;
                    }
                }
                rs.close();

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
                c = this.mDb.query(sql, new String[]{Base58.encode(inItem.getPrevTxHash()), Integer.toString(inItem.getPrevOutSn())});
                if (c.next()) {
                    int column = c.findColumn("out_address");
                    if (column != -1) {
                        result.add(c.getString(column));
                    }
                }
                c.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void completeInSignature(final List<In> ins) {
        this.mDb.executeUpdate(new BitherDBHelper.IExecuteDB() {
            @Override
            public void execute(Connection conn) throws SQLException {
                String sql = "update ins set in_signature=? where tx_hash=? and in_sn=? and ifnull(in_signature,'')=''";
                for (In in : ins) {
                    PreparedStatement preparedStatement = conn.prepareStatement(sql);
                    preparedStatement.setString(1, Base58.encode(in.getInSignature()));
                    preparedStatement.setString(2, Base58.encode(in.getTxHash()));
                    preparedStatement.setInt(3, in.getInSn());
                    preparedStatement.executeUpdate();
                }

            }
        });

    }

    public int needCompleteInSignature(String address) {
        int result = 0;
        String sql = "select max(txs.block_no) as block_no from outs,ins,txs where outs.out_address='" + address +
                "' and ins.prev_tx_hash=outs.tx_hash and ins.prev_out_sn=outs.out_sn " +
                " and ifnull(ins.in_signature,'')='' and txs.tx_hash=ins.tx_hash";
        ResultSet c = this.mDb.query(sql, null);
        try {

            if (c.next()) {
                int index = c.findColumn("block_no");
                if (index != -1) {
                    result = c.getInt(index);
                }
            }
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    public List<Out> getUnSpentOuts() {
        List<Out> outItemList = new ArrayList<Out>();

        String sql = "select * from outs where out_status=0";
        ResultSet c = this.mDb.query(sql, null);
        try {
            while (c.next()) {
                outItemList.add(applyCursorOut(c));
            }
            c.close();
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

                c = this.mDb.query(sql, new String[]{Base58.encode(inItem.getPrevTxHash()), Integer.toString(inItem.getPrevOutSn())});
                if (c.next()) {
                    int columnIndex = c.findColumn("cnt");
                    if (columnIndex != -1 && c.getInt(columnIndex) > 0) {
                        c.close();
                        return false;
                    }
                }
                c.close();

            }
            sql = "select count(0) cnt from addresses_txs where tx_hash=? and address=?";
            c = this.mDb.query(sql, new String[]{
                    Base58.encode(txItem.getTxHash()), address
            });
            int count = 0;

            if (c.next()) {
                int columnIndex = c.findColumn("cnt");
                if (columnIndex != -1) {
                    count = c.getInt(columnIndex);
                }
            }
            c.close();
            if (count > 0) {
                return true;
            }
            sql = "select count(0) cnt from outs where tx_hash=? and out_sn=? and out_address=?";
            for (In inItem : txItem.getIns()) {

                c = this.mDb.query(sql, new String[]{Base58.encode(inItem.getPrevTxHash())
                        , Integer.toString(inItem.getPrevOutSn()), address});
                count = 0;
                if (c.next()) {
                    int columnIndex = c.findColumn("cnt");
                    if (columnIndex != -1) {
                        count = c.getInt(columnIndex);
                    }
                }
                c.close();
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
        mDb.executeUpdate(new BitherDBHelper.IExecuteDB() {
            @Override
            public void execute(Connection conn) throws SQLException {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("delete from txs");
                stmt.executeUpdate("delete from ins");
                stmt.executeUpdate("delete from outs");
                stmt.executeUpdate("delete from addresses_txs");
                stmt.executeUpdate("delete from peers");
            }
        });
    }


    private Tx applyCursor(ResultSet c) throws AddressFormatException, SQLException {
        Tx txItem = new Tx();
        int idColumn = c.findColumn(AbstractDb.TxsColumns.BLOCK_NO);
        if (idColumn != -1 && c.getObject(idColumn) != null) {
            txItem.setBlockNo(c.getInt(idColumn));
        } else {
            txItem.setBlockNo(Tx.TX_UNCONFIRMED);
        }
        idColumn = c.findColumn(AbstractDb.TxsColumns.TX_HASH);
        if (idColumn != -1) {
            txItem.setTxHash(Base58.decode(c.getString(idColumn)));
        }
        idColumn = c.findColumn(AbstractDb.TxsColumns.SOURCE);
        if (idColumn != -1) {
            txItem.setSource(c.getInt(idColumn));
        }
        if (txItem.getSource() >= 1) {
            txItem.setSawByPeerCnt(txItem.getSource() - 1);
            txItem.setSource(1);
        } else {
            txItem.setSawByPeerCnt(0);
            txItem.setSource(0);
        }
        idColumn = c.findColumn(AbstractDb.TxsColumns.TX_TIME);
        if (idColumn != -1) {
            txItem.setTxTime(c.getInt(idColumn));
        }
        idColumn = c.findColumn(AbstractDb.TxsColumns.TX_VER);
        if (idColumn != -1) {
            txItem.setTxVer(c.getInt(idColumn));
        }
        idColumn = c.findColumn(AbstractDb.TxsColumns.TX_LOCKTIME);
        if (idColumn != -1) {
            txItem.setTxLockTime(c.getInt(idColumn));
        }
        return txItem;

    }

    private In applyCursorIn(ResultSet c) throws AddressFormatException, SQLException {
        In inItem = new In();
        int idColumn = c.findColumn(AbstractDb.InsColumns.TX_HASH);
        if (idColumn != -1) {
            inItem.setTxHash(Base58.decode(c.getString(idColumn)));
        }
        idColumn = c.findColumn(AbstractDb.InsColumns.IN_SN);
        if (idColumn != -1) {
            inItem.setInSn(c.getInt(idColumn));
        }
        idColumn = c.findColumn(AbstractDb.InsColumns.PREV_TX_HASH);
        if (idColumn != -1) {
            inItem.setPrevTxHash(Base58.decode(c.getString(idColumn)));
        }
        idColumn = c.findColumn(AbstractDb.InsColumns.PREV_OUT_SN);
        if (idColumn != -1) {
            inItem.setPrevOutSn(c.getInt(idColumn));
        }
        idColumn = c.findColumn(AbstractDb.InsColumns.IN_SIGNATURE);
        if (idColumn != -1) {
            String inSignature = c.getString(idColumn);
            if (!Utils.isEmpty(inSignature)) {
                inItem.setInSignature(Base58.decode(c.getString(idColumn)));
            }
        }
        idColumn = c.findColumn(AbstractDb.InsColumns.IN_SEQUENCE);
        if (idColumn != -1) {
            inItem.setInSequence(c.getInt(idColumn));
        }
        return inItem;
    }

    private Out applyCursorOut(ResultSet c) throws AddressFormatException, SQLException {
        Out outItem = new Out();
        int idColumn = c.findColumn(AbstractDb.OutsColumns.TX_HASH);
        if (idColumn != -1) {
            outItem.setTxHash(Base58.decode(c.getString(idColumn)));
        }
        idColumn = c.findColumn(AbstractDb.OutsColumns.OUT_SN);
        if (idColumn != -1) {
            outItem.setOutSn(c.getInt(idColumn));
        }
        idColumn = c.findColumn(AbstractDb.OutsColumns.OUT_SCRIPT);
        if (idColumn != -1) {
            outItem.setOutScript(Base58.decode(c.getString(idColumn)));
        }
        idColumn = c.findColumn(AbstractDb.OutsColumns.OUT_VALUE);
        if (idColumn != -1) {
            outItem.setOutValue(c.getLong(idColumn));
        }
        idColumn = c.findColumn(AbstractDb.OutsColumns.OUT_STATUS);
        if (idColumn != -1) {
            outItem.setOutStatus(Out.getOutStatus(c.getInt(idColumn)));
        }
        idColumn = c.findColumn(AbstractDb.OutsColumns.OUT_ADDRESS);
        if (idColumn != -1) {
            outItem.setOutAddress(c.getString(idColumn));
        }
        return outItem;
    }
}


































