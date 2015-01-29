package net.bither.xrandom;

import net.bither.bitherj.crypto.SecureCharSequence;

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
    void cancelGenerating(Runnable cancelRunnable) {

    }

    @Override
    void didSuccess(Object obj) {

    }

    @Override
    Thread getGeneratingThreadWithXRandom(UEntropyCollector collector, SecureCharSequence password) {
        return null;
    }
}
