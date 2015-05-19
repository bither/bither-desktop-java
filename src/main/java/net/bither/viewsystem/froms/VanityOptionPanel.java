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
 *
 */

package net.bither.viewsystem.froms;

import net.bither.BitherSetting;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.platform.builder.OSUtils;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.SystemUtil;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class VanityOptionPanel extends WizardPanel {
    public interface IVanityOptionListener {
        public void selectOption(BitherSetting.ECKeyType ecKeyType
                , int threadNum);

        public boolean useOpenCl();
    }

    private JSpinner spinnerCount;
    private IVanityOptionListener vanityOptionListener;
    private BitherSetting.ECKeyType ecKeyType = BitherSetting.ECKeyType.Compressed;

    private JCheckBox compressedCheckBox;

    public VanityOptionPanel(final IVanityOptionListener vanityOptionListener) {
        super(MessageKey.vanity_address_option, AwesomeIcon.FA_LIST);
        this.vanityOptionListener = vanityOptionListener;

        setOkAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int targetCount = 0;
                if (!vanityOptionListener.useOpenCl() && spinnerCount != null) {
                    targetCount = Integer.valueOf(spinnerCount.getValue().toString());
                }
                if (!compressedCheckBox.isSelected()) {
                    ecKeyType = BitherSetting.ECKeyType.UNCompressed;
                }
                if (vanityOptionListener != null) {
                    vanityOptionListener.selectOption(ecKeyType, targetCount);
                }
                closePanel();

            }
        });
    }


    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[]", // Column constraints
                "[][][][]80[]20[]" // Row constraints
        ));

        compressedCheckBox = new JCheckBox();
        compressedCheckBox.setSelected(true);
        compressedCheckBox.setText(LocaliserUtils.getString("compressed_private_key"));
        panel.add(compressedCheckBox, "align center,shrink,wrap");
        if (OSUtils.isWindows() && SystemUtil.isSystem32()) {
            ecKeyType = BitherSetting.ECKeyType.UNCompressed;
            compressedCheckBox.setSelected(false);
            compressedCheckBox.setEnabled(false);
        }
        if (vanityOptionListener != null && !vanityOptionListener.useOpenCl()) {
            panel.add(getSelectThreadsCount(), "align center,shrink,wrap");
        }

    }

    private JPanel getSelectThreadsCount() {
        JPanel panel = Panels.newPanel(new MigLayout(Panels.migXYLayout(),
                "[][]", // Column constraints
                "[]" // Row constraints))
        ));

        spinnerCount = new JSpinner();
        JLabel label = Labels.newValueLabel(LocaliserUtils.getString("thread_count"));
        panel.add(label, "align right,shrink ");
        panel.add(spinnerCount, "align center,shrink");
        int cpuCount = SystemUtil.getAvailableProcessors();
        Integer value = new Integer(cpuCount);
        Integer min = new Integer(1);

        Integer max = new Integer(cpuCount);
        Integer step = new Integer(1);
        SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, step);
        spinnerCount.setModel(model);
        return panel;
    }
}
