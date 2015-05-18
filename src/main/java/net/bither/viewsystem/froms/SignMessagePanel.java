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
import net.bither.BitherUI;
import net.bither.bitherj.core.Address;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.viewsystem.TextBoxes;
import net.bither.viewsystem.action.TextTransfer;
import net.bither.viewsystem.base.AccessibilityDecorator;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.components.ScrollBarUIDecorator;
import net.bither.viewsystem.components.borders.TextBubbleBorder;
import net.bither.viewsystem.themes.Themes;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;

public class SignMessagePanel extends WizardPanel {

    private Address address;
    private JLabel signingAddressLabel;
    private JLabel messageLabel;
    private JLabel signatureLabel;

    private JTextField signingAddress;
    private JTextArea signature;
    private JTextArea messageTextArea;

    private JPasswordField currentPassword;
    private JLabel spinner;

    JLabel reportLabel;

    public SignMessagePanel(Address address) {
        super(MessageKey.SIGN_MESSAGE_TITLE, AwesomeIcon.PENCIL);
        this.address = address;
    }

    @Override
    public void initialiseContent(JPanel panel) {

        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][]", // Column constraints
                "[][80][][30][30][20]" // Row constraints
        ));

        // Labels (also used in clipboard)
        signingAddressLabel = Labels.newBitcoinAddress();

        messageLabel = Labels.newMessage();
        signatureLabel = Labels.newSignature();

        signingAddress = TextBoxes.newTextField(43);
        signingAddress.setText(address.getAddress());
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

        signature = TextBoxes.newReadOnlyLengthLimitedTextArea(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {

            }

            @Override
            public void removeUpdate(DocumentEvent e) {

            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        }, 5, 40);
        AccessibilityDecorator.apply(signature, MessageKey.SIGNATURE);

        // Add them to the panel
        panel.add(signingAddressLabel);
        panel.add(signingAddress, "growx,span 3,push,wrap");

        panel.add(messageLabel);
        panel.add(messageScrollPane, "grow,span 3,push,wrap");
        panel.add(Labels.newEnterPassword());
        panel.add(getenterPasswordMaV(), "growx,span 3,wrap");

        panel.add(Buttons.newSignMessageButton(getSignMessageAction()), "cell 1 3,align right");
        //    panel.add(Buttons.newCopyAllButton(getCopyClipboardAction()), "cell 2 3");
        panel.add(Buttons.newClearAllButton(getClearAllAction()), "cell 3 3,wrap");

        panel.add(signatureLabel);
        panel.add(signature, "grow,span 3,push,wrap");

        reportLabel = Labels.newStatusLabel(Optional.<MessageKey>absent(), null, Optional.<Boolean>absent());
        AccessibilityDecorator.apply(reportLabel, MessageKey.NOTES);
        panel.add(reportLabel, "growx,span 4");

    }

    /**
     * @return A new action for signing the message
     */
    private Action getSignMessageAction() {

        // Sign the message
        return new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {

                signMessage();

            }

        };
    }

    /**
     * @return A new action for clearing the signing address, message text and signature
     */
    private Action getClearAllAction() {

        // Clear the fields and set focus
        return new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                signingAddress.setText("");
                messageTextArea.setText("");
                currentPassword.setText("");

                signature.setText("");
                reportLabel.setText("");
                reportLabel.setIcon(null);

                // Reset focus
                signingAddress.requestFocusInWindow();
            }
        };
    }

    private JPanel getenterPasswordMaV() {

        JPanel panel = Panels.newPanel(
                new MigLayout(
                        Panels.migXLayout(), // Layout
                        "[][][][]", // Columns
                        "[]" // Rows
                ));

        // Keep track of the credentials fields
        currentPassword = TextBoxes.newPassword();

        // Provide an invisible tar pit spinner
        spinner = Labels.newSpinner(Themes.currentTheme.fadedText(), BitherUI.NORMAL_PLUS_ICON_SIZE);
        spinner.setVisible(false);


        // Bind a document listener to allow instant update of UI to matched passwords
        currentPassword.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        updateModel();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        updateModel();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        updateModel();
                    }

                    /**
                     * Trigger any UI updates
                     */
                    private void updateModel() {
                        // Reset the credentials background
                        currentPassword.setBackground(Themes.currentTheme.dataEntryBackground());


                    }

                });


        panel.add(currentPassword, "growx,h 32,push");
        //panel.add(showButton, "shrink");

        // Ensure the icon label is a size suitable for rotation
        panel.add(spinner, BitherUI.NORMAL_PLUS_ICON_SIZE_MIG + ",wrap");

        return panel;

    }


    /**
     * Sign the message text with the address specified and update UI
     */
    private void signMessage() {

        String messageText = messageTextArea.getText();
        SecureCharSequence secureCharSequence = new SecureCharSequence(currentPassword.getPassword());
        String signMessage = this.address.signMessage(messageText, secureCharSequence);
        signature.setText(signMessage);

    }

    /**
     * @return A new action for copying the view contents to the clipboard
     */
    private Action getCopyClipboardAction() {

        return new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                TextTransfer textTransfer = new TextTransfer();
                //getReceiveAddress
                textTransfer.setClipboardContents(signature.getText());
            }

        };
    }


}
