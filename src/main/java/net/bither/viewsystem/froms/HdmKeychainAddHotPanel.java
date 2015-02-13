package net.bither.viewsystem.froms;

import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.base.RadioButtons;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by nn on 15/2/13.
 */
public class HdmKeychainAddHotPanel extends WizardPanel {


    public static interface DialogHdmKeychainAddHotDelegate {
        public void addWithXRandom();

        public void addWithoutXRandom();
    }

    private DialogHdmKeychainAddHotDelegate dialogHdmKeychainAddHotDelegate;
    private JRadioButton radioButton;


    public HdmKeychainAddHotPanel(final DialogHdmKeychainAddHotDelegate dialogHdmKeychainAddHotDelegate) {
        super(MessageKey.HDM, AwesomeIcon.FA_RECYCLE, true);
        this.dialogHdmKeychainAddHotDelegate = dialogHdmKeychainAddHotDelegate;

        setOkAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
                if (dialogHdmKeychainAddHotDelegate != null) {
                    if (!radioButton.isSelected()) {
                        dialogHdmKeychainAddHotDelegate.addWithoutXRandom();
                    } else {
                        dialogHdmKeychainAddHotDelegate.addWithXRandom();
                    }
                }

            }
        });
    }

    @Override
    public void initialiseContent(JPanel panel) {

        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][][][][]", // Column constraints
                "[]10[][][][][]80[]20[][][]" // Row constraints
        ));


        panel.add(Labels.newNoteLabel(new String[]{LocaliserUtils.getString("hdm_seed_generation_notice")}), "push,align center,wrap");
        radioButton = RadioButtons.newRadioButton(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        }, MessageKey.xrandom, null);
        radioButton.setFocusPainted(false);
        radioButton.setSelected(true);
        panel.add(radioButton, "push,align center,wrap");

    }
}
