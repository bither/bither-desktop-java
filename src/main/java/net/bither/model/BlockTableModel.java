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
