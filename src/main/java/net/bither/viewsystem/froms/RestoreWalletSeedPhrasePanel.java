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

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.bither.BitherUI;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.crypto.mnemonic.MnemonicCode;
import net.bither.bitherj.factory.ImportHDSeed;
import net.bither.bitherj.utils.Utils;
import net.bither.factory.ImportHDSeedDesktop;
import net.bither.factory.ImportListener;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.TextBoxes;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.listener.IDialogPasswordListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class RestoreWalletSeedPhrasePanel extends WizardPanel implements IDialogPasswordListener {
    private static final int PHRASE_COUNT = 24;


    private JTextArea seedPhraseTextArea;
    private List<String> seedPhraseList;
    private ImportHDSeed.ImportHDSeedType importHDSeedType;

    public RestoreWalletSeedPhrasePanel(ImportHDSeed.ImportHDSeedType importHDSeedType) {
        super(importHDSeedType == ImportHDSeed.ImportHDSeedType.HDSeedPhrase ?
                MessageKey.RESTORE_WALLET_SEED_PHRASE_HD_TITLE :
                MessageKey.RESTORE_WALLET_SEED_PHRASE_TITLE, AwesomeIcon.KEY);
        setOkAction(importHDMColdPhraseAction);
        this.importHDSeedType = importHDSeedType;
    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migLayout("fill,insets 0,hidemode 1"),
                "[]", // Column constraints
                "[][]" // Row constraints
        ));
        setOkEnabled(false);
        panel.add(Panels.newRestoreFromSeedPhrase(), "wrap");
        panel.add(enterSeedPhrasePanel(), "wrap");
    }

    public JPanel enterSeedPhrasePanel() {
        JPanel panel = Panels.newPanel(
                new MigLayout(
                        "insets 0", // Layout
                        "[][][]", // Columns
                        "[][]" // Rows
                ));

        // Create view components
        seedPhraseTextArea = TextBoxes.newEnterSeedPhrase();

        seedPhraseTextArea.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        updateModelFromView();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        updateModelFromView();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        updateModelFromView();
                    }
                });


        panel.add(Labels.newSeedPhrase());
        panel.add(seedPhraseTextArea, BitherUI.WIZARD_MAX_WIDTH_SEED_PHRASE_MIG);
        return panel;

    }

    public void updateModelFromView() {
        String text = seedPhraseTextArea.getText();
        seedPhraseList = Lists.newArrayList(Splitter
                        .on(" ")
                        .omitEmptyStrings()
                        .trimResults()
                        .split(text)
        );
        boolean okEnabled;
        if (this.importHDSeedType == ImportHDSeed.ImportHDSeedType.HDSeedPhrase) {
            okEnabled = seedPhraseList.size() % 3 != 0 || !text.endsWith(" ");
        } else {
            okEnabled = seedPhraseList.size() < PHRASE_COUNT || !text.endsWith(" ");
        }
        if (okEnabled) {
            setOkEnabled(false);
        } else {
            List<String> faildWorldList = new ArrayList<String>();
            for (String world : seedPhraseList) {
                if (!MnemonicCode.instance().getWordList().contains(world)) {
                    faildWorldList.add(world);
                }
            }
            if (faildWorldList.size() == 0) {
                setOkEnabled(true);
            } else {
                String str = Utils.joinString(faildWorldList, "-");
                new MessageDialog(LocaliserUtils.getString("hdm_import_word_list_wrong_word_warn") + str).showMsg();
            }
        }
    }

    public Action importHDMColdPhraseAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            PasswordPanel dialogPassword = new PasswordPanel(RestoreWalletSeedPhrasePanel.this);
            dialogPassword.showPanel();


        }
    };

    @Override
    public void onPasswordEntered(final SecureCharSequence password) {
        if (this.importHDSeedType == ImportHDSeed.ImportHDSeedType.HDSeedPhrase) {
            ImportHDSeedDesktop importHDSeedDesktop =
                    new ImportHDSeedDesktop(importHDSeedType,
                            null, seedPhraseList, password, new ImportListener() {
                        @Override
                        public void importSuccess() {
                            closePanel();
                        }
                    });
            importHDSeedDesktop.importHDSeed();
        } else {
            ImportHDSeedDesktop importHDSeedDesktop =
                    new ImportHDSeedDesktop(seedPhraseList, password, new ImportListener() {
                        @Override
                        public void importSuccess() {
                            closePanel();
                        }
                    });
            importHDSeedDesktop.importHDMColdSeed();
        }

    }
}
