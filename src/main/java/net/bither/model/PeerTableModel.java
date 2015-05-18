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

package net.bither.model;

import net.bither.bitherj.core.Peer;
import net.bither.bitherj.utils.Utils;
import net.bither.utils.LocaliserUtils;

import javax.swing.table.AbstractTableModel;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class PeerTableModel extends AbstractTableModel {
    private final Map<InetAddress, String> hostnames = new WeakHashMap<InetAddress, String>();

    private List<Peer> peerList;

    public PeerTableModel(List<Peer> peers) {
        this.peerList = peers;

    }

    @Override
    public int getRowCount() {
        return this.peerList.size();
    }

    @Override
    public Object getValueAt(int i, int i2) {
        Peer peer = this.peerList.get(i);
        switch (i2) {
            case 0:
                final InetAddress address = peer.getAddress().getAddr();
                final String hostname = hostnames.get(address);
                return hostname != null ? hostname : address
                        .getHostAddress();
            case 1:
                final long bestHeight = peer.getDisplayLastBlockHeight();
                return bestHeight > 0 ? bestHeight + " blocks"
                        : null;

            case 2:
                return peer.getSubVersion();
            case 3:
                return "protocol: " + peer.getClientVersion();
            case 4:
                final long pingTime = peer.pingTime;
                return pingTime < Long.MAX_VALUE ? Utils.format(LocaliserUtils.getString("peer_list_row_ping_time"), pingTime) : null;
        }


        return "";
    }


    @Override
    public int getColumnCount() {
        return 5;
    }
}
