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
import net.bither.bitherj.core.Address;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.factory.ImportPrivateKey;
import net.bither.utils.KeyUtil;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.dialogs.DialogProgress;
import net.bither.viewsystem.dialogs.MessageDialog;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ImportPrivateKeyDesktop extends ImportPrivateKey {
    public interface ImportPrivateKeyListener {
        public void importSuccess();
    }

    private DialogProgress dialogProgress;
    private ImportPrivateKeyListener importPrivateKeyListener;


    public ImportPrivateKeyDesktop(ImportPrivateKeyType importPrivateKeyType, String content,
                                   SecureCharSequence password, ImportPrivateKeyListener importPrivateKeyListener) {
        super(importPrivateKeyType, content, password);
        dialogProgress = new DialogProgress();
        this.importPrivateKeyListener = importPrivateKeyListener;

    }

    public ImportPrivateKeyDesktop(ImportPrivateKeyType importPrivateKeyType, String content, SecureCharSequence password) {
        this(importPrivateKeyType, content, password, null);

    }

    @Override
    public void importError(final int errorCode) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if ( dialogProgress!=null){
                    dialogProgress.dispose();
                }
                String message;
                switch (errorCode) {
                    case PASSWORD_WRONG:
                        message = LocaliserUtils.getString("password_wrong");
                        break;
                    case NETWORK_FAILED:
                        message = LocaliserUtils.getString("network_or_connection_error");
                        break;
                    case CAN_NOT_IMPORT_BITHER_COLD_PRIVATE_KEY:
                        message = LocaliserUtils.getString("import_private_key_qr_code_failed_monitored");
                        break;
                    case PRIVATE_KEY_ALREADY_EXISTS:
                        message = LocaliserUtils.getString("import_private_key_qr_code_failed_duplicate");
                        break;
                    case PASSWORD_IS_DIFFEREND_LOCAL:
                        message = LocaliserUtils.getString("import_private_key_qr_code_failed_different_password");
                        break;
                    case CONTAIN_SPECIAL_ADDRESS:
                        message = LocaliserUtils.getString("import_private_key_failed_special_address");
                        break;
                    case TX_TOO_MUCH:
                        message = LocaliserUtils.getString("import_private_key_failed_tx_too_mush");
                        break;
                    default:
                        message = LocaliserUtils.getString("import_private_key_qr_code_failed");

                        break;
                }
                new MessageDialog(message).showMsg();

            }
        });

    }


    public void importPrivateKey() {
        new Thread() {


            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dialogProgress.pack();
                        dialogProgress.setVisible(true);
                    }
                });
                Address address = initPrivateKey();
                if (address != null) {
                    List<Address> addressList = new ArrayList<Address>();
                    addressList.add(address);
                    KeyUtil.addAddressListByDesc(addressList);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dialogProgress.dispose();
                            Bither.refreshFrame();
                            new MessageDialog(LocaliserUtils.getString("import_private_key_qr_code_success")).showMsg();
                            if (importPrivateKeyListener != null) {
                                importPrivateKeyListener.importSuccess();
                            }
                        }
                    });
                }
            }

        }.start();
    }


}
