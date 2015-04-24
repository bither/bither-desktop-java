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

package net.bither.utils;

import net.bither.bitherj.AbstractApp;
import net.bither.bitherj.api.http.Http400Exception;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.HDMAddress;
import net.bither.bitherj.core.HDMKeychain;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.delegate.HDMHotAdd;
import net.bither.bitherj.delegate.HDMSingular;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.qrcode.HDMServerUnsignedQRCodePanel;
import net.bither.qrcode.IReadQRCode;
import net.bither.qrcode.IScanQRCode;
import net.bither.viewsystem.base.IProgress;
import net.bither.viewsystem.dialogs.DialogConfirmTask;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.froms.HdmKeychainAddHotPanel;
import net.bither.viewsystem.froms.PasswordPanel;
import net.bither.xrandom.HDMKeychainHotUEntropyDialog;

import javax.swing.*;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

public class HDMHotAddDesktop extends HDMHotAdd {

    private IProgress dp;

    public HDMHotAddDesktop(IHDMHotAddDelegate delegate, HDMSingular.HDMSingularDelegate hdmSingularUtilDelegate, IProgress progress) {
        super(delegate);

        this.delegate = delegate;
        singular = new HDMSingularDesktop(hdmSingularUtilDelegate);
        this.passwordGetter = new PasswordPanel.PasswordGetter(this);
        dp = progress;
        hdmKeychainLimit = AddressManager.isHDMKeychainLimit();

    }


