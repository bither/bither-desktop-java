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

package net.bither.xrandom;

import net.bither.Bither;
import net.bither.bitherj.core.HDMKeychain;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.utils.KeyUtil;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.PeerUtil;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.froms.PasswordPanel;

import javax.swing.*;
import java.util.ArrayList;

public class HDMKeychainColdUEntropyDialog extends UEntropyDialog {

    public HDMKeychainColdUEntropyDialog(PasswordPanel.PasswordGetter passwordGetter) {
        super(1, passwordGetter);
    }


    @Override
    void didSuccess(Object obj) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                quit();
                Bither.refreshFrame();
                new MessageDialog(LocaliserUtils.getString("hdm_keychain_xrandom_final_confirm")).showMsg();
            }
        });
    }

    @Override
    Thread getGeneratingThreadWithXRandom(UEntropyCollector collector) {
        return new GenerateThread(collector);
    }


    private class GenerateThread extends Thread {
        private double saveProgress = 0.1;
        private double startProgress = 0.01;
        private double progressKeyRate = 0.5;
        private double progressEntryptRate = 0.5;

        private long startGeneratingTime;


        private Runnable cancelRunnable;

        private UEntropyCollector entropyCollector;

        public GenerateThread(UEntropyCollector entropyCollector) {
            this.entropyCollector = entropyCollector;

        }

        @Override
        public synchronized void start() {

            SecureCharSequence password = passwordGetter.getPassword();
            if (password == null) {
                throw new IllegalStateException("GenerateThread does not have password");
            }
            startGeneratingTime = System.currentTimeMillis();
            super.start();
            onProgress(startProgress);
        }

        public void cancel(Runnable cancelRunnable) {
            this.cancelRunnable = cancelRunnable;
        }

        private void finishGenerate() {
            passwordGetter.wipe();
            PeerUtil.stopPeer();
            entropyCollector.stop();
        }

        @Override
        public void run() {
            boolean success = false;
            final ArrayList<String> addressStrs = new ArrayList<String>();
            double progress = startProgress;
            double itemProgress = (1.0 - startProgress - saveProgress) / (double) targetCount;

            try {
                entropyCollector.start();
                PeerUtil.stopPeer();


                for (int i = 0;
                     i < targetCount;
                     i++) {
                    if (cancelRunnable != null) {
                        finishGenerate();
                        SwingUtilities.invokeLater(cancelRunnable);
                        return;
                    }

                    XRandom xRandom = new XRandom(entropyCollector);

                    if (cancelRunnable != null) {
                        finishGenerate();
                        SwingUtilities.invokeLater(cancelRunnable);
                        return;
                    }

                    HDMKeychain chain = new HDMKeychain(xRandom, passwordGetter.getPassword());

                    progress += itemProgress * progressKeyRate;
                    onProgress(progress);
                    if (cancelRunnable != null) {
                        finishGenerate();
                        SwingUtilities.invokeLater(cancelRunnable);
                        return;
                    }


                    progress += itemProgress * progressKeyRate;
                    onProgress(progress);
                    if (cancelRunnable != null) {
                        finishGenerate();
                        SwingUtilities.invokeLater(cancelRunnable);

                        return;
                    }
                    // start encrypt
                    KeyUtil.setHDKeyChain(chain);

                    progress += itemProgress * progressEntryptRate;
                    onProgress(progress);
                }
                entropyCollector.stop();
                passwordGetter.wipe();
                if (cancelRunnable != null) {
                    finishGenerate();
                    SwingUtilities.invokeLater(cancelRunnable);
                    return;
                }

                success = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            finishGenerate();
            if (success) {
                while (System.currentTimeMillis() - startGeneratingTime < MinGeneratingTime) {

                }
                onProgress(1);
                didSuccess(addressStrs);
            } else {
                onFailed();
            }
        }


        private void onFailed() {
            quit();
        }

    }


}
