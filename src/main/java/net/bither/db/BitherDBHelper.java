package net.bither.db;

import net.bither.bitherj.db.AbstractDb;

import java.sql.*;

public class BitherDBHelper extends AbstractDBHelper {

    private static final String DB_NAME = "bither.db";

    public BitherDBHelper(String dbDir) {
        super(dbDir);
    }

    @Override
    protected String getDBName() {
        return DB_NAME;
    }

    @Override
    protected void createTables(Connection conn) throws SQLException {
        conn = DriverManager.getConnection(this.connectionString, null, null);
        conn.setAutoCommit(false);
        Statement stmt = conn.createStatement();
        ResultSet rsTables = conn.getMetaData().getTables(null, null, AbstractDb.Tables.BLOCKS, null);
        if (!rsTables.next()) {
            stmt.executeUpdate(AbstractDb.CREATE_BLOCKS_SQL);
            stmt.executeUpdate(AbstractDb.CREATE_BLOCK_NO_INDEX);
            stmt.executeUpdate(AbstractDb.CREATE_BLOCK_PREV_INDEX);
            stmt.executeUpdate(AbstractDb.CREATE_PEER_SQL);
            stmt.executeUpdate(AbstractDb.CREATE_ADDRESSTXS_SQL);
            stmt.executeUpdate(AbstractDb.CREATE_TXS_SQL);
            stmt.executeUpdate(AbstractDb.CREATE_INS_SQL);
            stmt.executeUpdate(AbstractDb.CREATE_OUTS_SQL);
            conn.commit();
        }
    }
}

