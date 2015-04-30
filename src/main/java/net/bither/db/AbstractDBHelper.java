package net.bither.db;

import java.io.File;
import java.sql.*;

public abstract class AbstractDBHelper {

    private Connection conn;

    private String dbFileFullName;
    protected String connectionString;

    public AbstractDBHelper(String dbDir) {
        this.dbFileFullName = dbDir + File.separator + getDBName();
        this.connectionString = "jdbc:sqlite:" + dbFileFullName;
    }


    protected abstract String getDBName();

    protected abstract int currentVersion();

    protected abstract int dbVersion();

    protected abstract void onCreate(Connection conn) throws SQLException;

    protected abstract void onUpgrade(Connection conn, int newVersion, int oldVerion) throws SQLException;

    public Connection getConn() {
        return conn;
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
            int dbVersion = dbVersion();
            int cuerrentVersion = currentVersion();
            if (dbVersion == 0) {
                onCreate(conn);
            } else if (dbVersion() < cuerrentVersion) {
                onUpgrade(conn, cuerrentVersion, dbVersion);
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


    public void close() {
        try {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
