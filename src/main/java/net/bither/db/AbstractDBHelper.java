package net.bither.db;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractDBHelper {

    public interface IExecuteDB {
        public void execute(Connection conn) throws SQLException;
    }

    private static Connection conn;

    private String dbFileFullName;
    protected String connectionString;

    public AbstractDBHelper(String dbDir) {
        this.dbFileFullName = dbDir + File.separator + getDBName();
        this.connectionString = "jdbc:sqlite:" + dbFileFullName;
    }


    protected abstract String getDBName();

    protected abstract void createTables(Connection conn) throws SQLException;

    public void initDb() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        try {
            createTables(conn);
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
