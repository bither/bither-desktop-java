package net.bither.viewsystem.froms;

import net.bither.BitherSetting;
import net.bither.BitherUI;
import net.bither.bitherj.core.Address;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.delegate.IPasswordGetterDelegate;
import net.bither.bitherj.factory.ImportPrivateKey;
import net.bither.factory.ImportPrivateKeyDesktop;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.model.OpenCLDevice;
import net.bither.utils.Localiser;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.NativeUtil;
import net.bither.utils.StringUtil;
import net.bither.viewsystem.TextBoxes;
import net.bither.viewsystem.base.FontSizer;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.base.renderer.SelectAddressImage;
import net.bither.viewsystem.components.ScrollBarUIDecorator;
import net.bither.viewsystem.dialogs.DialogPassword;
import net.bither.viewsystem.themes.Themes;
import net.miginfocom.swing.MigLayout;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.ProgressBarUI;
import javax.swing.table.AbstractTableModel;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by nn on 15/3/19.
 */
public class VanitygenPanel extends WizardPanel implements IPasswordGetterDelegate,
        ListSelectionListener {

    private DialogPassword.PasswordGetter passwordGetter;
    private JTextField textField;
    private JCheckBox caseInsensitiveBox;
    private JLabel lblTimeRemain;
    private JLabel lblSpeed;
    private JLabel lblDifficulty;
    private JLabel lblGenerated;
    private JLabel lblOne;

    private JLabel lblSelectDevice;
    private JLabel lblLoadingDevices;
    private JTable tbDevices;
    private JScrollPane sp;
    private OpenCLDevice selectedDevice;

    private boolean isInCalculatingView;

    private ArrayList<OpenCLDevice> devices = new ArrayList<OpenCLDevice>();

    private JProgressBar pb;

    private String[] privateKeys;

    private PeriodFormatter remainingTimeFormatter;

    private JPanel panel;

    public VanitygenPanel() {
        super(MessageKey.vanity_address, AwesomeIcon.VIMEO_SQUARE, true);
        passwordGetter = new DialogPassword.PasswordGetter(VanitygenPanel.this);
        remainingTimeFormatter = new PeriodFormatterBuilder().appendYears().appendSuffix
                (LocaliserUtils.getString("vanity_time_year_suffix")).appendMonths().appendSuffix
                (LocaliserUtils.getString("vanity_time_month_suffix")).appendDays().appendSuffix
                (LocaliserUtils.getString("vanity_time_day_suffix")).appendHours().appendSuffix
                (LocaliserUtils.getString("vanity_time_hour_suffix")).appendMinutes()
                .appendSuffix(LocaliserUtils.getString("vanity_time_minute_suffix"))
                .appendSeconds().appendSuffix(LocaliserUtils.getString
                        ("vanity_time_second_suffix")).printZeroNever().toFormatter();
        setOkAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isInCalculatingView) {
                    generateAddress();
                } else if (selectedDevice != null) {
                    showCalculate();
                }
            }
        });
    }

    @Override
    public void initialiseContent(JPanel panel) {
        this.panel = panel;

        panel.setLayout(new MigLayout(Panels.migXYLayout(), "[][grow][]", // Column constraints
                "20[][][]20[grow][][][][][grow]20[]20" // Row constraints
        ));
        lblOne = Labels.newValueLabel("1");
        caseInsensitiveBox = new JCheckBox(LocaliserUtils.getString("vanity_case_insensitive"));

        pb = new JProgressBar();
        pb.setValue(50);
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

        lblSelectDevice = Labels.newValueLabel("Select Computation Device");
        lblLoadingDevices = Labels.newSpinner(Themes.currentTheme.fadedText(), BitherUI
                .NORMAL_PLUS_ICON_SIZE);
        tbDevices = new JTable(selectDeviceTableModel);
        tbDevices.getColumnModel().getColumn(0).setResizable(true);
        tbDevices.getColumnModel().getColumn(1).setResizable(true);

        tbDevices.getColumnModel().getColumn(0).setMinWidth(1);
        tbDevices.getColumnModel().getColumn(0).setPreferredWidth(Integer.MAX_VALUE);

        tbDevices.getColumnModel().getColumn(1).setMinWidth(24);
        tbDevices.getColumnModel().getColumn(1).setPreferredWidth(24);
        tbDevices.getColumnModel().getColumn(1).setCellRenderer(new SelectAddressImage());
        tbDevices.setOpaque(true);
        tbDevices.setAutoCreateColumnsFromModel(true);
        tbDevices.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tbDevices.setAutoscrolls(true);
        tbDevices.setBorder(BorderFactory.createEmptyBorder());
        tbDevices.setComponentOrientation(ComponentOrientation.getOrientation(LocaliserUtils
                .getLocale()));
        tbDevices.setRowHeight(Math.max(BitherSetting.MINIMUM_ICON_HEIGHT, panel.getFontMetrics
                (FontSizer.INSTANCE.getAdjustedDefaultFont()).getHeight()) + BitherSetting
                .HEIGHT_DELTA);
        sp = new JScrollPane();
        sp.setViewportView(tbDevices);
        ScrollBarUIDecorator.apply(sp, false);
        tbDevices.getSelectionModel().addListSelectionListener(this);

        showSelectDevice();
    }

    private void showSelectDevice() {
        isInCalculatingView = false;
        panel.add(lblSelectDevice, "align center, cell 1 2, wrap");
        panel.add(lblLoadingDevices, "align center, cell 1 3, wrap");
        panel.add(sp, "cell 0 3 3 7, grow");
        lblLoadingDevices.setVisible(false);
        sp.setVisible(false);
        refreshDevices();
    }

    private void showCalculate() {
        isInCalculatingView = true;

        lblSelectDevice.setVisible(false);
        lblLoadingDevices.setVisible(false);
        sp.setVisible(false);

        panel.remove(lblSelectDevice);
        panel.remove(lblLoadingDevices);
        panel.remove(sp);

        panel.add(lblOne, "align right,cell 0 2,wrap");
        panel.add(textField, "align center,cell 1 2,grow");
        panel.add(caseInsensitiveBox, "align center,cell 2 2");
        panel.add(lblDifficulty, "align left,cell 0 4 3 1,wrap,gapleft 20");
        panel.add(lblGenerated, "align left,cell 0 5 3 1,wrap,gapleft 20");
        panel.add(lblSpeed, "align left,cell 0 6 3 1,wrap,gapleft 20");
        panel.add(lblTimeRemain, "align left,cell 0 7 3 1,wrap,gapleft 20");
        panel.add(pb, "align center,cell 0 9 3 1,gapleft 10,gapright 10,h 20!,grow,span");
        panel.doLayout();
    }

    private void generateAddress() {
        if (textField.getText().length() <= 0) {
            return;
        }
        final String input = "1" + textField.getText();

        if (StringUtil.validBicoinAddressBegin((input))) {
            pb.setVisible(true);
            textField.setEnabled(false);
            caseInsensitiveBox.setEnabled(false);
            setOkEnabled(false);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    NativeUtil.generateAddress(input);
                    privateKeys = NativeUtil.getPrivateKey();
                    final SecureCharSequence password = passwordGetter.getPassword();
                    ImportPrivateKeyDesktop importPrivateKey = new ImportPrivateKeyDesktop
                            (ImportPrivateKey.ImportPrivateKeyType.Text, privateKeys[1], password);
                    importPrivateKey.importPrivateKey();

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            closePanel();
                        }
                    });
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (privateKeys == null) {
                        final double[] ps = NativeUtil.getProgress();
                        if (ps != null) {
                            //TODO need to get all these data
                            final double progress = 0.3;
                            final long difficulty = 2200020;
                            final long generated = 20023;
                            final long speed = 1000;
                            final int nextPossibility = 50;
                            final long nextTimePeriodSeconds = 600 * 600;
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    pb.setValue((int) (progress * 100));
                                    lblDifficulty.setText(String.format(LocaliserUtils.getString
                                            ("vanity_difficulty"), difficulty));
                                    lblGenerated.setText(String.format(LocaliserUtils.getString
                                            ("vanity_generated"), generated));
                                    lblSpeed.setText(String.format(LocaliserUtils.getString
                                            ("vanity_speed"), speed));
                                    lblTimeRemain.setText(String.format(LocaliserUtils.getString
                                            ("vanity_time_remain"), nextPossibility,
                                            secondsToString(nextTimePeriodSeconds)));
                                }
                            });
                        }
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
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
                case 0:
                    return devices.get(rowIndex).getPlatformName() + " : " + devices.get(rowIndex).getDeviceName();
                case 1:
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
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                devices.clear();
                //TODO get all opencl devices
                devices.addAll(Arrays.asList(new OpenCLDevice(0, 0, "Apple", "apple cpu"), new
                        OpenCLDevice(0, 1, "Apple", "apple gpu")));
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (devices.size() == 0) {
                            //TODO no device can't use oclvanitygen
                            return;
                        }
                        selectedDevice = devices.get(0);
                        if (devices.size() == 1) {
                            showCalculate();
                        }
                        sp.setVisible(true);
                        lblLoadingDevices.setVisible(false);
                        selectDeviceTableModel.fireTableDataChanged();
                    }
                });
            }
        }.start();
    }

    private String secondsToString(long seconds) {
        return remainingTimeFormatter.print(new Period(seconds * 1000));
    }

    @Override
    public void beforePasswordDialogShow() {

    }

    @Override
    public void afterPasswordDialogDismiss() {

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
            selectDeviceTableModel.fireTableDataChanged();
        }
    }
}
