package net.bither.db.base;

import com.google.common.base.Function;
import net.bither.bitherj.db.imp.base.ICursor;
import net.bither.bitherj.db.imp.base.IDb;
import net.bither.db.AbstractDBHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
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

    @Override
    public void execUpdate(String sql, String[] params) {
        try {
            PreparedStatement statement = this.getConnection().prepareStatement(sql);
            if (params != null && params.length > 0) {
                for (int i = 1; i <= params.length; i++) {
                    statement.setString(i, params[i - 1]);
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
            PreparedStatement preparedStatement = this.getConnection().prepareStatement(sql);
            if (params != null && params.length > 0) {
                for (int i = 1; i <= params.length; i++) {
                    preparedStatement.setString(i, params[i - 1]);
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
            PreparedStatement preparedStatement = this.getConnection().prepareStatement(sql);
            if (params != null && params.length > 0) {
                for (int i = 1; i <= params.length; i++) {
                    preparedStatement.setString(i, params[i - 1]);
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

    public Connection getConnection() {
        return connection;
    }
}
