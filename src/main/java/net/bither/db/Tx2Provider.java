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

import com.google.common.base.Function;
import net.bither.ApplicationInstanceManager;
import net.bither.bitherj.core.In;
import net.bither.bitherj.core.Out;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.db.imp.AbstractTxProvider;
import net.bither.bitherj.db.imp.base.ICursor;
import net.bither.bitherj.db.imp.base.IDb;
import net.bither.bitherj.utils.Base58;
import net.bither.bitherj.utils.Utils;
import net.bither.db.base.JavaCursor;
import net.bither.db.base.JavaDb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Tx2Provider extends AbstractTxProvider {

    private static Tx2Provider txProvider = new Tx2Provider(ApplicationInstanceManager.txDBHelper);

    public static Tx2Provider getInstance() {
        return txProvider;
    }

    private TxDBHelper helper;

    public Tx2Provider(TxDBHelper helper) {
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

    String txInsertSql = "insert into txs " +
            "(tx_hash,tx_ver,tx_locktime,tx_time,block_no,source)" +
            " values (?,?,?,?,?,?) ";
    String inInsertSql = "insert into ins " +
            "(tx_hash,in_sn,prev_tx_hash,prev_out_sn,in_signature,in_sequence)" +
            " values (?,?,?,?,?,?) ";
    String outInsertSql = "insert into outs " +
            "(tx_hash,out_sn,out_script,out_value,out_status,out_address,hd_account_id)" +
            " values (?,?,?,?,?,?,?)";

    @Override
    protected void insertTxToDb(IDb db, Tx tx) {
        String blockNoString = null;
        if (tx.getBlockNo() != Tx.TX_UNCONFIRMED) {
            blockNoString = Integer.toString(tx.getBlockNo());
        }
        this.execUpdate(db, txInsertSql, new String[] {
                Base58.encode(tx.getTxHash()),
                Long.toString(tx.getTxVer()),
                Long.toString(tx.getTxLockTime()),
                Long.toString(tx.getTxTime()),
                blockNoString,
                Integer.toString(tx.getSource())
        });
    }

    @Override
    protected void insertInToDb(IDb db, In in) {
        String signatureString = null;
        if (in.getInSignature() != null) {
            signatureString = Base58.encode(in.getInSignature());
        }
        this.execUpdate(db, inInsertSql, new String[]{
                Base58.encode(in.getTxHash()),
                Integer.toString(in.getInSn()),
                Base58.encode(in.getPrevTxHash()),
                Integer.toString(in.getPrevOutSn()),
                signatureString,
                Long.toString(in.getInSequence())
        });
    }

    @Override
    protected void insertOutToDb(IDb db, Out out) {
        String outAddress = null;
        if (!Utils.isEmpty(out.getOutAddress())) {
            outAddress = out.getOutAddress();
        }
        this.execUpdate(db, outInsertSql, new String[] {Base58.encode(out.getTxHash())
                , Integer.toString(out.getOutSn())
                , Base58.encode(out.getOutScript())
                , Long.toString(out.getOutValue())
                , Integer.toString(out.getOutStatus().getValue())
                , outAddress
                , Integer.toString(out.getHDAccountId())});
    }
}