    @Override
    public void hotClick() {

        if (hdmKeychainLimit) {
            return;
        }
        if (singular.isInSingularMode()) {
            return;
        }
        HdmKeychainAddHotPanel hdmKeychainAddHotPanel = new HdmKeychainAddHotPanel(new HdmKeychainAddHotPanel.DialogHdmKeychainAddHotDelegate() {

            @Override
            public void addWithXRandom() {
                //  HDMKeychainHotUEntropyActivity.passwordGetter = passwordGetter;
                if (singular.shouldGoSingularMode()) {
                    HDMKeychainHotUEntropyDialog.hdmSingular = singular;
                } else {
                    singular.runningWithoutSingularMode();
                }
                if (delegate != null) {
                    delegate.callKeychainHotUEntropy();
                }
            }

            @Override
            public void addWithoutXRandom() {
                new Thread() {
                    @Override
                    public void run() {
                        SecureCharSequence password = passwordGetter.getPassword();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                dp.beginProgress();
                            }
                        });
                        if (password == null) {
                            return;
                        }
                        PeerUtil.stopPeer();
                        if (singular.shouldGoSingularMode()) {
                            singular.setPassword(password);
                            singular.generateEntropy();
                        } else {
                            singular.runningWithoutSingularMode();
                            HDMKeychain keychain = new HDMKeychain(new SecureRandom(),
                                    password);
                            KeyUtil.setHDKeyChain(keychain);
                            PeerUtil.startPeer();
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    dp.endProgress();
                                    if (delegate != null) {
                                        delegate.moveToCold(true);
                                    }
                                }
                            });
                        }
                    }
                }.start();
            }
        });
        hdmKeychainAddHotPanel.showPanel();
    }

    @Override
    public void coldClick() {
        if (hdmKeychainLimit) {
            return;
        }
        if (singular.isInSingularMode()) {
            return;
        }
        DialogConfirmTask confirmTaskDialog = new DialogConfirmTask(LocaliserUtils.getString("hdm_keychain_add_scan_cold"),
                new Runnable() {

                    @Override
                    public void run() {
                        if (delegate != null) {
                            delegate.callScanCold();
                        }
                    }
                });
        confirmTaskDialog.pack();
        confirmTaskDialog.setVisible(true);


    }

    public void setCallScanColdResult(final String result) {
        new Thread() {
            @Override
            public void run() {
                try {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dp.beginProgress();
                        }
                    });
                    if (Utils.isEmpty(result) || !QRCodeUtil.verifyBitherQRCode(result)) {
                        return;
                    }
                    coldRoot = Utils.hexStringToByteArray(result);
                    final int count = AbstractApp.bitherjSetting.hdmAddressPerSeedPrepareCount() -
                            AddressManager.getInstance().getHdmKeychain().uncompletedAddressCount();
                    if (passwordGetter.hasPassword() && count > 0) {

                    }
                    if (count > 0) {
                        SecureCharSequence password = passwordGetter.getPassword();
                        if (password == null) {

                            return;
                        }
                        AddressManager.getInstance().getHdmKeychain().prepareAddresses
                                (count, password, Arrays.copyOf(coldRoot, coldRoot.length));
                    }
                    initHDMBidFromColdRoot();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dp.endProgress();
                            delegate.moveToServer(true);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    coldRoot = null;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dp.endProgress();
                            new MessageDialog(LocaliserUtils.getString("hdm_keychain_add_scan_cold")).showMsg();
                        }
                    });
                }
            }
        }.start();
    }

    @Override
    public void serviceClick() {
        if (hdmKeychainLimit) {
            return;
        }
        if (singular.isInSingularMode()) {
            return;
        }
        if (coldRoot == null && hdmBid == null) {
            isServerClicked = true;
            coldClick();
            return;
        }

        new Thread() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dp.beginProgress();
                    }
                });
                try {
                    initHDMBidFromColdRoot();
                    final String preSign = hdmBid.getPreSignString();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dp.endProgress();
                            HDMServerUnsignedQRCodePanel hdmServerUnsignedQRCodePanel = new HDMServerUnsignedQRCodePanel(new IScanQRCode() {
                                @Override
                                public void handleResult(String result, IReadQRCode readQRCode) {
                                    readQRCode.close();
                                    serverQRCode(result);
                                }
                            }, preSign);
                            hdmServerUnsignedQRCodePanel.showPanel();
                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                    String msg = LocaliserUtils.getString("network_or_connection_error");
                    if (e instanceof Http400Exception) {
                        msg = ExceptionUtil.getHDMHttpExceptionMessage(((Http400Exception) e)
                                .getErrorCode());
                    }
                    final String m = msg;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dp.endProgress();
                            new MessageDialog(m).showMsg();

                        }
                    });

                }
            }
        }.start();

    }


    public void xrandomResult() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (singular.isInSingularMode()) {
                            singular.xrandomFinished();
                        } else if (AddressManager.getInstance().getHdmKeychain() != null) {
                            if (delegate != null) {
                                delegate.moveToCold(true);
                            }
                        }

                    }
                });
            }
        }).start();


    }

    @Override
    public void scanColdResult(String result) {
        try {
            coldRoot = Utils.hexStringToByteArray(result);
            final int count = AbstractApp.bitherjSetting.hdmAddressPerSeedPrepareCount() -
                    AddressManager.getInstance().getHdmKeychain().uncompletedAddressCount();
            new Thread() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dp.beginProgress();
                        }
                    });
                    try {

                        if (count > 0) {
                            SecureCharSequence password = passwordGetter.getPassword();
                            if (password == null) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        dp.endProgress();
                                    }
                                });
                                return;
                            }
                            AddressManager.getInstance().getHdmKeychain().prepareAddresses
                                    (count, password, Arrays.copyOf(coldRoot, coldRoot.length));
                        }
                        initHDMBidFromColdRoot();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                dp.endProgress();
                                if (isServerClicked) {
                                    serviceClick();
                                } else {
                                    if (delegate != null) {
                                        delegate.moveToServer(true);
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        coldRoot = null;
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                dp.endProgress();
                                new MessageDialog(LocaliserUtils.getString("hdm_keychain_add_scan_cold")).showMsg();
                            }
                        });
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            coldRoot = null;
            new MessageDialog(LocaliserUtils.getString("hdm_keychain_add_scan_cold")).showMsg();
        }

    }

    @Override
    public void serverQRCode(final String result) {
        if (hdmBid == null) {
            return;
        }

        new Thread() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dp.beginProgress();
                    }
                });
                try {
                    SecureCharSequence password = passwordGetter.getPassword();
                    if (password == null) {
                        return;
                    }
                    hdmBid.setSignature(result, password);
                    PeerUtil.stopPeer();
                    final HDMKeychain keychain = AddressManager.getInstance().getHdmKeychain();
                    final List<HDMAddress> as = keychain.completeAddresses(1, password,
                            new HDMKeychain.HDMFetchRemotePublicKeys() {
                                @Override
                                public void completeRemotePublicKeys(CharSequence password,
                                                                     List<HDMAddress.Pubs>
                                                                             partialPubs) {
                                    try {
                                        HDMKeychain.getRemotePublicKeys(hdmBid, password,
                                                partialPubs);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        String msg = LocaliserUtils.getString("network_or_connection_error");
                                        if (e instanceof Http400Exception) {
                                            msg = ExceptionUtil.getHDMHttpExceptionMessage((
                                                    (Http400Exception) e).getErrorCode());
                                        }
                                        final String m = msg;
                                        SwingUtilities.invokeLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                dp.endProgress();
                                                new MessageDialog(m).showMsg();
                                            }
                                        });
                                    }
                                }
                            });

                    PeerUtil.startPeer();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dp.beginProgress();
                            if (as.size() > 0) {
                                if (delegate != null) {
                                    delegate.moveToFinal(true);
                                }
                            }

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    final Exception finalE = e;

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dp.endProgress();
                            String msg = LocaliserUtils.getString("hdm_keychain_add_sign_server_qr_code_error");
                            if (finalE instanceof Http400Exception) {
                                msg = ExceptionUtil.getHDMHttpExceptionMessage((
                                        (Http400Exception) finalE).getErrorCode());

                            }
                            new MessageDialog(msg).showMsg();
                        }
                    });
                }
            }


        }.start();
    }

    @Override
    public void beforePasswordDialogShow() {
        dp.endProgress();
    }

    @Override
    public void afterPasswordDialogDismiss() {
        dp.endProgress();
    }


}
