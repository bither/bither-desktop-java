package net.bither.viewsystem.froms;

import net.bither.BitherSetting;
import net.bither.fonts.AwesomeIcon;
import net.bither.http.BitherUrl;
import net.bither.languages.MessageKey;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.ViewUtil;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URI;

public class AboutPanel extends WizardPanel {
    public AboutPanel() {
        super(MessageKey.ABOUT, AwesomeIcon.SMILE_O,false);
    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][][][][]", // Column constraints
                "[]10[][][][][]80[]20[][][]" // Row constraints
        ));

        //  String version = System.getProperties().getProperty("Implementation-Version");
        //System.out.println(System.getProperties());

        panel.add(Labels.newValueLabel(LocaliserUtils.getString("version") + ": " + BitherSetting.VERSION), "push,align center,wrap");

        panel.add(Buttons.newLaunchBrowserButton(getLaunchBrowserAction(), MessageKey.VISIT_WEBSITE), "wrap,align center");

    }

    private Action getLaunchBrowserAction() {

        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    String url = BitherUrl.BITHER_URL;
                    ViewUtil.openURI(new URI(url));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        };
    }

}
