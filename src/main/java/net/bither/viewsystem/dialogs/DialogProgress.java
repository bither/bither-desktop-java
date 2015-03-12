package net.bither.viewsystem.dialogs;

import net.bither.BitherUI;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.themes.Themes;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * Created by nn on 15/3/12.
 */
public class DialogProgress extends BitherDialog {

    private JPanel contentPane;

    public DialogProgress() {
        contentPane = Panels.newPanel();
        setContentPane(contentPane);
        setModal(true);
        setUndecorated(true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        initDialog();
        contentPane.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[]", // Column constraints
                "[]" // Row constraints

        ));
        JLabel labelRefrsh = Labels.newSpinner(Themes.currentTheme.fadedText(), BitherUI.NORMAL_PLUS_ICON_SIZE);
        contentPane.add(labelRefrsh, "align center,span,wrap");

    }

}
