package net.bither.db.base;

import net.bither.bitherj.db.imp.base.IDb;
import net.bither.db.AbstractDBHelper;

import java.sql.Connection;
import java.sql.SQLException;

public class JavaDb implements IDb {
    private Connection connection;

    public JavaDb(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void beginTransaction() {
        try {
            this.connection.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endTransaction() {
        try {
            this.connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
