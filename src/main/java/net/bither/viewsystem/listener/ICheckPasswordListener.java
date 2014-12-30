package net.bither.viewsystem.listener;

import net.bither.bitherj.crypto.SecureCharSequence;

public interface ICheckPasswordListener {
    public boolean checkPassword(SecureCharSequence password);
}
