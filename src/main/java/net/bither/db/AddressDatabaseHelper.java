package net.bither.db;

import net.bither.bitherj.db.AbstractDb;

import java.sql.*;

public class AddressDatabaseHelper extends AbstractDBHelper {

    private static final String DB_NAME = "address.db";

    @Override
    protected String getDBName() {
        return DB_NAME;
    }

    public AddressDatabaseHelper(String dbDir) {
        super(dbDir);
    }

    @Override
    protected void createTables(Connection conn) throws SQLException {
        conn = DriverManager.getConnection(this.connectionString, null, null);
        conn.setAutoCommit(false);
        Statement stmt = conn.createStatement();
        ResultSet rsTables = conn.getMetaData().getTables(null, null, AbstractDb.Tables.Addresses, null);
        if (!rsTables.next()) {
            stmt.executeUpdate(AbstractDb.CREATE_ADDRESSES_SQL);
            stmt.executeUpdate(AbstractDb.CREATE_HDM_BID_SQL);
            stmt.executeUpdate(AbstractDb.CREATE_HD_SEEDS_SQL);
            stmt.executeUpdate(AbstractDb.CREATE_HDM_ADDRESSES_SQL);
            stmt.executeUpdate(AbstractDb.CREATE_PASSWORD_SEED_SQL);
            stmt.executeUpdate(AbstractDb.CREATE_ALIASES_SQL);
            conn.commit();
        }
    }

}
