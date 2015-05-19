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
import net.bither.BitherUI;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.delegate.IPasswordGetterDelegate;
import net.bither.bitherj.factory.ImportPrivateKey;
import net.bither.bitherj.utils.Utils;
import net.bither.factory.ImportPrivateKeyDesktop;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.model.OpenCLDevice;
import net.bither.platform.builder.OSUtils;
import net.bither.utils.BitherVanitygen;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.StringUtil;
import net.bither.utils.SystemUtil;
import net.bither.viewsystem.TextBoxes;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.FontSizer;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.base.renderer.SelectAddressImage;
import net.bither.viewsystem.components.ScrollBarUIDecorator;
import net.bither.viewsystem.dialogs.DialogConfirmTask;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.themes.Themes;
import net.miginfocom.swing.MigLayout;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class VanitygenPanel extends WizardPanel implements IPasswordGetterDelegate,
        ListSelectionListener, ActionListener, BitherVanitygen.IVanitygenListener, VanityOptionPanel.IVanityOptionListener {

    private PasswordPanel.PasswordGetter passwordGetter;
    private JTextField textField;
    private JCheckBox caseInsensitiveBox;
    private JLabel lblTimeRemain;
    private JLabel lblSpeed;
    private JLabel lblDifficulty;
    private JLabel lblGenerated;
    private JLabel lblOne;
    private JProgressBar pb;

    private JLabel lblSelectDevice;
    private JCheckBox cbxCPU;
    private JCheckBox cbxGPU;
    private JLabel lblLoadingDevices;
    private JTable tbDevices;
    private JScrollPane sp;
    private OpenCLDevice selectedDevice;
    private JButton btnOption;

    private boolean isInCalculatingView;
    private int threadNum;
    private BitherSetting.ECKeyType ecKeyType = BitherSetting.ECKeyType.Compressed;

    private ArrayList<OpenCLDevice> devices = new ArrayList<OpenCLDevice>();

    private PeriodFormatter remainingTimeFormatter;
    private JPanel panel;


    private Thread computingThread;
    private BitherVanitygen bitherVanitygen;


    private String privateKeyStr;
    private String addressStr;


    public VanitygenPanel() {
        super(MessageKey.vanity_address, AwesomeIcon.VIMEO_SQUARE);
        passwordGetter = new PasswordPanel.PasswordGetter(VanitygenPanel.this);
        remainingTimeFormatter = new PeriodFormatterBuilder().printZeroNever().appendYears().appendSuffix
                (LocaliserUtils.getString("vanity_time_year_suffix")).appendMonths().appendSuffix
                (LocaliserUtils.getString("vanity_time_month_suffix")).appendDays().appendSuffix
                (LocaliserUtils.getString("vanity_time_day_suffix")).appendHours().appendSuffix
                (LocaliserUtils.getString("vanity_time_hour_suffix")).appendMinutes()
                .appendSuffix(LocaliserUtils.getString("vanity_time_minute_suffix"))
                .appendSeconds().appendSuffix(LocaliserUtils.getString
                        ("vanity_time_second_suffix")).toFormatter();
        setOkAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isInCalculatingView) {
                    generateAddress();
                } else {
                    showCalculate();
                }
            }
        });
        if (OSUtils.isWindows() && SystemUtil.isSystem32()) {
            ecKeyType = BitherSetting.ECKeyType.UNCompressed;
        }
    }

    @Override
    public void initialiseContent(JPanel panel) {
        this.panel = panel;

        panel.setLayout(new MigLayout(Panels.migXYLayout(), "[][grow][]", // Column constraints
                "20[][][]20[grow][][][][][grow]20[]20" // Row constraints
        ));
        lblOne = Labels.newValueLabel("1");
        caseInsensitiveBox = new JCheckBox(LocaliserUtils.getString("vanity_case_insensitive"));
        btnOption = Buttons.newOptionsButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                VanityOptionPanel vanityOptionPanel = new VanityOptionPanel(VanitygenPanel.this);
                vanityOptionPanel.showPanel();

            }
        });

        pb = new JProgressBar();
        pb.setValue(0);
        pb.setMaximum(1000);
        pb.setVisible(false);

        textField = TextBoxes.newEnterAddress(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {

            }

            @Override
            public void removeUpdate(DocumentEvent e) {

            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });
        lblDifficulty = Labels.newValueLabel("");
        lblGenerated = Labels.newValueLabel("");
        lblSpeed = Labels.newValueLabel("");
        lblTimeRemain = Labels.newValueLabel("");


        lblSelectDevice = Labels.newValueLabel(LocaliserUtils.getString
                ("vanity_select_computation_device"));
        Font font = lblSelectDevice.getFont();
        lblSelectDevice.setFont(font.deriveFont(14.0f));
        lblLoadingDevices = Labels.newSpinner(Themes.currentTheme.fadedText(), BitherUI
                .NORMAL_PLUS_ICON_SIZE);
        cbxCPU = new JCheckBox("CPU");
        cbxCPU.addActionListener(this);
        cbxGPU = new JCheckBox("GPU (OpenCL)");
        cbxGPU.addActionListener(this);
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

        showSelectDevice();
    }

    private void showSelectDevice() {
        isInCalculatingView = false;
        panel.add(lblSelectDevice, "align center, cell 0 0 3 1, wrap");
        panel.add(lblLoadingDevices, "align center, cell 0 1 3 1, wrap");
        panel.add(cbxCPU, "align left, cell 0 2, wrap");
        panel.add(cbxGPU, "align left, cell 0 3, wrap");
        panel.add(sp, "cell 0 4 3 8, grow");
        lblLoadingDevices.setVisible(false);
        sp.setVisible(false);
        cbxCPU.setVisible(false);
        cbxGPU.setVisible(false);
        setOkEnabled(false);
        refreshDevices();
    }

    private void showCalculate() {
        isInCalculatingView = true;

        lblSelectDevice.setVisible(false);
        lblLoadingDevices.setVisible(false);
        sp.setVisible(false);
        cbxCPU.setVisible(false);
        cbxGPU.setVisible(false);

        panel.remove(lblSelectDevice);
        panel.remove(lblLoadingDevices);
        panel.remove(sp);
        panel.remove(cbxCPU);
        panel.remove(cbxGPU);

        panel.add(lblOne, "align right,cell 0 2,wrap");
        panel.add(textField, "align center,cell 1 2,grow");
        panel.add(btnOption, "align center,cell 2 2");
        panel.add(caseInsensitiveBox, "align center,cell 3 2");
        panel.add(lblDifficulty, "align left,cell 0 4 3 1,wrap,gapleft 20");
        panel.add(lblGenerated, "align left,cell 0 5 3 1,wrap,gapleft 20");
        panel.add(lblSpeed, "align left,cell 0 6 3 1,wrap,gapleft 20");
        panel.add(lblTimeRemain, "align left,cell 0 7 3 1,wrap,gapleft 20");
        panel.add(pb, "align center,cell 0 9 3 1,gapleft 10,gapright 10,h 20!,grow,span");
        panel.doLayout();
        textField.requestFocus();
    }

    private void generateAddress() {
        if (textField.getText().length() <= 0) {
            new MessageDialog(LocaliserUtils.getString("vanity_address_not_empty")).showMsg();
            return;
        }
        final String input = "1" + textField.getText();
        String vaildBitcoinAddress = StringUtil.validBicoinAddressBegin(input);
        if (!Utils.isEmpty(vaildBitcoinAddress)) {
            String prompt = Utils.format(LocaliserUtils.getString("vanity_address_not_contains"), vaildBitcoinAddress);
            new MessageDialog(prompt).showMsg();
            return;

        }
        pb.setVisible(true);
        textField.setEnabled(false);
        caseInsensitiveBox.setEnabled(false);
        setOkEnabled(false);
        computingThread = new Thread() {
            @Override
            public void run() {
                boolean useOpenCL = shouldUseOpenCL();
                boolean igoreCase = caseInsensitiveBox.isSelected();
                String openclConfig = null;
                if (selectedDevice != null) {
                    openclConfig = selectedDevice.getConfigureString();
                }
                bitherVanitygen = new BitherVanitygen(input, useOpenCL, igoreCase, threadNum,
                        ecKeyType, openclConfig, VanitygenPanel.this);
                bitherVanitygen.generateAddress();

            }
        };
        computingThread.start();
    }


    private AbstractTableModel selectDeviceTableModel = new AbstractTableModel() {
        @Override
        public int getRowCount() {
            return devices == null ? 0 : devices.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 1:
                    return devices.get(rowIndex).getPlatformName() + " : " + devices.get
                            (rowIndex).getDeviceName();
                case 0:
                    return devices.get(rowIndex).equals(selectedDevice);
            }
            return null;
        }

        @Override
        public String getColumnName(int column) {
            return "";
        }
    };

    private void refreshDevices() {
        lblLoadingDevices.setVisible(true);
        sp.setVisible(false);
        cbxGPU.setVisible(false);
        cbxCPU.setVisible(false);
        setOkEnabled(false);
        new Thread() {
            @Override
            public void run() {
                devices.clear();
                devices.addAll(BitherVanitygen.getCLDevices());
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setOkEnabled(true);
                        if (devices.size() <= 1) {
                            showCalculate();
                            return;
                        }
                        selectedDevice = findGPUDevice();
                        cbxCPU.setSelected(selectedDevice == null);
                        cbxGPU.setSelected(selectedDevice != null);
                        sp.setVisible(true);
                        cbxGPU.setVisible(true);
                        cbxCPU.setVisible(true);
                        lblLoadingDevices.setVisible(false);
                        selectDeviceTableModel.fireTableDataChanged();
                    }
                });
            }
        }.start();
    }

    private boolean shouldUseOpenCL() {
        return selectedDevice != null;
    }

    @Override
    public void closePanel() {
        if (!Utils.isEmpty(privateKeyStr)) {
            Runnable okRunable = new Runnable() {
                @Override
                public void run() {

                    importPrivateKey();
                }
            };
            Runnable cancelRunable = new Runnable() {
                @Override
                public void run() {
                    privateKeyStr = null;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            quit();
                        }
                    });

                }
            };
            String msg = Utils.format(LocaliserUtils.getString("vantiy_is_not_add"), addressStr);
            DialogConfirmTask dialogConfirmTask = new DialogConfirmTask(msg,
                    okRunable, cancelRunable);
            dialogConfirmTask.pack();
            dialogConfirmTask.setVisible(true);

        } else {
            quit();
        }
    }

    private void quit() {
        privateKeyStr = null;
        super.closePanel();
        if (bitherVanitygen != null) {
            bitherVanitygen.stop();
        }
        if (computingThread != null && computingThread.isAlive() && !computingThread
                .isInterrupted()) {
            bitherVanitygen.stop();
            computingThread.interrupt();
        }
    }

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
                    selectedDevice = devices.get(i);
                    break;
                }
            }
            if (selectedDevice != null) {
                cbxCPU.setSelected(false);
                cbxGPU.setSelected(true);
            }
            selectDeviceTableModel.fireTableDataChanged();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cbxCPU) {
            if (cbxCPU.isSelected()) {
                selectedDevice = null;
                cbxGPU.setSelected(false);
                selectDeviceTableModel.fireTableDataChanged();
            } else {
                cbxCPU.setSelected(true);
            }
            return;
        } else if (e.getSource() == cbxGPU) {
            if (cbxGPU.isSelected()) {
                if (devices.size() < 1) {
                    cbxGPU.setSelected(false);
                    cbxCPU.setSelected(true);
                    return;
                }
                if (selectedDevice == null) {
                    selectedDevice = findGPUDevice();
                    if (selectedDevice == null) {
                        selectedDevice = devices.get(0);
                    }
                }
                cbxCPU.setSelected(false);
                selectDeviceTableModel.fireTableDataChanged();
            } else {
                cbxGPU.setSelected(true);
            }
            return;
        }
    }


    private OpenCLDevice findGPUDevice() {
        if (devices == null) {
            return null;
        }
        for (OpenCLDevice d : devices) {
            if (d.isGPU()) {
                return d;
            }
        }
        return null;
    }

    @Override
    public void beforePasswordDialogShow() {

    }

    @Override
    public void afterPasswordDialogDismiss() {

    }

    @Override
    public void onProgress(final String speed, final long generated, final double progress, final int nextPossibility, final String nextTimePeriodSeconds) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                pb.setValue((int) (pb.getMinimum() + (progress / 100) * (pb
                        .getMaximum() - pb.getMinimum())));

                lblGenerated.setText(String.format(LocaliserUtils.getString
                        ("vanity_generated"), generated, progress));
                lblSpeed.setText(String.format(LocaliserUtils.getString
                        ("vanity_speed"), speed));

                lblTimeRemain.setText(String.format(LocaliserUtils.getString("vanity_time_remain"), nextPossibility, nextTimePeriodSeconds));
            }

        });
    }

    private void importPrivateKey() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final SecureCharSequence password = passwordGetter.getPassword();
                if (password != null) {
                    ImportPrivateKeyDesktop importPrivateKey = new ImportPrivateKeyDesktop
                            (ImportPrivateKey.ImportPrivateKeyType.Text, privateKeyStr, password, new ImportPrivateKeyDesktop.ImportPrivateKeyListener() {
                                @Override
                                public void importSuccess() {
                                    privateKeyStr = null;
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            closePanel();
                                        }
                                    });
                                }
                            });
                    importPrivateKey.importPrivateKey();


                } else {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            closePanel();
                        }
                    });
                }


            }
        }).start();

    }

    @Override
    public void getAddress(String address) {
        addressStr = address;
    }

    @Override
    public void getPrivateKey(final String privateKey, final long useTime) {
        privateKeyStr = privateKey;
        final Runnable okRunnable = new Runnable() {
            @Override
            public void run() {
                importPrivateKey();
            }
        };
        final Runnable cancelRunnable = new Runnable() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        closePanel();
                    }
                });


            }
        };
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("time:" + useTime);
                long seconds = useTime / 1000;
                if (seconds == 0) {
                    seconds = 1;
                }
                String str = Utils.format(LocaliserUtils.getString("vanitygen_address_generate_success"),
                        addressStr);
                DialogConfirmTask dialogConfirmTask = new DialogConfirmTask(str, okRunnable, cancelRunnable);
                dialogConfirmTask.pack();
                dialogConfirmTask.setVisible(true);
            }
        });


    }

    @Override
    public void onDifficulty(final String difficulty) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                lblDifficulty.setText(String.format(LocaliserUtils.getString
                        ("vanity_difficulty"), difficulty));
            }
        });


    }

    @Override
    public void error(String error) {
        new MessageDialog(error).showMsg();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                closePanel();
            }
        });


    }

    @Override
    public void selectOption(BitherSetting.ECKeyType ecKeyType, int threadNum) {
        this.ecKeyType = ecKeyType;
        this.threadNum = threadNum;

    }

    @Override
    public boolean useOpenCl() {
        return shouldUseOpenCL();
    }

    private String speedToString(double speed) {
        String[] KMG = new String[]{"", "k", "M", "G"};

        int i = 0;
        while (speed >= 1000) {
            i++;
            speed /= 1000.0;
        }
        return String.format("%s%s", new DecimalFormat("#.##").format(speed), KMG[i]);
    }

    private String secondsToString(long seconds) {
        long now = System.currentTimeMillis();
        return remainingTimeFormatter.print(new Period(now, now + seconds * 1000, PeriodType.yearMonthDayTime()));
    }
}
