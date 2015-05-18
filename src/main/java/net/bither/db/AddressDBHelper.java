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

import net.bither.bitherj.db.AbstractDb;
import net.bither.preference.UserPreference;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AddressDBHelper extends AbstractDBHelper {

    private static final String DB_NAME = "address.db";
    private static final int CURRENT_VERSION = 3;

    public AddressDBHelper(String dbDir) {
        super(dbDir);
    }

    @Override
    protected String getDBName() {
        return DB_NAME;
    }

    @Override
    protected int currentVersion() {
        return CURRENT_VERSION;
    }

    @Override
    protected int dbVersion() {
        int dbVersion = UserPreference.getInstance().getAddressDbVersion();
        if (dbVersion == 0) {
            //no record dbversion is 1
            try {
                Connection connection = getConn();
                assert connection != null;
                if (hasAddressTables(connection)) {
                    dbVersion = 1;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return dbVersion;
    }

    @Override
    protected void onUpgrade(Connection conn, int newVersion, int oldVerion) throws SQLException {
        Statement stmt = conn.createStatement();
        switch (oldVerion) {
            case 1:
                v1Tov2(stmt);
            case 2:
                v2ToV3(stmt);

        }
        conn.commit();
        stmt.close();
        UserPreference.getInstance().setAddressDbVersion(CURRENT_VERSION);
    }

    @Override
    protected void onCreate(Connection conn) throws SQLException {

        if (hasAddressTables(conn)) {
            return;
        }
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(AbstractDb.CREATE_ADDRESSES_SQL);
        stmt.executeUpdate(AbstractDb.CREATE_HDM_BID_SQL);
        stmt.executeUpdate(AbstractDb.CREATE_HD_SEEDS_SQL);
        stmt.executeUpdate(AbstractDb.CREATE_HDM_ADDRESSES_SQL);
        stmt.executeUpdate(AbstractDb.CREATE_PASSWORD_SEED_SQL);
        stmt.executeUpdate(AbstractDb.CREATE_ALIASES_SQL);
        stmt.executeUpdate(AbstractDb.CREATE_VANITY_ADDRESS_SQL);
        stmt.executeUpdate(AbstractDb.CREATE_HD_ACCOUNT);
        conn.commit();
        stmt.close();
        UserPreference.getInstance().setAddressDbVersion(CURRENT_VERSION);

    }

    //v1.3.4
    private void v1Tov2(Statement statement) throws SQLException {
        statement.executeUpdate(AbstractDb.CREATE_HD_ACCOUNT);

    }

    //1.3.5
    private void v2ToV3(Statement statement) throws SQLException {
        statement.executeUpdate(AbstractDb.CREATE_VANITY_ADDRESS_SQL);
    }

    private boolean hasAddressTables(Connection conn) throws SQLException {
        ResultSet rs = conn.getMetaData().getTables(null, null, AbstractDb.Tables.Addresses, null);
        boolean hasTable = rs.next();
        rs.close();
        return hasTable;

    }


}
