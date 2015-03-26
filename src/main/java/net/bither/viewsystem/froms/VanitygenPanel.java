package net.bither.viewsystem.froms;

import net.bither.BitherUI;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.delegate.IPasswordGetterDelegate;
import net.bither.bitherj.factory.ImportPrivateKey;
import net.bither.factory.ImportPrivateKeyDesktop;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.utils.Localiser;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.NativeUtil;
import net.bither.utils.StringUtil;
import net.bither.viewsystem.TextBoxes;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.DialogPassword;
import net.bither.viewsystem.themes.Themes;
import net.miginfocom.swing.MigLayout;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.ProgressBarUI;

import java.awt.Color;
import java.awt.Label;
import java.awt.event.ActionEvent;

/**
 * Created by nn on 15/3/19.
 */
public class VanitygenPanel extends WizardPanel implements IPasswordGetterDelegate {

    private DialogPassword.PasswordGetter passwordGetter;
    private JTextField textField;
    private JCheckBox caseInsensitiveBox;
    private JLabel lblTimeRemain;
    private JLabel lblSpeed;
    private JLabel lblDifficulty;
    private JLabel lblGenerated;

    private JProgressBar pb;

    private String[] privateKeys;

    private PeriodFormatter remainingTimeFormatter;

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
                generateAddress();
            }
        });
    }

    @Override
    public void initialiseContent(JPanel panel) {

        panel.setLayout(new MigLayout(Panels.migXYLayout(), "[][grow][]", // Column constraints
                "20[][][][grow][][][][][grow][]20" // Row constraints
        ));
        JLabel lblOne = Labels.newValueLabel("1");
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

//        btnGenerate = Buttons.newNormalButton(new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//
//            }
//        }, MessageKey.add_address_generate_address_with_private_key, AwesomeIcon.CHECK);

        panel.add(lblOne, "align right,cell 0 2,wrap");
        panel.add(textField, "align center,cell 1 2,grow");
        panel.add(caseInsensitiveBox, "align center,cell 2 2");
        panel.add(lblDifficulty, "align left,cell 0 4 3 1,wrap,gapleft 20");
        panel.add(lblGenerated, "align left,cell 0 5 3 1,wrap,gapleft 20");
        panel.add(lblSpeed, "align left,cell 0 6 3 1,wrap,gapleft 20");
        panel.add(lblTimeRemain, "align left,cell 0 7 3 1,wrap,gapleft 20");
        panel.add(pb, "align center,cell 0 9 3 1,gapleft 10,gapright 10,h 20!,grow,span");

    }

    @Override
    public void beforePasswordDialogShow() {

    }

    @Override
    public void afterPasswordDialogDismiss() {


    }

    private void generateAddress() {
        final String input = "1" + textField.getText();

        if (StringUtil.validBicoinAddressBegin((input))) {
            pb.setVisible(true);

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
                            final double progress = 0.3;
                            final long difficulty = 2200020;
                            final long generated = 20023;
                            final long speed = 1000;
                            final int nextPossibility = 50;
                            final long nextTimePeriodSeconds = 600;
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
                                            ("vanity_time_remain"), nextPossibility, secondsToString
                                            (nextTimePeriodSeconds)));
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

    private String secondsToString(long seconds) {
        return remainingTimeFormatter.print(new Period(seconds * 1000));
    }
}
