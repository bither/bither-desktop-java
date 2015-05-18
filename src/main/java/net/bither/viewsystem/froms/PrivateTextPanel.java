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

import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.utils.Utils;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.utils.WalletUtils;
import net.bither.viewsystem.base.Panels;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class PrivateTextPanel extends WizardPanel {
    private JTextArea taPrivateText;
    private SecureCharSequence secureCharSequence;

    public PrivateTextPanel(SecureCharSequence secureCharSequence) {
        super(MessageKey.PRIVATE_KEY_TEXT, AwesomeIcon.FA_FILE_TEXT);
        this.secureCharSequence = Utils.formatHashFromCharSequence(secureCharSequence, 4, 16);
        secureCharSequence.wipe();

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
        taPrivateText.setFont(new Font("Monospaced", taPrivateText.getFont().getStyle(), taPrivateText.getFont().getSize()));
        taPrivateText.setText(this.secureCharSequence.toString());
        taPrivateText.setBackground(panel.getBackground());

        panel.add(taPrivateText, "align center,cell 2 2 ,grow");


    }
}
