package net.bither.viewsystem.froms;

import net.bither.BitherUI;
import net.bither.fonts.AwesomeDecorator;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.panels.PanelDecorator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public abstract class WizardPanel {

    private JPanel wizardScreenPanel;
    private JPanel contentPanel;
    private JButton btnOk;
    private JButton btnCancel;
    private boolean isPopover;
    private JLabel labTitle;

    public WizardPanel(MessageKey key, AwesomeIcon icon, boolean isPopover) {
        this.isPopover = isPopover;
        wizardScreenPanel = Panels.newRoundedPanel();
        if (isPopover) {
            setDimension(new Dimension(BitherUI.POPOVER_MAX_WIDTH, BitherUI.POPOVER_MAX_HEIGHT));
        } else {
            setDimension(new Dimension(BitherUI.WIZARD_MIN_WIDTH, BitherUI.WIZARD_MIN_HEIGHT));
        }

        PanelDecorator.applyWizardTheme(wizardScreenPanel);

        labTitle = Labels.newTitleLabel(key);
        wizardScreenPanel.add(labTitle, "span 4," + BitherUI.WIZARD_MAX_WIDTH_MIG + ",shrink,aligny top,align center,wrap");


        contentPanel = Panels.newDetailBackgroundPanel(icon);
        wizardScreenPanel.add(contentPanel, "span 4,grow,push,wrap");
        JButton empty = Buttons.newExitButton(null, false);
        empty.setVisible(false);

        wizardScreenPanel.add(empty, "cell 0 2,push");
        btnCancel = Buttons.newCancelButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closePanel();


            }
        });
        wizardScreenPanel.add(btnCancel, "cell 3 2");


    }

    protected void updateTitle(String title) {
        labTitle.setText(title);
    }

    protected void modifOkButton(AwesomeIcon icon, String text) {

        btnOk.setText(text);
        AwesomeDecorator.applyIcon(icon, btnOk, true, BitherUI.NORMAL_ICON_SIZE);

    }

    private void setDimension(Dimension dimension) {
        wizardScreenPanel.setMinimumSize(dimension);
        wizardScreenPanel.setPreferredSize(dimension);
        wizardScreenPanel.setSize(dimension);
    }

    public void closePanel() {
        if (this.isPopover) {
            Panels.hideLightBoxPopoverIfPresent();
        } else {
            Panels.hideLightBoxIfPresent();
        }
    }

    public void setOkAction(Action action) {
        if (action != null) {
            btnOk = Buttons.newYesButton(action, AwesomeIcon.CHECK, false);
            wizardScreenPanel.add(btnOk, "cell 1 2");
        }

    }

    public void setOkEnabled(boolean enabled) {
        btnOk.setEnabled(enabled);
    }

    public void setCancelEnabled(boolean enabled) {
        btnCancel.setEnabled(enabled);
    }

    public void showPanel() {
        if (this.isPopover) {
            initialiseContent(contentPanel);
            Panels.showLightBoxPopover(wizardScreenPanel);
        } else {
            initialiseContent(contentPanel);
            Panels.showLightBox(wizardScreenPanel);
        }
    }

    public abstract void initialiseContent(JPanel panel);


}
