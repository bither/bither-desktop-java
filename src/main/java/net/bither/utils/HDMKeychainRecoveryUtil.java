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
import net.bither.bitherj.core.HDMAddress;
import net.bither.bitherj.core.HDMBId;
import net.bither.bitherj.core.HDMKeychain;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.crypto.hd.DeterministicKey;
import net.bither.bitherj.crypto.hd.HDKeyDerivation;
import net.bither.bitherj.delegate.IPasswordGetterDelegate;
import net.bither.bitherj.utils.Utils;
import net.bither.qrcode.*;
import net.bither.viewsystem.dialogs.DialogConfirmTask;
import net.bither.viewsystem.dialogs.DialogProgress;
import net.bither.viewsystem.froms.PasswordPanel;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class HDMKeychainRecoveryUtil implements IPasswordGetterDelegate {
    private DialogProgress dp;

    private ReentrantLock lock = new ReentrantLock();
    private Condition coldRootCondition = lock.newCondition();
    private Condition hdmIdCondiction = lock.newCondition();

    private byte[] coldRoot;
    private HDMBId hdmBid;
    private String hdmBidSignature;


    public HDMKeychainRecoveryUtil(DialogProgress dp) {
        if (dp == null) {
            this.dp = new DialogProgress();
        } else {
            this.dp = dp;
        }

    }

    public boolean canRecover() {
        return AddressManager.getInstance().getHdmKeychain() == null;
    }

    public String recovery() {
        if (AddressManager.getInstance().getHdmKeychain() != null) {
            throw new RuntimeException("Already has hdm keychain can not recover");
        }
        PasswordPanel.PasswordGetter passwordGetter = new PasswordPanel.PasswordGetter(
                this);
        if (getColdRoot() == null) {
            return null;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                dp.pack();
                dp.setVisible(true);
            }
        });
        String preSign;
        try {
            preSign = getHDMIdPresign();
        } catch (Exception e) {
            e.printStackTrace();
            String msg = LocaliserUtils.getString("network_or_connection_error");
            if (e instanceof Http400Exception) {
                msg = ExceptionUtil.getHDMHttpExceptionMessage(((Http400Exception) e)
                        .getErrorCode());
            }
            dismissDp();
            return msg;
        }
        getHDMSignature(preSign);
        if (hdmBidSignature == null) {
            dismissDp();
            return null;
        }
        SecureCharSequence password = passwordGetter.getPassword();
        if (password == null) {
            dismissDp();
            return null;
        }
        PeerUtil.stopPeer();
        HDMKeychain.HDMKeychainRecover keychain;
        try {
            keychain = new HDMKeychain.HDMKeychainRecover(coldRoot, password,
                    new HDMKeychain.HDMFetchRemoteAddresses() {
                        @Override
                        public List<HDMAddress.Pubs> getRemoteExistsPublicKeys(CharSequence password) {
                            try {
                                return hdmBid.recoverHDM(hdmBidSignature, password);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            String msg = LocaliserUtils.getString("network_or_connection_error");
            if (e instanceof Http400Exception) {
                msg = ExceptionUtil.getHDMHttpExceptionMessage(((Http400Exception) e)
                        .getErrorCode());
            }
            dismissDp();
            return msg;
        }


        if (keychain.getAllCompletedAddresses().size() > 0) {
            KeyUtil.setHDKeyChain(keychain);
            PeerUtil.startPeer();
        } else {
            PeerUtil.startPeer();
            dismissDp();
            return LocaliserUtils.getString("hdm_keychain_recovery_no_addresses");
        }

        dismissDp();
        return LocaliserUtils.getString("hdm_keychain_recovery_message");
    }

    private void getHDMSignature(final String presign) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                dp.dispose();
                HDMServerUnsignedQRCodePanel hdmServerUnsignedQRCodePanel = new HDMServerUnsignedQRCodePanel(
                        new HDMServerUnsignedQRCodeListener() {
                            @Override
                            public void handleResult(String result, IReadQRCode readQRCode) {
                                readQRCode.close();
                                setServerSignatureResult(result);
                            }

                            @Override
                            public void scanSignedHDMServerQRCodeCancel() {
                                try {
                                    lock.lock();
                                    hdmIdCondiction.signal();
                                } finally {
                                    lock.unlock();
                                }
                            }
                        }, presign);
                hdmServerUnsignedQRCodePanel.showPanel();
            }
        });
        try {
            lock.lockInterruptibly();
            hdmIdCondiction.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    private String getHDMIdPresign() throws Exception {
        initHDMBidFromColdRoot();
        return hdmBid.getPreSignString();
    }

    private void initHDMBidFromColdRoot() {
        if (hdmBid != null) {
            return;
        }
        DeterministicKey root = HDKeyDerivation.createMasterPubKeyFromExtendedBytes(Arrays.copyOf
                (coldRoot, coldRoot.length));
        DeterministicKey key = root.deriveSoftened(0);
        String address = Utils.toAddress(key.getPubKeyHash());
        root.wipe();
        key.wipe();
        hdmBid = new HDMBId(address);
    }

    private void setServerSignatureResult(String serverSignatureResult) {
        hdmBidSignature = serverSignatureResult;
        try {
            lock.lock();
            hdmIdCondiction.signal();
        } finally {
            lock.unlock();
        }

    }

    public void setColdRoot(String coldRootString) {
        String result = coldRootString;
        coldRoot = Utils.hexStringToByteArray(result);
        try {
            lock.lock();
            coldRootCondition.signal();
        } finally {
            lock.unlock();
        }

    }


    private byte[] getColdRoot() {
        if (coldRoot == null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    DialogConfirmTask dialogConfirmTask = new DialogConfirmTask(LocaliserUtils.getString("hdm_keychain_add_scan_cold"), new Runnable() {
                        @Override
                        public void run() {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {

                                }
                            });
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    SelectTransportQRCodePanel selectTransportQRCodePanel = new SelectTransportQRCodePanel(new IScanQRCode() {
                                        @Override
                                        public void handleResult(String result, IReadQRCode readQRCode) {
                                            readQRCode.close();
                                            setColdRoot(result);
                                        }
                                    });
                                    selectTransportQRCodePanel.showPanel();
                                }
                            });
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            coldRoot = null;
                            try {
                                lock.lock();
                                coldRootCondition.signal();
                            } finally {
                                lock.unlock();
                            }
                        }
                    });
                    dialogConfirmTask.pack();
                    dialogConfirmTask.setVisible(true);
                }
            });
            try {
                lock.lockInterruptibly();
                coldRootCondition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
        return coldRoot;
    }

    private void dismissDp() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                dp.dispose();
            }
        });
    }

    @Override
    public void beforePasswordDialogShow() {
        dp.dispose();
    }

    @Override
    public void afterPasswordDialogDismiss() {
        dp.dispose();
    }
}
