package net.bither.xrandom;

import net.bither.Bither;
import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.HDMKeychain;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.utils.KeyUtil;
import net.bither.utils.PeerUtil;

import javax.swing.*;
import java.util.ArrayList;

public class HDMKeychainColdUEntropyDialog extends UEntropyDialog {
    private SecureCharSequence password;

    public HDMKeychainColdUEntropyDialog(SecureCharSequence password) {
        super(1, password);
        this.password = password;
    }

    public SecureCharSequence getPassword() {
        return password;
    }


    @Override
    void didSuccess(Object obj) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                quit();
                Bither.refreshFrame();
            }
        });
    }

    @Override
    Thread getGeneratingThreadWithXRandom(UEntropyCollector collector, SecureCharSequence password) {
        return new GenerateThread(collector, password);
    }


    private class GenerateThread extends Thread {
        private double saveProgress = 0.1;
        private double startProgress = 0.01;
        private double progressKeyRate = 0.5;
        private double progressEntryptRate = 0.5;

        private long startGeneratingTime;

        private SecureCharSequence password;
        private Runnable cancelRunnable;

        private UEntropyCollector entropyCollector;

        public GenerateThread(UEntropyCollector entropyCollector, SecureCharSequence password) {
            this.entropyCollector = entropyCollector;
            this.password = password;
        }

        @Override
        public synchronized void start() {
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
            if (password != null) {
                password.wipe();
                password = null;
            }
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

                java.util.List<Address> addressList = new ArrayList<Address>();
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

                    HDMKeychain chain = new HDMKeychain(xRandom, password);

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
                password.wipe();
                password = null;

                if (cancelRunnable != null) {
                    finishGenerate();
                    SwingUtilities.invokeLater(cancelRunnable);
                    return;
                }
                KeyUtil.addAddressListByDesc(addressList);
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
