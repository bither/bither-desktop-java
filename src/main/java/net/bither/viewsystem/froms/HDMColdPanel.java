package net.bither.viewsystem.froms;

import net.bither.BitherSetting;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.base.RadioButtons;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HDMColdPanel extends WizardPanel {
    private JRadioButton radioButton;

    public HDMColdPanel() {
        super(MessageKey.HDM, AwesomeIcon.FA_RECYCLE, false);
    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][][][][]", // Column constraints
                "[]10[][][][][]80[]20[][][]" // Row constraints
        ));

        panel.add(Labels.newValueLabel(LocaliserUtils.getString("version") + ": " + BitherSetting.VERSION), "push,align center,wrap");
        radioButton = RadioButtons.newRadioButton(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        }, MessageKey.CHECK_PRIVATE_KEY, null);
        panel.add(radioButton, "push,align center,wrap");
        JButton button = Buttons.newButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        }, MessageKey.CHECK_PRIVATE_KEY);
        panel.add(button, "push,align center,wrap");

    }
}