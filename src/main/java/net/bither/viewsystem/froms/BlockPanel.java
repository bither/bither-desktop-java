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

package net.bither.viewsystem.froms;

import net.bither.BitherSetting;
import net.bither.bitherj.core.Block;
import net.bither.bitherj.core.BlockChain;
import net.bither.fonts.AwesomeIcon;
import net.bither.implbitherj.BlockNotificationCenter;
import net.bither.languages.MessageKey;
import net.bither.model.BlockTableModel;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.FontSizer;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.components.ScrollBarUIDecorator;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class BlockPanel extends WizardPanel implements BlockNotificationCenter.IBlockListener {
    private JTable table;
    private List<Block> blocks = new ArrayList<Block>();
    private BlockTableModel blockTableModel;
    private JButton btnPeer;

    public BlockPanel() {
        super(MessageKey.BLOCKS, AwesomeIcon.FA_SHARE_ALT);
        BlockNotificationCenter.addBlockChange(this);
    }

    @Override
    public void initialiseContent(JPanel panel) {

        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "10[]10", // Column constraints
                "[][]10" // Row constraints
        ));

        btnPeer = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closePanel();
                PeersPanel peersPanel = new PeersPanel();
                peersPanel.showPanel();
            }
        }, MessageKey.PEERS, AwesomeIcon.FA_USERS);
        panel.add(btnPeer, "align center,shrink,wrap");

        blockTableModel = new BlockTableModel(blocks);
        table = new JTable(blockTableModel);
        FontMetrics fontMetrics = panel.getFontMetrics(FontSizer.INSTANCE.getAdjustedDefaultFont());
        TableColumn tableColumn = table.getColumnModel().getColumn(0);
        int statusWidth = fontMetrics.stringWidth("1509452");
        tableColumn.setPreferredWidth(statusWidth + BitherSetting.STATUS_WIDTH_DELTA);
        tableColumn = table.getColumnModel().getColumn(1);
        int tiemW = fontMetrics.stringWidth("10 hour 23 minutes");
        tableColumn.setPreferredWidth(tiemW + BitherSetting.STATUS_WIDTH_DELTA);
        tableColumn = table.getColumnModel().getColumn(2);
        int hashW = fontMetrics.stringWidth("00000000000000001af98a918bfe925759b5bf4215625be076c70d96e3b47e42");
        tableColumn.setPreferredWidth(hashW);
        table.setAutoCreateColumnsFromModel(true);
        table.setAutoResizeMode(0);
        table.setAutoscrolls(true);
        final JScrollPane jScrollPane = new JScrollPane();
        jScrollPane.setViewportView(table);
        ScrollBarUIDecorator.apply(jScrollPane, false);
        panel.add(jScrollPane, "push,align center,grow");
        refreshBlock();


    }

    @Override
    public void blockChange() {
        refreshBlock();
    }

    private void refreshBlock() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Block> temp = BlockChain.getInstance().getLimitBlocks(100);
                if (temp != null && temp.size() > 0) {
                    blocks.clear();
                    blocks.addAll(temp);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            blockTableModel.fireTableDataChanged();
                        }
                    });
                }
            }
        }).start();
    }

}
