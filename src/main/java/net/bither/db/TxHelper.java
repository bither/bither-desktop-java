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

import net.bither.bitherj.core.In;
import net.bither.bitherj.core.Out;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.db.AbstractDb;
import net.bither.bitherj.exception.AddressFormatException;
import net.bither.bitherj.utils.Base58;
import net.bither.bitherj.utils.Utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class TxHelper {

    public static Tx applyCursor(ResultSet c) throws AddressFormatException, SQLException {
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

    public static In applyCursorIn(ResultSet c) throws AddressFormatException, SQLException {
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

    public static Out applyCursorOut(ResultSet c) throws AddressFormatException, SQLException {
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

    public static void addInsAndOuts(TxDBHelper mDb, Tx txItem) throws AddressFormatException, SQLException {
        String txHashStr = Base58.encode(txItem.getTxHash());
        txItem.setOuts(new ArrayList<Out>());
        txItem.setIns(new ArrayList<In>());
        String sql = "select * from ins where tx_hash=? order by in_sn";
        PreparedStatement statement = mDb.getPreparedStatement(sql, new String[]{txHashStr});
        ResultSet c = statement.executeQuery();
        while (c.next()) {
            In inItem = TxHelper.applyCursorIn(c);
            inItem.setTx(txItem);
            txItem.getIns().add(inItem);
        }
        c.close();
        statement.close();

        sql = "select * from outs where tx_hash=? order by out_sn";
        statement = mDb.getPreparedStatement(sql, new String[]{txHashStr});
        c = statement.executeQuery();
        while (c.next()) {
            Out outItem = TxHelper.applyCursorOut(c);
            outItem.setTx(txItem);
            txItem.getOuts().add(outItem);
        }
        c.close();
        statement.close();
    }


}
