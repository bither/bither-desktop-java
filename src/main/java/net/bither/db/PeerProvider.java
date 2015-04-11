/*
* Copyright 2014 http://Bither.net
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package net.bither.db;

import net.bither.ApplicationInstanceManager;
import net.bither.bitherj.core.Peer;
import net.bither.bitherj.db.AbstractDb;
import net.bither.bitherj.db.IPeerProvider;
import net.bither.bitherj.utils.Utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PeerProvider implements IPeerProvider {
    private static final String insertPeerSql = "insert into peers " +
            "(peer_address,peer_port,peer_services,peer_timestamp,peer_connected_cnt)" +
            " values (?,?,?,?,?) ";

    private static PeerProvider peerProvider = new PeerProvider(ApplicationInstanceManager.txDBHelper);

    public static PeerProvider getInstance() {
        return peerProvider;
    }

    private BitherDBHelper mDb;

    public PeerProvider(BitherDBHelper db) {
        this.mDb = db;
    }

    public List<Peer> getAllPeers() {
        List<Peer> peers = new ArrayList<Peer>();
        String sql = "select * from peers";
        try {
            ResultSet c = this.mDb.query(sql, null);
            while (c.next()) {
                Peer peer = applyCursor(c);
                if (peer != null) {
                    peers.add(peer);
                }
            }
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return peers;

    }

    public void deletePeersNotInAddresses(List<InetAddress> peerAddrsses) {
        final List<Long> needDeletePeers = new ArrayList<Long>();
        String sql = "select peer_address from peers";
        ResultSet c = this.mDb.query(sql, null);
        try {
            while (c.next()) {
                int idColumn = c.findColumn(AbstractDb.PeersColumns.PEER_ADDRESS);
                if (idColumn != -1) {
                    long peerAddress = c.getLong(idColumn);
                    boolean in = false;
                    for (InetAddress a : peerAddrsses) {
                        if (Utils.parseLongFromAddress(a) == peerAddress) {
                            in = true;
                            break;
                        }
                    }
                    if (!in) {
                        needDeletePeers.add(peerAddress);
                    }
                }

            }
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            this.mDb.getConn().setAutoCommit(false);
            for (long i : needDeletePeers) {
                PreparedStatement preparedStatement = this.mDb.getConn().prepareStatement("delete peers where peer_address=?");
                preparedStatement.setLong(1, i);

            }
            this.mDb.getConn().commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public ArrayList<InetAddress> exists(ArrayList<InetAddress> peerAddresses) {
        ArrayList<InetAddress> exists = new ArrayList<InetAddress>();
        List<Peer> peerItemList = getAllPeers();
        for (Peer item : peerItemList) {
            if (peerAddresses.contains(item.getPeerAddress())) {
                exists.add(item.getPeerAddress());
            }
        }
        return exists;
    }


    public synchronized void addPeers(List<Peer> items) {
        final List<Peer> addItems = new ArrayList<Peer>();
        List<Peer> allItems = getAllPeers();
        for (Peer peerItem : items) {
            if (!allItems.contains(peerItem) && !addItems.contains(peerItem)) {
                addItems.add(peerItem);
            }
        }
        if (addItems.size() > 0) {
            try {
                this.mDb.getConn().setAutoCommit(false);
                for (Peer item : addItems) {
                    PreparedStatement preparedStatement = this.mDb.getConn().prepareStatement(insertPeerSql);
                    preparedStatement.setLong(1, Utils.parseLongFromAddress(item
                            .getPeerAddress()));
                    preparedStatement.setLong(2, item.getPeerPort());
                    preparedStatement.setLong(3, item.getPeerServices());
                    preparedStatement.setLong(4, item.getPeerTimestamp());
                    preparedStatement.setLong(5, item.getPeerConnectedCnt());
                    preparedStatement.executeUpdate();
                }
                this.mDb.getConn().commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }


    public void removePeer(InetAddress address) {
        try {
            if (this.mDb.getConn() != null && !this.mDb.getConn().isClosed()) {
                this.mDb.executeUpdate("delete from peers where peer_address = ?", new String[]{Long.toString(Utils.parseLongFromAddress
                        (address))});

//TODO Database synchronization is wrong
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void conncetFail(InetAddress address) {
        try {
            long addressLong = Utils.parseLongFromAddress(address);
            String sql = "select count(0) cnt from peers where peer_address=? and peer_connected_cnt=0";

            ResultSet c = this.mDb.query(sql, new String[]{Long.toString(addressLong)});
            int cnt = 0;
            if (c.next()) {
                int idColumn = c.findColumn("cnt");
                if (idColumn != -1) {
                    cnt = c.getInt(idColumn);
                }

            }
            c.close();

            if (cnt == 0) {
                sql = "update peers set peer_connected_cnt=peer_connected_cnt+1 where peer_address="
                        + Long.toString(addressLong);
                this.mDb.executeUpdate(sql, null);
            } else {
                sql = "update peers set peer_connected_cnt=2 where peer_address=" + Long.toString
                        (addressLong);
                this.mDb.executeUpdate(sql, null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void connectSucceed(InetAddress address) {
        long addressLong = Utils.parseLongFromAddress(address);
        this.mDb.executeUpdate("update peers  set peer_connected_cnt=1 , peer_timestamp=? where peer_address=?",
                new String[]{Long.toString(new Date().getTime()), Long.toString(addressLong)});
    }

    public List<Peer> getPeersWithLimit(int limit) {
        List<Peer> peerItemList = new ArrayList<Peer>();
        String sql = "select * from peers order by peer_address limit ?";
        try {
            ResultSet c = this.mDb.query(sql, new String[]{Integer.toString(limit)});
            while (c.next()) {
                Peer peer = applyCursor(c);
                if (peer != null) {
                    peerItemList.add(peer);
                }

            }
            c.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return peerItemList;
    }

    public void cleanPeers() {
        try {
            int maxPeerSaveCnt = 1000;
            String disconnectingPeerCntSql = "select count(0) cnt from peers where " +
                    "peer_connected_cnt<>1";
            int disconnectingPeerCnt = 0;

            ResultSet c = this.mDb.query(disconnectingPeerCntSql, null);
            if (c.next()) {
                int idColumn = c.findColumn("cnt");
                if (idColumn != -1) {
                    disconnectingPeerCnt = c.getInt(idColumn);
                }
            }
            c.close();
            if (disconnectingPeerCnt > maxPeerSaveCnt) {
                String sql = "select peer_timestamp from peers where peer_connected_cnt<>1 " +
                        "order by peer_timestamp desc limit 1 offset ? ";
                c = this.mDb.query(sql, new String[]{Integer.toString(maxPeerSaveCnt)});
                long timestamp = 0;
                if (c.next()) {
                    int idColumn = c.findColumn(AbstractDb.PeersColumns.PEER_TIMESTAMP);
                    if (idColumn != -1) {
                        timestamp = c.getLong(idColumn);
                    }
                }
                c.close();
                if (timestamp > 0) {
                    mDb.executeUpdate("delete peers where peer_connected_cnt<>1 and peer_timestamp<=?"
                            , new String[]{Long.toString(timestamp)});
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteUnknowHost(long address) {
        this.mDb.executeUpdate("delete peers where peer_address=?"
                , new String[]{Long.toString(address)});

    }

    private Peer applyCursor(ResultSet rs) throws SQLException {
        InetAddress address = null;
        int idColumn = rs.findColumn(AbstractDb.PeersColumns.PEER_ADDRESS);
        if (idColumn != -1) {
            long addressLong = rs.getLong(idColumn);
            try {
                address = Utils.parseAddressFromLong(addressLong);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                deleteUnknowHost(addressLong);
            }
        }
        if (address == null) {
            return null;
        }
        Peer peerItem = new Peer(address);
        idColumn = rs.findColumn(AbstractDb.PeersColumns.PEER_CONNECTED_CNT);
        if (idColumn != -1) {
            peerItem.setPeerConnectedCnt(rs.getInt(idColumn));
        }
        idColumn = rs.findColumn(AbstractDb.PeersColumns.PEER_PORT);
        if (idColumn != -1) {
            peerItem.setPeerPort(rs.getInt(idColumn));
        }
        idColumn = rs.findColumn(AbstractDb.PeersColumns.PEER_SERVICES);
        if (idColumn != -1) {
            peerItem.setPeerServices(rs.getLong(idColumn));
        }
        idColumn = rs.findColumn(AbstractDb.PeersColumns.PEER_TIMESTAMP);
        if (idColumn != -1) {
            peerItem.setPeerTimestamp(rs.getInt(idColumn));
        }
        return peerItem;

    }

    public void recreate() {
        try {
            Statement stmt = this.mDb.getConn().createStatement();
            stmt.executeUpdate("drop table " + AbstractDb.Tables.PEERS + ";");
            stmt.executeUpdate(AbstractDb.CREATE_PEER_SQL);
            this.mDb.getConn().commit();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
