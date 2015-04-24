/*
 *
 *  * Copyright 2014 http://Bither.net
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package net.bither.utils;

import net.bither.bitherj.api.http.Http400Exception;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.HDMBId;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.delegate.IPasswordGetterDelegate;
import net.bither.qrcode.HDMServerUnsignedQRCodeListener;
import net.bither.qrcode.HDMServerUnsignedQRCodePanel;
import net.bither.qrcode.IReadQRCode;
import net.bither.viewsystem.dialogs.DialogProgress;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.froms.PasswordPanel;

import javax.swing.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by songchenwen on 15/2/11.
 */
public class HDMResetServerPasswordUtil implements IPasswordGetterDelegate {
    private int ServerQRCodeRequestCode = 1651;

    private PasswordPanel.PasswordGetter passwordGetter;

    private DialogProgress dp;

    private ReentrantLock lock = new ReentrantLock();
    private Condition hdmIdCondiction = lock.newCondition();

    private HDMBId hdmBid;
    private String serverSignature;

    public HDMResetServerPasswordUtil(DialogProgress dp) {
        this(dp, null);
    }


    public HDMResetServerPasswordUtil(CharSequence password) {
        this(null, password);
    }

    public HDMResetServerPasswordUtil(DialogProgress dp, CharSequence password) {
        if (dp == null) {
            this.dp = new DialogProgress();
        } else {
            this.dp = dp;
        }

        passwordGetter = new PasswordPanel.PasswordGetter(this);
        setPassword(password);
    }

    public void setPassword(CharSequence password) {
        if (password != null) {
            passwordGetter.setPassword(new SecureCharSequence(password));
        } else {
            passwordGetter.setPassword(null);
        }
    }

    public boolean changePassword() {
        hdmBid = HDMBId.getHDMBidFromDb();
        serverSignature = null;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                dp.pack();
                dp.setVisible(true);
            }
        });
        String pre;
        try {
            pre = hdmBid.getPreSignString();
        } catch (Http400Exception ex400) {
            ex400.printStackTrace();
            showMsg(ExceptionUtil.getHDMHttpExceptionMessage(ex400.getErrorCode()));
            passwordGetter.wipe();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            showMsg(LocaliserUtils.getString("network_or_connection_error"));
            passwordGetter.wipe();
            return false;
        }
        final String preSign = pre;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                dp.dispose();
                serverSignature = null;
                HDMServerUnsignedQRCodePanel hdmServerUnsignedQRCodePanel = new HDMServerUnsignedQRCodePanel(new HDMServerUnsignedQRCodeListener() {
                    @Override
                    public void handleResult(String result, IReadQRCode readQRCode) {
                        readQRCode.close();
                        setServerSignatureResult(result);
                    }

                    @Override
                    public void scanSignedHDMServerQRCodeCancel() {
                        serverSignature = null;
                        try {
                            lock.lock();
                            hdmIdCondiction.signal();
                        } finally {
                            lock.unlock();
                        }
                    }
                }, preSign);
                hdmServerUnsignedQRCodePanel.showPanel();

            }
        });
        try {
            lock.lock();
            hdmIdCondiction.awaitUninterruptibly();
        } finally {
            lock.unlock();
        }
        if (serverSignature == null) {
            passwordGetter.wipe();
            return false;
        }
        SecureCharSequence password = passwordGetter.getPassword();
        if (password == null) {
            passwordGetter.wipe();
            return false;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                dp.pack();
                dp.setVisible(true);
            }
        });
        try {
            if (AddressManager.getInstance().getHdmKeychain() != null &&
                    AddressManager.getInstance().getHdmKeychain().isInRecovery()) {
                hdmBid.recoverHDM(serverSignature, password);
            } else {
                hdmBid.setSignature(serverSignature, password);
            }
        } catch (Http400Exception ex400) {
            ex400.printStackTrace();
            showMsg(ExceptionUtil.getHDMHttpExceptionMessage(ex400.getErrorCode()));
            passwordGetter.wipe();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            showMsg(LocaliserUtils.getString("hdm_keychain_add_sign_server_qr_code_error"));
            passwordGetter.wipe();
            return false;
        }
        passwordGetter.wipe();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                dp.dispose();
            }
        });
        return true;
    }

    public boolean setServerSignatureResult(String serverSignatureString) {
        serverSignature = serverSignatureString;
        try {
            lock.lock();
            hdmIdCondiction.signal();
        } finally {
            lock.unlock();
        }
        return true;
    }

    private void showMsg(final String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MessageDialog(msg).showMsg();
            }
        });

    }

    @Override
    public void beforePasswordDialogShow() {
        if (dp.isShowing()) {
            dp.dispose();
        }
    }

    @Override
    public void afterPasswordDialogDismiss() {
        if (!dp.isShowing()) {
            dp.dispose();
        }
    }
}
