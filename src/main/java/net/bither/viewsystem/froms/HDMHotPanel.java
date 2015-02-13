package net.bither.viewsystem.froms;

import net.bither.BitherSetting;
import net.bither.bitherj.api.http.Http400Exception;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.HDMAddress;
import net.bither.bitherj.core.HDMBId;
import net.bither.bitherj.core.HDMKeychain;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.crypto.hd.DeterministicKey;
import net.bither.bitherj.crypto.hd.HDKeyDerivation;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.qrcode.HDMServerUnsignedQRCodePanel;
import net.bither.qrcode.IReadQRCode;
import net.bither.qrcode.IScanQRCode;
import net.bither.qrcode.SelectTransportQRCodePanel;
import net.bither.utils.ExceptionUtil;
import net.bither.utils.KeyUtil;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.DialogPassword;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.xrandom.HDMKeychainHotUEntropyDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

public class HDMHotPanel extends WizardPanel implements DialogPassword.PasswordGetter.PasswordGetterDelegate {
    private JButton btnHot;
    private JButton btnCold;
    private JButton btnService;
    private DialogPassword.PasswordGetter passwordGetter;

    private HDMBId hdmBid;

    private byte[] coldRoot;
    private boolean hdmKeychainLimit;

    public HDMHotPanel() {
        super(MessageKey.HDM, AwesomeIcon.FA_RECYCLE, false);
        passwordGetter = new DialogPassword.PasswordGetter(HDMHotPanel.this);
    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][][][][]", // Column constraints
                "[]10[][][][]" // Row constraints

        ));
        btnHot = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addHot();
            }
        }, MessageKey.HDM, AwesomeIcon.FA_RECYCLE);
        btnCold = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCold();

            }
        }, MessageKey.HDM, AwesomeIcon.FA_RECYCLE);
        btnService = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addService();
            }
        }, MessageKey.HDM, AwesomeIcon.FA_RECYCLE);

        panel.add(btnHot, "align center,cell 3 2 ,grow,wrap");
        panel.add(btnCold, "align center,cell 3 3 ,grow,wrap");
        panel.add(btnService, "align center,cell 3 4 ,grow,wrap");
        passwordGetter = new DialogPassword.PasswordGetter();


    }

    private void addHot() {
        HdmKeychainAddHotPanel hdmKeychainAddHotPanel = new HdmKeychainAddHotPanel(new HdmKeychainAddHotPanel.DialogHdmKeychainAddHotDelegate() {
            @Override
            public void addWithXRandom() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SecureCharSequence password = passwordGetter.getPassword();
                        if (password == null) {
                            return;
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                HDMKeychainHotUEntropyDialog hdmKeychainHotUEntropyDialog = new HDMKeychainHotUEntropyDialog(passwordGetter);
                                hdmKeychainHotUEntropyDialog.pack();
                                hdmKeychainHotUEntropyDialog.setVisible(true);
                                if (AddressManager.getInstance().getHdmKeychain() != null) {
                                    finishHot();
                                }

                            }
                        });

                    }
                }).start();

            }

            @Override
            public void addWithoutXRandom() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SecureCharSequence password = passwordGetter.getPassword();
                        if (password == null) {
                            return;
                        }
                        HDMKeychain keychain = new HDMKeychain(new SecureRandom(), password);
                        KeyUtil.setHDKeyChain(keychain);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (AddressManager.getInstance().getHdmKeychain() != null) {
                                    finishHot();
                                }
                            }
                        });

                    }
                }).start();


            }
        });
        hdmKeychainAddHotPanel.showPanel();

    }

    private void addCold() {
        SelectTransportQRCodePanel selectTransportQRCodePanel = new SelectTransportQRCodePanel(new IScanQRCode() {
            @Override
            public void handleResult(String result, IReadQRCode readQRCode) {
                try {
                    readQRCode.close();
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
                                        finishCold();
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
        }, true);
        selectTransportQRCodePanel.showPanel();

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


    private void addService() {
        if (hdmKeychainLimit) {
            return;
        }
        if (coldRoot == null && hdmBid == null) {
            addCold();
            return;
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
                            HDMServerUnsignedQRCodePanel hdmServerUnsignedQRCodePanel = new HDMServerUnsignedQRCodePanel(new IScanQRCode() {
                                @Override
                                public void handleResult(String result, IReadQRCode readQRCode) {
                                    readQRCode.close();
                                    if (Utils.isEmpty(result) || !QRCodeUtil.verifyBitherQRCode(result)) {
                                        return;
                                    }
                                    setSign(result);
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
                    new MessageDialog(msg).showMsg();
                }
            }
        }.start();


    }

    private void setSign(final String result) {

        if (hdmBid == null) {
            return;
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    SecureCharSequence password = passwordGetter.getPassword();
                    if (password == null) {
                        return;
                    }
                    hdmBid.setSignature(result, password);

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
                                                new MessageDialog(m).showMsg();
                                            }
                                        });
                                    }
                                }
                            });


                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {

                            if (as.size() > 0) {
                                finishService();
                            }

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    final Exception finalE = e;

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {

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

    private void finishHot() {

    }

    private void finishCold() {

    }

    private void finishService() {

    }

    @Override
    public void beforePasswordDialogShow() {

    }

    @Override
    public void afterPasswordDialogDismiss() {

    }
}
