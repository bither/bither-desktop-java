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

import com.github.sarxos.webcam.Webcam;
import net.bither.BitherSetting;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.FontSizer;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.base.renderer.SelectAddressImage;
import net.bither.viewsystem.components.ScrollBarUIDecorator;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;

public class SelectWebcamPanel extends WizardPanel implements ListSelectionListener {

    public interface ISelectWencamListener {
        public void onSelect(Webcam webcam);
    }

    private JTable tbDevices;

    private Webcam selectedWebcam;

    java.util.List<Webcam> webcams = Webcam.getWebcams();

    private JScrollPane sp;
    private ISelectWencamListener selectWencamListener;

    public SelectWebcamPanel(ISelectWencamListener selectWencamListener) {
        super(MessageKey.select_camera, AwesomeIcon.FA_LIST);
        this.selectWencamListener = selectWencamListener;
    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "10[]10", // Column constraints
                "10[]10" // Row constraints
        ));

        tbDevices = new JTable(selectDeviceTableModel);
        tbDevices.getColumnModel().getColumn(0).setResizable(true);
        tbDevices.getColumnModel().getColumn(1).setResizable(true);

        tbDevices.getColumnModel().getColumn(1).setMinWidth(1);
        tbDevices.getColumnModel().getColumn(1).setPreferredWidth(Integer.MAX_VALUE);

        tbDevices.getColumnModel().getColumn(0).setMinWidth(20);
        tbDevices.getColumnModel().getColumn(0).setPreferredWidth(20);
        tbDevices.getColumnModel().getColumn(0).setCellRenderer(new SelectAddressImage());
        tbDevices.setOpaque(true);
        tbDevices.setAutoCreateColumnsFromModel(true);
        tbDevices.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tbDevices.setAutoscrolls(true);
        tbDevices.setBorder(BorderFactory.createEmptyBorder());
        tbDevices.setComponentOrientation(ComponentOrientation.getOrientation(LocaliserUtils
                .getLocale()));
        tbDevices.setRowHeight(Math.max(BitherSetting.MINIMUM_ICON_HEIGHT, panel.getFontMetrics
                (FontSizer.INSTANCE.getAdjustedDefaultFont()).getHeight()) + BitherSetting
                .HEIGHT_DELTA * 2);
        sp = new JScrollPane();
        sp.setViewportView(tbDevices);
        ScrollBarUIDecorator.apply(sp, false);
        tbDevices.getSelectionModel().addListSelectionListener(this);
        panel.add(sp, "push, grow");
    }

    @Override
    public void showPanel() {
        super.showPanel();
        if (webcams.size() == 1) {
            closePanel();
            if (selectWencamListener != null) {
                selectWencamListener.onSelect(webcams.get(0));

            }
        }


    }

    private AbstractTableModel selectDeviceTableModel = new AbstractTableModel() {
        @Override
        public int getRowCount() {
            return webcams == null ? 0 : webcams.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 1:
                    return webcams.get(rowIndex).getName();
                case 0:
                    return webcams.get(rowIndex).equals(selectedWebcam);
            }
            return null;
        }

        @Override
        public String getColumnName(int column) {
            return "";
        }
    };

    @Override
    public void valueChanged(ListSelectionEvent e) {

        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
        if (!lsm.isSelectionEmpty()) {
            int minIndex = lsm.getMinSelectionIndex();
            int maxIndex = lsm.getMaxSelectionIndex();
            for (int i = minIndex;
                 i <= maxIndex;
                 i++) {
                if (lsm.isSelectedIndex(i)) {
                    selectedWebcam = webcams.get(i);
                    closePanel();
                    if (this.selectWencamListener != null) {
                        this.selectWencamListener.onSelect(selectedWebcam);

                    }
                    break;
                }
            }

            this.selectDeviceTableModel.fireTableDataChanged();
        }

    }
}
