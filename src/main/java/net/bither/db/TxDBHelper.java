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

public class TxDBHelper extends AbstractDBHelper {

    private static final String DB_NAME = "bither.db";
    private static final int CURRENT_VERSION = 3;

    public static final String CREATE_ENTERPRISE_HDM_ADDRESSES = "create table if not exists hd_account_addresses " +
            "(path_type integer not null" +
            ", address_index integer not null" +
            ", is_issued integer not null" +
            ", address text not null" +
            ", pub_key_1 text not null" +
            ", pub_key_2 text not null" +
            ", pub_key_3 text not null" +
            ", is_synced integer not null" +
            ", primary key (address));";

    public static final String ADD_ENTERPRISE_HD_ACCOUNT_ID_FOR_OUTS = "alter table outs add column enterprise_hd_account_id integer;";

    public TxDBHelper(String dbDir) {
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
        int dbVersion = UserPreference.getInstance().getTxDbVersion();
        if (dbVersion == 0) {
            //no record dbversion is 1
            try {
                Connection connection = getConn();
                assert connection != null;
                if (hasTxTables(connection)) {
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
                v1ToV2(stmt);
            case 2:
                v2Tov3(stmt);

        }
        conn.commit();
        stmt.close();
        UserPreference.getInstance().setTxDbVersion(CURRENT_VERSION);
    }

    @Override
    protected void onCreate(Connection conn) throws SQLException {
        if (hasTxTables(conn)) {
            return;
        }
        Statement stmt = conn.createStatement();
        createBlocksTable(stmt);

        createPeersTable(stmt);

        createAddressTxsTable(stmt);

        createTxsTable(stmt);
        createOutsTable(stmt);
        createInsTable(stmt);

        createHDAccountAddress(stmt);

        stmt.executeUpdate(CREATE_ENTERPRISE_HDM_ADDRESSES);
        stmt.executeUpdate(ADD_ENTERPRISE_HD_ACCOUNT_ID_FOR_OUTS);

        conn.commit();
        stmt.close();
        UserPreference.getInstance().setTxDbVersion(CURRENT_VERSION);
    }


    private void createBlocksTable(Statement stmt) throws SQLException {
        stmt.executeUpdate(AbstractDb.CREATE_BLOCKS_SQL);
        stmt.executeUpdate(AbstractDb.CREATE_BLOCK_NO_INDEX);
        stmt.executeUpdate(AbstractDb.CREATE_BLOCK_PREV_INDEX);
    }

    private void createTxsTable(Statement stmt) throws SQLException {
        stmt.executeUpdate(AbstractDb.CREATE_TXS_SQL);
        stmt.executeUpdate(AbstractDb.CREATE_TX_BLOCK_NO_INDEX);
    }

    private void createAddressTxsTable(Statement stmt) throws SQLException {
        stmt.executeUpdate(AbstractDb.CREATE_ADDRESSTXS_SQL);
    }

    private void createInsTable(Statement stmt) throws SQLException {
        stmt.executeUpdate(AbstractDb.CREATE_INS_SQL);
        stmt.executeUpdate(AbstractDb.CREATE_IN_PREV_TX_HASH_INDEX);
    }

    private void createOutsTable(Statement stmt) throws SQLException {
        stmt.executeUpdate(AbstractDb.CREATE_OUTS_SQL);
        stmt.executeUpdate(AbstractDb.CREATE_OUT_OUT_ADDRESS_INDEX);
    }

    private void createPeersTable(Statement stmt) throws SQLException {
        stmt.executeUpdate(AbstractDb.CREATE_PEER_SQL);
    }

    private void createHDAccountAddress(Statement stmt) throws SQLException {
        stmt.executeUpdate(AbstractDb.CREATE_HD_ACCOUNT_ADDRESSES);
        stmt.executeUpdate(AbstractDb.CREATE_HD_ACCOUNT_ADDRESS_INDEX);
    }


    private void v1ToV2(Statement stmt) throws SQLException {
        stmt.executeUpdate(AbstractDb.ADD_HD_ACCOUNT_ID_FOR_OUTS);
        createHDAccountAddress(stmt);

    }

    private void v2Tov3(Statement statement) throws SQLException {
        statement.executeUpdate(CREATE_ENTERPRISE_HDM_ADDRESSES);
        statement.executeUpdate(ADD_ENTERPRISE_HD_ACCOUNT_ID_FOR_OUTS);
    }

    public void rebuildTx() {
        try {
            getConn().setAutoCommit(false);
            Statement stmt = getConn().createStatement();


            stmt.executeUpdate("drop table " + AbstractDb.Tables.TXS + ";");
            stmt.executeUpdate("drop table " + AbstractDb.Tables.OUTS + ";");
            stmt.executeUpdate("drop table " + AbstractDb.Tables.INS + ";");
            stmt.executeUpdate("drop table " + AbstractDb.Tables.ADDRESSES_TXS + ";");
            stmt.executeUpdate("drop table " + AbstractDb.Tables.PEERS + ";");

            stmt.executeUpdate(AbstractDb.CREATE_TXS_SQL);
            stmt.executeUpdate(AbstractDb.CREATE_OUTS_SQL);
            stmt.executeUpdate(AbstractDb.CREATE_INS_SQL);
            stmt.executeUpdate(AbstractDb.CREATE_ADDRESSTXS_SQL);
            stmt.executeUpdate(AbstractDb.CREATE_PEER_SQL);

            getConn().commit();
            stmt.close();
        } catch (SQLException e) {
            try {
                getConn().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }

    }

    private boolean hasTxTables(Connection conn) throws SQLException {
        ResultSet rs = conn.getMetaData().getTables(null, null, AbstractDb.Tables.TXS, null);
        boolean hasTable = rs.next();
        rs.close();
        return hasTable;

    }


}

