/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.bither.factory;

import net.bither.Bither;
import net.bither.bitherj.core.HDAccount;
import net.bither.bitherj.core.HDMKeychain;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.factory.ImportHDSeed;
import net.bither.utils.KeyUtil;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.PeerUtil;
import net.bither.viewsystem.dialogs.DialogProgress;
import net.bither.viewsystem.dialogs.MessageDialog;

import javax.swing.*;
import java.util.List;

public class ImportHDSeedDesktop extends ImportHDSeed {

    private ImportListener importListener;
    private DialogProgress dialogProgress;

    public ImportHDSeedDesktop(String content, SecureCharSequence password, ImportListener importListener) {
        super(ImportHDSeedType.HDMColdSeedQRCode, content, null, password);
        this.importListener = importListener;

    }

    public ImportHDSeedDesktop(List<String> worlds, SecureCharSequence password, ImportListener importListener) {
        super(ImportHDSeedType.HDMColdPhrase, null, worlds, password);
        this.importListener = importListener;

    }

    public ImportHDSeedDesktop(ImportHDSeedType importHDSeedType,
                               String content, List<String> worlds, SecureCharSequence password, ImportListener importListener) {
        super(importHDSeedType, content, worlds, password);
        this.importListener = importListener;
    }


    public void importHDMColdSeed() {
        new Thread() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dialogProgress = new DialogProgress();
                        dialogProgress.pack();
                        dialogProgress.setVisible(true);
                    }
                });
                HDMKeychain result = importHDMKeychain();
                if (result != null) {
                    if (importListener != null) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                importListener.importSuccess();
                            }
                        });
                    }
                    KeyUtil.setHDKeyChain(result);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dialogProgress.dispose();
                            Bither.getCoreController().fireRecreateAllViews(true);
                            new MessageDialog(LocaliserUtils.getString("import_private_key_qr_code_success")).showMsg();
                        }
                    });
                    Bither.refreshFrame();

                }
            }
        }.start();

    }


    public void importHDSeed() {
        new Thread() {
            @Override
            public void run() {
                PeerUtil.stopPeer();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dialogProgress = new DialogProgress();
                        dialogProgress.pack();
                        dialogProgress.setVisible(true);
                    }
                });
                HDAccount result = importHDAccount();
                if (result != null) {
                    if (importListener != null) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                importListener.importSuccess();
                            }
                        });
                    }
                    KeyUtil.setHDAccount(result);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dialogProgress.dispose();
                            Bither.getCoreController().fireRecreateAllViews(true);
                            new MessageDialog(LocaliserUtils.getString("import_private_key_qr_code_success")).showMsg();
                        }
                    });
                    Bither.refreshFrame();
                    PeerUtil.startPeer();

                }
            }
        }.start();

    }

    @Override
    public void importError(final int errorCode) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (dialogProgress != null) {
                    dialogProgress.dispose();
                }
                String meessage;
                switch (errorCode) {

                    case PASSWORD_IS_DIFFEREND_LOCAL:
                        meessage = LocaliserUtils.getString("import_private_key_qr_code_failed_different_password");
                        break;
                    case NOT_HDM_COLD_SEED:
                        meessage = LocaliserUtils.getString("import_hdm_cold_seed_format_error");
                        break;
                    case NOT_HD_ACCOUNT_SEED:
                        meessage = LocaliserUtils.getString("import_hd_account_seed_format_error");
                        break;
                    default:
                        meessage = LocaliserUtils.getString("import_private_key_qr_code_failed");

                        break;
                }
                new MessageDialog(meessage).showMsg();

            }
        });

    }
}
