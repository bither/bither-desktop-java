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

import net.bither.bitherj.core.Block;
import net.bither.bitherj.utils.Utils;
import net.bither.utils.DateUtils;
import net.bither.utils.WalletUtils;

import javax.swing.table.AbstractTableModel;
import java.util.Date;
import java.util.List;

public class BlockTableModel extends AbstractTableModel {

    private List<Block> blockList;

    public BlockTableModel(List<Block> blocks) {
        this.blockList = blocks;

    }

    @Override
    public int getRowCount() {
        return this.blockList.size();
    }

    @Override
    public Object getValueAt(int i, int i2) {
        Block block = this.blockList.get(i);
        switch (i2) {
            case 0:
                return Integer.toString(block.getBlockNo());
            case 1:
                final long timeMs = block.getBlockTime()
                        * DateUtils.SECOND_IN_MILLIS;
                return DateUtils.dateToRelativeTime(new Date(timeMs));

            case 2:
                return WalletUtils.formatHash(
                        Utils.bytesToHexString(Utils.reverseBytes(block.getBlockHash())), 8, 0, ' ');
        }


        return "";
    }


    @Override
    public int getColumnCount() {
        return 3;
    }
}
