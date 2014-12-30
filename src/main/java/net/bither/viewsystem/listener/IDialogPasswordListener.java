package net.bither.viewsystem.listener;

import net.bither.bitherj.crypto.SecureCharSequence;

public interface IDialogPasswordListener {
    public void onPasswordEntered(SecureCharSequence password);
}
