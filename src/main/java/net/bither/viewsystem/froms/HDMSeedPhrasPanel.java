package net.bither.viewsystem.froms;

import net.bither.bitherj.BitherjSettings;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.preference.UserPreference;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Panels;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.List;

public class HDMSeedPhrasPanel extends WizardPanel {
    private JTextArea taPrivateText;
    private String worldString;

    public HDMSeedPhrasPanel(List<String> worldList) {
        super(MessageKey.HDM_COLD_SEED_WORD_LIST, AwesomeIcon.BITBUCKET, true);
        worldString = "";
        for (int i = 0; i < worldList.size(); i++) {
            if (i == worldList.size() - 1) {
                worldString += worldList.get(i);
            } else if ((i + 1) % 3 == 0) {
                worldString += worldList.get(i) + "-" + "\n";

            } else {
                worldString += worldList.get(i) + "-";
            }
        }

        if (UserPreference.getInstance().getAppMode() == BitherjSettings.AppMode.HOT) {
            updateTitle(LocaliserUtils.getString("hdm_hot_seed_word_list"));
        }


    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][][]", // Column constraints
                "[]20[][][][][]80[]40[][]" // Row constraints
        ));

        taPrivateText = new JTextArea();
        taPrivateText.setBorder(null);
        taPrivateText.setEditable(false);
        taPrivateText.setText(worldString);
        taPrivateText.setBackground(panel.getBackground());
        taPrivateText.setFont(taPrivateText.getFont().deriveFont(20));
        panel.add(taPrivateText, "align center,cell 2 2 ,grow");

    }
}
