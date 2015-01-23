package net.bither.db;

import net.bither.bitherj.db.AbstractDb;

import java.io.File;
import java.sql.*;

public class BitherDBHelper {

    public interface IExecuteDB {
        public void execute(Connection conn) throws SQLException;
    }

    private static final String DB_NAME = "bither.db";

    private static Connection conn;
    private String dbFileFullName;
    private String connectionString;

    public BitherDBHelper(String dbDir) {
        this.dbFileFullName = dbDir + File.separator + DB_NAME;
        this.connectionString = "jdbc:sqlite:" + dbFileFullName;
    }

    public void initDb() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        try {
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
        } catch (SQLException e) {
            File file = new File(dbFileFullName);
            if (file.exists()) {
                file.delete();
            }
            e.printStackTrace();
        }
    }

    public ResultSet query(String sql, String[] arg) {

        ResultSet rs = null;
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            if (arg != null) {
                for (int i = 0; i < arg.length; i++) {
                    stmt.setString(i + 1, arg[i]);
                }
            }
            rs = stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    public boolean executeUpdate(String sql, String[] arg) {

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            if (arg != null) {
                for (int i = 0; i < arg.length; i++) {
                    stmt.setString(i + 1, arg[i]);
                }
            }
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean executeUpdate(IExecuteDB executeDB) {

        try {

            if (executeDB != null) {
                executeDB.execute(conn);
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void close() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}

