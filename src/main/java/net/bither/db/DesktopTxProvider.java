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

import net.bither.ApplicationInstanceManager;
import net.bither.bitherj.core.DesktopHDMAddress;
import net.bither.bitherj.db.AbstractDb;
import net.bither.bitherj.db.IDesktopTxProvider;
import net.bither.bitherj.utils.Base58;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DesktopTxProvider implements IDesktopTxProvider {

    private static DesktopTxProvider enDesktopTxProvider =
            new DesktopTxProvider(ApplicationInstanceManager.txDBHelper);

    private TxDBHelper mDb;

    public static DesktopTxProvider getInstance() {
        return enDesktopTxProvider;
    }

    private DesktopTxProvider(TxDBHelper db) {
        this.mDb = db;
    }


    private static final String insert_hdm_address_sql = "insert into desktop_hdm_account_addresses " +
            "(path_type,address_index,is_issued,address,pub_key_1,pub_key_2,pub_key_3,is_synced)" +
            " values (?,?,?,?,?,?,?,?) ";


    public void addAddress(List<DesktopHDMAddress> addressList) {
        try {
            this.mDb.getConn().setAutoCommit(false);
            for (DesktopHDMAddress address : addressList) {
                PreparedStatement stmt = this.mDb.getConn().prepareStatement(insert_hdm_address_sql);
                String[] params = new String[]{Integer.toString(address.getPathType().getValue()),
                        Integer.toString(address.getIndex()), Integer.toString(address.isIssued() ? 1 : 0),
                        address.getAddress(), Base58.encode(address.getPubHot()), Base58.encode(address.getPubRemote())
                        , Base58.encode(address.getPubCold()), Integer.toString(address.isSyncComplete() ? 1 : 0)
                };
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        stmt.setString(i + 1, params[i]);
                    }
                }
                stmt.executeUpdate();
                stmt.close();
            }
            this.mDb.getConn().commit();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public int maxHDMAddressPubIndex() {
        int maxIndex = -1;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement("select ifnull(max(address_index),-1)  address_index from desktop_hdm_account_addresses ", null);
            ResultSet cursor = statement.executeQuery();

            if (cursor.next()) {
                int idColumn = cursor.findColumn(AbstractDb.HDMAddressesColumns.HD_SEED_INDEX);
                if (idColumn != -1) {
                    maxIndex = cursor.getInt(idColumn);
                }
            }
            cursor.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maxIndex;
    }


}
