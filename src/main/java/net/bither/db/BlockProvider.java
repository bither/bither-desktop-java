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
import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.core.Block;
import net.bither.bitherj.db.AbstractDb;
import net.bither.bitherj.db.IBlockProvider;
import net.bither.bitherj.exception.AddressFormatException;
import net.bither.bitherj.utils.Base58;
import net.bither.utils.LogUtil;
import net.bither.utils.StringUtil;
import net.bither.utils.SystemUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockProvider implements IBlockProvider {
    private static final String insertBlockSql = "insert into blocks " +
            "(block_no,block_hash,block_root,block_ver,block_bits,block_nonce,block_time,block_prev,is_main)" +
            " values (?,?,?,?,?,?,?,?,?) ";

    private static BlockProvider blockProvider = new BlockProvider(ApplicationInstanceManager.txDBHelper);

    public static BlockProvider getInstance() {
        return blockProvider;
    }

    private TxDBHelper mDb;


    private BlockProvider(TxDBHelper db) {
        this.mDb = db;
    }

    public List<Block> getAllBlocks() {
        List<Block> blockItems = new ArrayList<Block>();
        String sql = "select * from blocks order by block_no desc";
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, null);
            ResultSet c = statement.executeQuery();
            while (c.next()) {
                blockItems.add(applyCursor(c));
            }
            c.close();
            statement.close();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return blockItems;
    }

    @Override
    public List<Block> getLimitBlocks(int limit) {
        List<Block> blockItems = new ArrayList<Block>();
        String sql = "select * from blocks order by block_no desc  limit ?";


        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{Integer.toString(limit)});
            ResultSet c = statement.executeQuery();
            while (c.next()) {
                blockItems.add(applyCursor(c));
            }
            c.close();
            statement.close();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return blockItems;
    }

    public List<Block> getBlocksFrom(int blockNo) {
        List<Block> blockItems = new ArrayList<Block>();
        String sql = "select * from blocks where block_no>? order by block_no desc";
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{Integer.toString(blockNo)});
            ResultSet c = statement.executeQuery();
            while (c.next()) {
                blockItems.add(applyCursor(c));
            }
            c.close();
            statement.close();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.gc();
        return blockItems;
    }

    public int getBlockCount() {
        String sql = "select count(*) cnt from blocks ";
        int count = 0;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, null);
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                int idColumn = c.findColumn("cnt");
                if (idColumn != -1) {
                    count = c.getInt(idColumn);
                }
            }
            c.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public Block getLastBlock() {
        Block item = null;
        String sql = "select * from blocks where is_main=1 order by block_no desc limit 1";

        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, null);
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                item = applyCursor(c);
            }
            c.close();
            statement.close();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

        }

        return item;
    }

    public Block getLastOrphanBlock() {
        Block item = null;
        String sql = "select * from blocks where is_main=0 order by block_no desc limit 1";
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, null);
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                item = applyCursor(c);
            }
            c.close();
            statement.close();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return item;
    }

    public Block getBlock(byte[] blockHash) {
        Block item = null;
        String sql = "select * from blocks where block_hash=?";
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{Base58.encode(blockHash)});
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                item = applyCursor(c);
            }
            c.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        }
        return item;
    }

    public Block getOrphanBlockByPrevHash(byte[] prevHash) {
        Block item = null;
        String sql = "select * from blocks where block_prev=? and is_main=0";
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{Base58.encode(prevHash)});
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                item = applyCursor(c);
            }
            c.close();
            statement.close();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return item;
    }

    public Block getMainChainBlock(byte[] blockHash) {
        Block item = null;
        String sql = "select * from blocks where block_hash=? and is_main=1";

        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{Base58.encode(blockHash)});
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                item = applyCursor(c);
            }
            c.close();
            statement.close();
        } catch (AddressFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return item;
    }

    public List<byte[]> exists(List<byte[]> blockHashes) {
        List<byte[]> exists = new ArrayList<byte[]>();
        List<Block> blockItems = getAllBlocks();
        for (Block blockItm : blockItems) {
            for (byte[] bytes : exists) {
                if (Arrays.equals(bytes, blockItm.getBlockHash())) {
                    exists.add(bytes);
                    break;
                }
            }
        }
        return exists;
    }

    public boolean isExist(byte[] blockHash) {
        boolean result = false;
        String sql = "select count(0) cnt from blocks where block_hash=?";

        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{Base58.encode(blockHash)});
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                int idColumn = c.findColumn("cnt");
                if (idColumn != -1) {
                    result = (c.getInt(idColumn) == 1);
                }
            }
            c.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void addBlocks(List<Block> blockItemList) {
        final List<Block> addBlockList = new ArrayList<Block>();
        List<Block> allBlockList = getAllBlocks();
        for (Block item : blockItemList) {
            if (!allBlockList.contains(item)) {
                addBlockList.add(item);
            }
        }
        allBlockList.clear();
        try {
            this.mDb.getConn().setAutoCommit(false);
            for (Block item : addBlockList) {
                PreparedStatement preparedStatement = this.mDb.getConn().prepareStatement(insertBlockSql);
                preparedStatement.setInt(1, item.getBlockNo());
                preparedStatement.setString(2, Base58.encode(item.getBlockHash()));
                preparedStatement.setString(3, Base58.encode(item.getBlockRoot()));
                preparedStatement.setLong(4, item.getBlockVer());
                preparedStatement.setLong(5, item.getBlockBits());
                preparedStatement.setLong(6, item.getBlockNonce());
                preparedStatement.setInt(7, item.getBlockTime());
                preparedStatement.setString(8, Base58.encode(item.getBlockPrev()));
                preparedStatement.setInt(9, item.isMain() ? 1 : 0);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
            this.mDb.getConn().commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        LogUtil.printlnOut("addBlocks");
        SystemUtil.callSystemGC();


    }

    public void addBlock(Block item) {
        boolean blockExists = blockExists(item.getBlockHash());
        if (!blockExists) {

            this.mDb.executeUpdate(insertBlockSql, new String[]{Integer.toString(item.getBlockNo()),
                    Base58.encode(item.getBlockHash()), Base58.encode(item.getBlockRoot()), Long.toString(item.getBlockVer())
                    , Long.toString(item.getBlockBits()), Long.toString(item.getBlockNonce()), Integer.toString(item.getBlockTime()), Base58.encode(item.getBlockPrev()), Integer.toString(item.isMain() ? 1 : 0)});
        }
        LogUtil.printlnOut("addBlock");

    }

    public boolean blockExists(byte[] blockHash) {
        String sql = "select count(0) cnt from blocks where block_hash=?";

        int cnt = 0;
        try {
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, new String[]{Base58.encode(blockHash)});
            ResultSet c = statement.executeQuery();
            if (c.next()) {
                int idColumn = c.findColumn("cnt");
                if (idColumn != -1) {
                    cnt = c.getInt(idColumn);
                }
            }
            c.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cnt > 0;
    }

    public void updateBlock(byte[] blockHash, boolean isMain) {
        this.mDb.executeUpdate("update blocks set is_main=? where block_hash=?",
                new String[]{Integer.toString(isMain ? 1 : 0), Base58.encode(blockHash)});
    }

    public void removeBlock(byte[] blockHash) {
        this.mDb.executeUpdate("delete from blocks where block_hash=?", new String[]{Base58.encode(blockHash)});
    }

    public void cleanOldBlock() {
        try {
            String sql = "select count(0) cnt from blocks";
            PreparedStatement statement = this.mDb.getPreparedStatement(sql, null);
            ResultSet c = statement.executeQuery();
            int cnt = 0;
            if (c.next()) {
                int idColumn = c.findColumn("cnt");
                if (idColumn != -1) {
                    cnt = c.getInt(idColumn);
                }
            }
            c.close();
            statement.close();
            if (cnt > 5000) {
                sql = "select max(block_no) max_block_no from blocks where is_main=1";
                statement = this.mDb.getPreparedStatement(sql, null);
                c = statement.executeQuery();
                int maxBlockNo = 0;
                if (c.next()) {
                    int idColumn = c.findColumn("max_block_no");
                    if (idColumn != -1) {
                        maxBlockNo = c.getInt(idColumn);

                    }
                }
                c.close();
                statement.close();
                int blockNo = (maxBlockNo - BitherjSettings.BLOCK_DIFFICULTY_INTERVAL) - maxBlockNo % BitherjSettings.BLOCK_DIFFICULTY_INTERVAL;
                this.mDb.executeUpdate("delete from blocks where block_no<?", new String[]{Integer.toString(blockNo)});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private Block applyCursor(ResultSet rs) throws AddressFormatException, SQLException {
        byte[] blockHash = null;
        long version = 1;
        byte[] prevBlock = null;
        byte[] merkleRoot = null;
        int timestamp = 0;
        long target = 0;
        long nonce = 0;
        int blockNo = 0;
        boolean isMain = false;
        int idColumn = rs.findColumn(AbstractDb.BlocksColumns.BLOCK_BITS);
        if (idColumn != -1) {
            target = rs.getLong(idColumn);
        }
        idColumn = rs.findColumn(AbstractDb.BlocksColumns.BLOCK_HASH);
        if (idColumn != -1) {
            blockHash = Base58.decode(rs.getString(idColumn));
        }
        idColumn = rs.findColumn(AbstractDb.BlocksColumns.BLOCK_NO);
        if (idColumn != -1) {
            blockNo = rs.getInt(idColumn);
        }
        idColumn = rs.findColumn(AbstractDb.BlocksColumns.BLOCK_NONCE);
        if (idColumn != -1) {
            nonce = rs.getLong(idColumn);
        }
        idColumn = rs.findColumn(AbstractDb.BlocksColumns.BLOCK_PREV);
        if (idColumn != -1) {
            prevBlock = Base58.decode(rs.getString(idColumn));
        }
        idColumn = rs.findColumn(AbstractDb.BlocksColumns.BLOCK_ROOT);
        if (idColumn != -1) {
            merkleRoot = Base58.decode(rs.getString(idColumn));
        }
        idColumn = rs.findColumn(AbstractDb.BlocksColumns.BLOCK_TIME);
        if (idColumn != -1) {
            timestamp = rs.getInt(idColumn);
        }
        idColumn = rs.findColumn(AbstractDb.BlocksColumns.BLOCK_VER);
        if (idColumn != -1) {
            version = rs.getLong(idColumn);
        }
        idColumn = rs.findColumn(AbstractDb.BlocksColumns.IS_MAIN);
        if (idColumn != -1) {
            isMain = rs.getInt(idColumn) == 1;
        }
        return new Block(blockHash, version, prevBlock, merkleRoot, timestamp, target, nonce, blockNo, isMain);

    }
}
