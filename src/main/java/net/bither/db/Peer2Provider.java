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
import net.bither.bitherj.core.Peer;
import net.bither.bitherj.db.imp.AbstractPeerProvider;
import net.bither.bitherj.db.imp.base.IDb;
import net.bither.bitherj.utils.Utils;
import net.bither.db.base.JavaDb;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Peer2Provider extends AbstractPeerProvider {

    private static Peer2Provider peerProvider = new Peer2Provider(ApplicationInstanceManager.txDBHelper);

    public static Peer2Provider getInstance() {
        return peerProvider;
    }

    private TxDBHelper helper;

    public Peer2Provider(TxDBHelper helper) {
        this.helper = helper;
    }

    @Override
    public IDb getReadDb() {
        return new JavaDb(this.helper.getConn());
    }

    @Override
    public IDb getWriteDb() {
        return new JavaDb(this.helper.getConn());
    }

//    @Override
//    public void addPeers(List<Peer> items) {
//        List<Peer> addItems = new ArrayList<Peer>();
//        List<Peer> allItems = getAllPeers();
//        for (Peer peerItem : items) {
//            if (!allItems.contains(peerItem) && !addItems.contains(peerItem)) {
//                addItems.add(peerItem);
//            }
//        }
//        if (addItems.size() > 0) {
//            String sql = "insert into peers(peer_address,peer_port,peer_services,peer_timestamp,peer_connected_cnt) values(?,?,?,?,?)";
//            IDb writeDb = this.getWriteDb();
//            writeDb.beginTransaction();
//            for (Peer item : addItems) {
//                try {
//                    PreparedStatement statement = ((JavaDb)this.getWriteDb()).getConnection().prepareStatement(sql);
//                    statement.setLong(1, Utils.parseLongFromAddress(item.getPeerAddress()));
//                    statement.setString(2, "8333");
//                    statement.setLong(3, item.getPeerServices());
//                    statement.setLong(4, item.getPeerTimestamp());
//                    statement.setInt(5, item.getPeerConnectedCnt());
////                    if (params != null && params.length > 0) {
////                        for (int i = 1; i <= params.length; i++) {
////                            statement.setString(1, params[i - 1]);
////                        }
////                    }
//                    statement.executeUpdate();
//                    statement.close();
//                } catch (SQLException e) {
//
//                }
//
////                this.execUpdate(writeDb, sql, new String[]{
////                        Long.toString(Utils.parseLongFromAddress(item.getPeerAddress()))
////                        , Integer.toString(item.getPeerPort())
////                        , Long.toString(item.getPeerServices())
////                        , Integer.toString(item.getPeerTimestamp())
////                        , Integer.toString(item.getPeerConnectedCnt())});
//            }
//            writeDb.endTransaction();
//        }
//    }
}
