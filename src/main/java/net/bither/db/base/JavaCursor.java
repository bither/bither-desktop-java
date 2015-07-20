package net.bither.db.base;

import net.bither.bitherj.db.imp.base.ICursor;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JavaCursor implements ICursor {
    private ResultSet rs;

    public JavaCursor(ResultSet rs) {
        this.rs = rs;
    }

    @Override
    public int getCount() {
        try {
            return rs.getRow();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public boolean move(int var1) {
        try {
            return rs.absolute(var1);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean moveToPosition(int var1) {
        try {
            return rs.absolute(var1);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean moveToFirst() {
        try {
            return rs.first();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean moveToLast() {
        try {
            return rs.last();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean moveToNext() {
        try {
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean moveToPrevious() {
        try {
            return rs.previous();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isFirst() {
        try {
            return rs.isFirst();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isLast() {
        try {
            return rs.isLast();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isBeforeFirst() {
        try {
            return rs.isBeforeFirst();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isAfterLast() {
        try {
            return rs.isAfterLast();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int getColumnIndex(String var1) {
        try {
            return rs.findColumn(var1);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int getColumnIndexOrThrow(String var1) throws IllegalArgumentException {
        try {
            return rs.findColumn(var1);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String getColumnName(int var1) {
        return null;
    }

    @Override
    public String[] getColumnNames() {
        return new String[0];
    }

    @Override
    public int getColumnCount() {
        return 0;
    }

    @Override
    public byte[] getBlob(int var1) {
        try {
            return rs.getBytes(var1 + 1);
        } catch (SQLException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    public String getString(int var1) {
        try {
            return rs.getString(var1 + 1);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public short getShort(int var1) {
        try {
            return rs.getShort(var1 + 1);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int getInt(int var1) {
        try {
            return rs.getInt(var1 + 1);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public long getLong(int var1) {
        try {
            return rs.getLong(var1 + 1);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public float getFloat(int var1) {
        try {
            return rs.getFloat(var1 + 1);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public double getDouble(int var1) {
        try {
            return rs.getDouble(var1 + 1);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int getType(int var1) {
        try {
            return rs.getType();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean isNull(int var1) {
        try {
            return rs.wasNull();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void close() {
        try {
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isClosed() {
        try {
            return rs.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
