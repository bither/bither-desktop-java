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

package net.bither.factory;


import net.bither.Bither;
import net.bither.BitherSetting;
import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.crypto.DumpedPrivateKey;
import net.bither.bitherj.crypto.ECKey;
import net.bither.bitherj.crypto.PasswordSeed;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.PrivateKeyUtil;
import net.bither.bitherj.utils.TransactionsUtil;
import net.bither.preference.UserPreference;
import net.bither.utils.KeyUtil;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.dialogs.ProgressDialog;
import net.bither.bitherj.BitherjSettings.AddressType;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ImportPrivateKey {
    public enum ImportPrivateKeyType {
        Text, BitherQrcode, Bip38
    }
    private String content;
    private SecureCharSequence password;
    private ImportPrivateKeyType importPrivateKeyType;

    private ProgressDialog progressDialog;

    public ImportPrivateKey( ImportPrivateKeyType importPrivateKeyType
            , String content, SecureCharSequence password) {
        this.content = content;
        this.password = password;

        this.importPrivateKeyType = importPrivateKeyType;

        progressDialog = new ProgressDialog();
    }

    public void importPrivateKey() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ECKey ecKey = getEckey();
                try {
                    showPd();
                    if (ecKey == null) {

                        if (importPrivateKeyType == ImportPrivateKeyType.BitherQrcode) {
                            new MessageDialog(LocaliserUtils.getString("password.wrong")).showMsg();

                        } else {
                            new MessageDialog(LocaliserUtils.getString("import.private.key.qr.code.failed")).showMsg();
                        }
                        disposePd();
                        return;
                    }
                    List<String> addressList = new ArrayList<String>();
                    addressList.add(ecKey.toAddress());
                    if (UserPreference.getInstance().getAppMode() == BitherjSettings.AppMode.HOT) {
                        checkAddress(ecKey, addressList);
                    } else {
                        addECKey(ecKey);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    new MessageDialog(LocaliserUtils.getString("import.private.key.qr.code.failed")).showMsg();
                } finally {
                    password.wipe();
                    if (ecKey != null) {
                        ecKey.clearPrivateKey();
                    }
                    disposePd();
                }

            }
        }).start();

    }

    private void showPd() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressDialog.pack();
                progressDialog.setVisible(true);


            }
        });
    }

    private void disposePd() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressDialog.dispose();
            }
        });
    }

    private void checkAddress(ECKey ecKey, List<String> addressList) {
        try {
            AddressType addressType = TransactionsUtil.checkAddress(addressList);
            handlerResult(ecKey, addressType);
        } catch (Exception e) {
            new MessageDialog(LocaliserUtils.getString("network.or.connection.error")).showMsg();

        }

    }

    private void addECKey(ECKey ecKey) {
        String encryptedPrivateString;
        if (importPrivateKeyType == ImportPrivateKeyType.BitherQrcode) {
            encryptedPrivateString = QRCodeUtil.getNewVersionEncryptPrivKey(content);
        } else {
            ecKey = PrivateKeyUtil.encrypt(ecKey, password);
            encryptedPrivateString = PrivateKeyUtil.getEncryptedString(ecKey);
        }
        Address address = new Address(ecKey.toAddress(), ecKey.getPubKey(), encryptedPrivateString
                , ecKey.isFromXRandom());
        if (AddressManager.getInstance().getWatchOnlyAddresses().contains(address)) {
            password.wipe();
            new MessageDialog(LocaliserUtils.getString("import.private.key.qr.code.failed.monitored")).showMsg();
            return;
        } else if (AddressManager.getInstance().getPrivKeyAddresses().contains(address)) {
            password.wipe();

            new MessageDialog(LocaliserUtils.getString("import.private.key.qr.code.failed.duplicate")).showMsg();
            return;

        } else {
            if (importPrivateKeyType == ImportPrivateKeyType.BitherQrcode) {
                PasswordSeed passwordSeed = UserPreference.getInstance().getPasswordSeed();
                if (passwordSeed != null && !passwordSeed.checkPassword(password)) {
                    password.wipe();

                    new MessageDialog(LocaliserUtils.getString("import.private.key.qr.code.failed.different.password")).showMsg();
                    return;
                }
            } else {
                password.wipe();
            }
            List<Address> addressList = new ArrayList<Address>();
            addressList.add(address);
            KeyUtil.addAddressListByDesc(addressList);
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Bither.getCoreController().fireRecreateAllViews(true);
            }
        });


    }


    private void handlerResult(ECKey ecKey
            , AddressType addressType) {
        switch (addressType) {
            case Normal:
                addECKey(ecKey);
                break;
            case SpecialAddress:
                new MessageDialog(LocaliserUtils.getString("import.private.key.failed.special.address")).showMsg();

                break;
            case TxTooMuch:
                new MessageDialog(LocaliserUtils.getString("import.private.key.failed.tx.too.mush")).showMsg();

                break;
        }
    }


    private ECKey getEckey() {
        ECKey ecKey = null;
        DumpedPrivateKey dumpedPrivateKey = null;
        try {
            switch (this.importPrivateKeyType) {
                case Text:
                    dumpedPrivateKey = new DumpedPrivateKey(this.content);
                    ecKey = dumpedPrivateKey.getKey();
                    break;
                case BitherQrcode:
                    ecKey = PrivateKeyUtil.getECKeyFromSingleString(content, password);
                    break;
                case Bip38:
                    dumpedPrivateKey = new DumpedPrivateKey(content);
                    ecKey = dumpedPrivateKey.getKey();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dumpedPrivateKey != null) {
                dumpedPrivateKey.clearPrivateKey();
            }
        }
        return ecKey;
    }

}
