package net.bither.viewsystem.froms;

import net.bither.BitherUI;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.delegate.IPasswordGetterDelegate;
import net.bither.bitherj.factory.ImportPrivateKey;
import net.bither.factory.ImportPrivateKeyDesktop;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.utils.NativeUtil;
import net.bither.utils.StringUtil;
import net.bither.viewsystem.TextBoxes;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.DialogPassword;
import net.bither.viewsystem.themes.Themes;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.ProgressBarUI;

import java.awt.Color;
import java.awt.event.ActionEvent;

/**
 * Created by nn on 15/3/19.
 */
public class VanitygenPanel extends WizardPanel implements IPasswordGetterDelegate {

    private DialogPassword.PasswordGetter passwordGetter;
    private JTextField textField;
    // private JButton btnGenerate;
    private JLabel labelRefrsh;
    private JLabel labelProgress;
    private JProgressBar pb;

    private String[] privateKeys;

    public VanitygenPanel() {
        super(MessageKey.vanity_address, AwesomeIcon.VIMEO_SQUARE, true);
        passwordGetter = new DialogPassword.PasswordGetter(VanitygenPanel.this);
        setOkAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateAddress();
            }
        });


    }

    @Override
    public void initialiseContent(JPanel panel) {

        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][]", // Column constraints
                "[][][][]80[]20[]" // Row constraints
        ));
        pb = new JProgressBar();
        pb.setValue(50);
        labelRefrsh = Labels.newSpinner(Themes.currentTheme.fadedText(), BitherUI.NORMAL_PLUS_ICON_SIZE);

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
        labelProgress = Labels.newValueLabel("45");
//        btnGenerate = Buttons.newNormalButton(new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//
//            }
//        }, MessageKey.add_address_generate_address_with_private_key, AwesomeIcon.CHECK);

        panel.add(textField, "align center,cell 0 2 ");
        panel.add(labelRefrsh, "align center,span,wrap");
        panel.add(labelProgress, "align center,cell 0 3,shrink,wrap");
        panel.add(pb, "align center,cell 0 4,grow");
        labelRefrsh.setVisible(false);
        labelProgress.setVisible(false);
        //panel.add(btnGenerate, "align right,cell 0 3,shrink,wrap");


    }

    @Override
    public void beforePasswordDialogShow() {

    }

    @Override
    public void afterPasswordDialogDismiss() {


    }

    private void generateAddress() {
        final String input = textField.getText();

        if (StringUtil.validBicoinAddressBegin((input))) {
            labelProgress.setVisible(true);
            labelRefrsh.setVisible(true);
            new Thread(new Runnable() {
                @Override
                public void run() {

                    NativeUtil.generateAddress(input);
                    privateKeys = NativeUtil.getPrivateKey();
                    final SecureCharSequence password = passwordGetter.getPassword();
                    ImportPrivateKeyDesktop importPrivateKey = new ImportPrivateKeyDesktop(
                            ImportPrivateKey.ImportPrivateKeyType.Text, privateKeys[1], password);
                    importPrivateKey.importPrivateKey();

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            closePanel();
                            // new MessageDialog(Utils.format(LocaliserUtils.getString("add_vanity_address_success"), privateKeys[0])).showMsg();
                            labelRefrsh.setVisible(false);
                            labelProgress.setVisible(false);
                        }
                    });
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (privateKeys == null) {
                        final double[] ps = NativeUtil.getProgress();
                        if(ps == null){
                            System.out.println("ps null");
                            continue;
                        }
                        final double progress = ps[3];
                        System.out.println("v progress: " + (progress * 100));
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                String str = "";
                                for (double value : ps) {
                                    str = str + "," + value;
                                }
                                labelProgress.setText(str);
                                pb.setValue((int) (progress * 100));
                            }
                        });
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


}
