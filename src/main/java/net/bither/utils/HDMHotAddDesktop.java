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

import net.bither.BitherSetting;
import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.api.http.Http400Exception;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.HDMAddress;
import net.bither.bitherj.core.HDMKeychain;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.delegate.HDMHotAdd;
import net.bither.bitherj.delegate.HDMSingular;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.viewsystem.dialogs.ConfirmTaskDialog;
import net.bither.viewsystem.dialogs.DialogPassword;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.dialogs.ProgressDialog;
import net.bither.viewsystem.froms.HdmKeychainAddHotPanel;

import javax.swing.*;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

public class HDMHotAddDesktop extends HDMHotAdd {

    private ProgressDialog dp;

    public HDMHotAddDesktop(IHDMHotAddDelegate delegate, HDMSingular.HDMSingularDelegate hdmSingularUtilDelegate) {
        super(delegate);

        this.delegate = delegate;
        singularUtil = new HDMSingularDesktop(hdmSingularUtilDelegate);
        this.passwordGetter = new DialogPassword.PasswordGetter(this);
        dp = new ProgressDialog();

        hdmKeychainLimit = AddressManager.isHDMKeychainLimit();

    }


    @Override
    public void hotClick() {
        if (hdmKeychainLimit) {
            return;
        }
        if (singularUtil.isInSingularMode()) {
            return;
        }
        HdmKeychainAddHotPanel hdmKeychainAddHotPanel = new HdmKeychainAddHotPanel(new HdmKeychainAddHotPanel.DialogHdmKeychainAddHotDelegate() {

            @Override
            public void addWithXRandom() {
                //  HDMKeychainHotUEntropyActivity.passwordGetter = passwordGetter;
                if (singularUtil.shouldGoSingularMode()) {
                    //    HDMKeychainHotUEntropyActivity.singularUtil = singularUtil;
                } else {
                    singularUtil.runningWithoutSingularMode();
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
                        if (password == null) {
                            return;
                        }
                        if (singularUtil.shouldGoSingularMode()) {
                            singularUtil.setPassword(password);
                            singularUtil.generateEntropy();
                        } else {
                            singularUtil.runningWithoutSingularMode();
                            HDMKeychain keychain = new HDMKeychain(new SecureRandom(),
                                    password);
                            KeyUtil.setHDKeyChain(keychain);
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    dp.dispose();
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
        if (singularUtil.isInSingularMode()) {
            return;
        }
        ConfirmTaskDialog confirmTaskDialog = new ConfirmTaskDialog(LocaliserUtils.getString("hdm_keychain_add_scan_cold"),
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

    public void setCallScanColdResult(String result) {
        try {

            if (Utils.isEmpty(result) || !QRCodeUtil.verifyBitherQRCode(result)) {
                return;
            }
            coldRoot = Utils.hexStringToByteArray(result);
            final int count = BitherSetting.HDM_ADDRESS_PER_SEED_PREPARE_COUNT -
                    AddressManager.getInstance().getHdmKeychain().uncompletedAddressCount();
            if (passwordGetter.hasPassword() && count > 0) {

            }
            new Thread() {
                @Override
                public void run() {
                    try {
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
                                delegate.moveToServer(true);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        coldRoot = null;
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                new MessageDialog(LocaliserUtils.getString("hdm_keychain_add_scan_cold")).showMsg();
                            }
                        });
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            coldRoot = null;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new MessageDialog(LocaliserUtils.getString("hdm_keychain_add_scan_cold")).showMsg();
                }
            });
        }
    }

    @Override
    public void serviceClick() {
        if (hdmKeychainLimit) {
            return;
        }
        if (singularUtil.isInSingularMode()) {
            return;
        }
        if (coldRoot == null && hdmBid == null) {
            isServerClicked = true;
            coldClick();
            return;
        }
        if (dp == null) {
            dp = new ProgressDialog();
        }
        if (!dp.isShowing()) {
            dp.pack();
            dp.setVisible(true);
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    initHDMBidFromColdRoot();
                    final String preSign = hdmBid.getPreSignString();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dp.dispose();
//                            new DialogHDMServerUnsignedQRCode(activity, preSign,
//                                    new DialogHDMServerUnsignedQRCode
//                                            .DialogHDMServerUnsignedQRCodeListener() {
//                                        @Override
//                                        public void scanSignedHDMServerQRCode() {
//
//                                            if (delegate != null) {
//                                                delegate.callServerQRCode();
//                                            }
//
//                                        }
//
//                                        @Override
//                                        public void scanSignedHDMServerQRCodeCancel() {
//
//                                        }
//                                    }).show();
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
                            dp.dispose();
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
                        if (singularUtil.isInSingularMode()) {
                            singularUtil.xrandomFinished();
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
            final int count = BitherjSettings.HDM_ADDRESS_PER_SEED_PREPARE_COUNT -
                    AddressManager.getInstance().getHdmKeychain().uncompletedAddressCount();
            if (!dp.isShowing() && passwordGetter.hasPassword() && count > 0) {
                dp.pack();
                dp.setVisible(true);
            }
            new Thread() {
                @Override
                public void run() {
                    try {
                        if (count > 0) {
                            SecureCharSequence password = passwordGetter.getPassword();
                            if (password == null) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        dp.dispose();
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
                                dp.dispose();
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
                                dp.dispose();
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
        if (!dp.isShowing()) {
            dp.pack();
            dp.setVisible(true);
        }
        final ProgressDialog dd = dp;
        new Thread() {
            @Override
            public void run() {
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
                                                dd.dispose();
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
                            dp.setVisible(false);
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
                            dd.dispose();
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
        dp.dispose();
    }

    @Override
    public void afterPasswordDialogDismiss() {
        dp.dispose();
    }

    private void showDp() {
        dp.pack();
        dp.setVisible(true);
    }


}
