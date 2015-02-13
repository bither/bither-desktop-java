package net.bither.xrandom;

import net.bither.Bither;
import net.bither.bitherj.core.Address;
import net.bither.bitherj.crypto.ECKey;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.utils.PrivateKeyUtil;
import net.bither.utils.KeyUtil;
import net.bither.utils.PeerUtil;
import net.bither.viewsystem.dialogs.DialogPassword;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class PrivateKeyUEntropyDialog extends UEntropyDialog<java.util.List<String>> {

    public PrivateKeyUEntropyDialog(int targetCount, DialogPassword.PasswordGetter passwordGetter) {
        super(targetCount, passwordGetter);
    }

    @Override
    Thread getGeneratingThreadWithXRandom(UEntropyCollector collector) {
        return new GenerateThread(collector);
    }

    @Override
    void didSuccess(List<String> addressStr) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                quit();
                Bither.refreshFrame();

            }
        });

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
            SecureCharSequence password = passwordGetter.getPassword();
            if (password != null) {
                password.wipe();
                password = null;
            }
            PeerUtil.stopPeer();
            entropyCollector.stop();
        }

        @Override
        public void run() {
            SecureCharSequence password = passwordGetter.getPassword();
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
                    ECKey ecKey = ECKey.generateECKey(xRandom);
                    ecKey.setFromXRandom(true);
                    progress += itemProgress * progressKeyRate;
                    onProgress(progress);
                    if (cancelRunnable != null) {
                        finishGenerate();
                        SwingUtilities.invokeLater(cancelRunnable);

                        return;
                    }
                    // start encrypt
                    ecKey = PrivateKeyUtil.encrypt(ecKey, password);
                    Address address = new Address(ecKey.toAddress(), ecKey.getPubKey(),
                            PrivateKeyUtil.getEncryptedString(ecKey), ecKey.isFromXRandom());
                    ecKey.clearPrivateKey();
                    addressList.add(address);
                    addressStrs.add(address.getAddress());

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
