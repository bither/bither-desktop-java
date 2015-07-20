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

    @Override
    public void execUpdate(String sql, String[] params) {
        try {
            PreparedStatement statement = ((JavaDb)this.getWriteDb()).getConnection().prepareStatement(sql);
            if (params != null && params.length > 0) {
                for (int i = 1; i <= params.length; i++) {
                    statement.setString(1, params[i - 1]);
                }
            }
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execQueryOneRecord(String sql, String[] params, Function<ICursor, Void> func) {
        try {
            PreparedStatement preparedStatement = ((JavaDb)this.getReadDb()).getConnection().prepareStatement(sql);
            if (params != null && params.length > 0) {
                for (int i = 1; i <= params.length; i++) {
                    preparedStatement.setString(1, params[i - 1]);
                }
            }
            ICursor c = new JavaCursor(preparedStatement.executeQuery());
            if (c.moveToNext()) {
                func.apply(c);
            }
            c.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execQueryLoop(String sql, String[] params, Function<ICursor, Void> func) {
        try {
            PreparedStatement preparedStatement = ((JavaDb)this.getReadDb()).getConnection().prepareStatement(sql);
            if (params != null && params.length > 0) {
                for (int i = 1; i <= params.length; i++) {
                    preparedStatement.setString(1, params[i - 1]);
                }
            }
            ICursor c = new JavaCursor(preparedStatement.executeQuery());
            while (c.moveToNext()) {
                func.apply(c);
            }
            c.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execUpdate(IDb db, String sql, String[] params) {
        try {
            PreparedStatement statement = ((JavaDb)db).getConnection().prepareStatement(sql);
            if (params != null && params.length > 0) {
                for (int i = 1; i <= params.length; i++) {
                    statement.setString(1, params[i - 1]);
                }
            }
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execQueryOneRecord(IDb db, String sql, String[] params, Function<ICursor, Void> func) {
        try {
            PreparedStatement preparedStatement = ((JavaDb)db).getConnection().prepareStatement(sql);
            if (params != null && params.length > 0) {
                for (int i = 1; i <= params.length; i++) {
                    preparedStatement.setString(1, params[i - 1]);
                }
            }
            ICursor c = new JavaCursor(preparedStatement.executeQuery());
            if (c.moveToNext()) {
                func.apply(c);
            }
            c.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execQueryLoop(IDb db, String sql, String[] params, Function<ICursor, Void> func) {
        try {
            PreparedStatement preparedStatement = ((JavaDb)db).getConnection().prepareStatement(sql);
            if (params != null && params.length > 0) {
                for (int i = 1; i <= params.length; i++) {
                    preparedStatement.setString(1, params[i - 1]);
                }
            }
            ICursor c = new JavaCursor(preparedStatement.executeQuery());
            while (c.moveToNext()) {
                func.apply(c);
            }
            c.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
