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

import com.google.common.base.Optional;
import net.bither.bitherj.utils.PrivateKeyUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.WhitespaceTrimmer;
import net.bither.viewsystem.TextBoxes;
import net.bither.viewsystem.base.AccessibilityDecorator;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.components.ScrollBarUIDecorator;
import net.bither.viewsystem.components.borders.TextBubbleBorder;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.themes.Themes;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class VerifyMessagePanel extends WizardPanel {
    private JTextField verifyingAddress;
    private JTextArea signatureTextArea;
    private JTextArea messageTextArea;

    private JLabel reportLabel;

    public VerifyMessagePanel() {
        super(MessageKey.VERIFY_MESSAGE_TITLE, AwesomeIcon.CHECK);

    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][]", // Column constraints
                "[]5[][100][30][30][30]" // Row constraints
        ));

        verifyingAddress = TextBoxes.newTextField(43);

        messageTextArea = TextBoxes.newEnterMessage();

        // The message is a wall of text so needs scroll bars in many cases
        messageTextArea.setBorder(null);

        // Message requires its own scroll pane
        JScrollPane messageScrollPane = new JScrollPane();
        messageScrollPane.setOpaque(true);
        messageScrollPane.setBackground(Themes.currentTheme.dataEntryBackground());
        messageScrollPane.setBorder(null);
        messageScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        messageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // View port requires special handling
        messageScrollPane.setViewportView(messageTextArea);
        messageScrollPane.getViewport().setBackground(Themes.currentTheme.dataEntryBackground());
        messageScrollPane.setViewportBorder(new TextBubbleBorder(Themes.currentTheme.dataEntryBorder()));

        // Ensure we maintain the overall theme
        ScrollBarUIDecorator.apply(messageScrollPane, true);

        signatureTextArea = TextBoxes.newTextArea(5, 40);
        AccessibilityDecorator.apply(signatureTextArea, MessageKey.SIGNATURE);

        panel.add(Labels.newVerifyMessageNote(), "growx,span 4,wrap");

        panel.add(Labels.newBitcoinAddress());
        panel.add(verifyingAddress, "growx,span 3,push,wrap");

        panel.add(Labels.newMessage());
        panel.add(messageScrollPane, "grow,span 3,push,wrap");

        panel.add(Labels.newSignature());
        panel.add(signatureTextArea, "grow,span 3,push,wrap");

        panel.add(Buttons.newVerifyMessageButton(getSignMessageAction()), "align right,cell 2 4,");
        //panel.add(Buttons.newPasteAllButton(getPasteAllAction()), "align right,cell 2 4,");
        panel.add(Buttons.newClearAllButton(getClearAllAction()), "cell 3 4,wrap");

        reportLabel = Labels.newStatusLabel(Optional.<MessageKey>absent(), null, Optional.<Boolean>absent());
        AccessibilityDecorator.apply(reportLabel, MessageKey.NOTES);
        panel.add(reportLabel, "growx,span 4");

    }

    /**
     * @return A new "verify message" action
     */
    private Action getSignMessageAction() {

        // Sign the message
        return new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {

                verifyMessage();

            }

        };
    }

    /**
     * @return A new "clear all" action
     */
    private Action getClearAllAction() {

        // Sign the message
        return new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {

                verifyingAddress.setText("");
                messageTextArea.setText("");
                signatureTextArea.setText("");
                reportLabel.setText("");
                reportLabel.setIcon(null);
            }

        };
    }
//
//    /**
//     * @return A new "paste all" action
//     */
//    private Action getPasteAllAction() {
//
//        // Sign the message
//        return new AbstractAction() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//
//                BitcoinMessages.SignedMessage signedMessage = BitcoinMessages.parseSignedMessage(ClipboardUtils.pasteStringFromClipboard());
//
//                messageTextArea.setText(signedMessage.getMessage());
//                verifyingAddress.setText(signedMessage.getAddress());
//                signatureTextArea.setText(signedMessage.getSignature());
//
//                reportLabel.setText("");
//                reportLabel.setIcon(null);
//
//            }
//
//        };
//    }

    /**
     * Verify the message text against the address specified and update UI
     */
    private void verifyMessage() {
        String addressText = WhitespaceTrimmer.trim(verifyingAddress.getText().trim());
        String messageText = messageTextArea.getText().trim();
        String signatureText = signatureTextArea.getText().trim();
        if (Utils.isEmpty(addressText) || Utils.isEmpty(messageText) || Utils.isEmpty(signatureText)) {
            new MessageDialog(LocaliserUtils.getString("verify_message_signature_verify_failed")).showMsg();
        } else {
            boolean isVerify = PrivateKeyUtil.verifyMessage(addressText, messageText, signatureText);
            if (isVerify) {
                new MessageDialog(LocaliserUtils.getString("verify_message_signature_verify_success")).showMsg();
            } else {
                new MessageDialog(LocaliserUtils.getString("verify_message_signature_verify_failed")).showMsg();
            }
        }
    }
}
