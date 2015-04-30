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

package net.bither.runnable;

import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.HDMAddress;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.exception.PasswordException;
import net.bither.bitherj.exception.TxBuilderException;
import net.bither.bitherj.utils.Utils;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.WalletUtils;


public class CompleteTransactionRunnable extends BaseRunnable {
    private Address wallet;

    private SecureCharSequence password;
    private long amount;
    private String toAddress;

    private String changeAddress;
    private boolean toSign = false;
    private HDMAddress.HDMFetchOtherSignatureDelegate sigFetcher1;
    private HDMAddress.HDMFetchOtherSignatureDelegate sigFetcher2;


    static {
        WalletUtils.initTxBuilderException();
    }


    public CompleteTransactionRunnable(Address a, long amount, String toAddress,
                                       SecureCharSequence password) throws Exception {
        this(a, amount, toAddress, toAddress, password, null);
    }

    public CompleteTransactionRunnable(Address a, long amount, String toAddress,
                                       String changeAddress, SecureCharSequence password) throws Exception {
        this(a, amount, toAddress, changeAddress, password, null);
    }

    public CompleteTransactionRunnable(Address a, long amount, String toAddress,
                                       String changeAddress, SecureCharSequence password,
                                       HDMAddress.HDMFetchOtherSignatureDelegate
                                               otherSigFetcher1) throws Exception {
        this(a, amount, toAddress, changeAddress, password, otherSigFetcher1, null);
    }

    public CompleteTransactionRunnable(Address a, long amount, String toAddress,
                                       String changeAddress, SecureCharSequence password,
                                       HDMAddress.HDMFetchOtherSignatureDelegate
                                               otherSigFetcher1,
                                       HDMAddress.HDMFetchOtherSignatureDelegate
                                               otherSigFetcher2) throws Exception {
        boolean isHDM = otherSigFetcher1 != null || otherSigFetcher2 != null;
        this.amount = amount;
        this.toAddress = toAddress;
        this.password = password;
        sigFetcher1 = otherSigFetcher1;
        sigFetcher2 = otherSigFetcher2;
        if (isHDM) {
            wallet = a;
            toSign = true;
        } else if (password == null || password.length() == 0) {
            wallet = a;
            toSign = false;
        } else {
            if (a.hasPrivKey()) {
                wallet = a;
            } else {
                throw new Exception("address not with private key");
            }
            toSign = true;
        }
        if (!Utils.isEmpty(changeAddress)) {
            this.changeAddress = changeAddress;
        } else {
            this.changeAddress = wallet.getAddress();
        }
    }

    @Override
    public void run() {
        prepare();

        try {
            Tx tx = wallet.buildTx(amount, toAddress, changeAddress);
            if (tx == null) {
                error(0, LocaliserUtils.getString("send_failed"));

                return;
            }
            if (toSign) {
                if (wallet.isHDM()) {
                    if (sigFetcher1 != null && sigFetcher2 != null) {
                        ((HDMAddress) wallet).signTx(tx, password, sigFetcher1, sigFetcher2);
                    } else if (sigFetcher1 != null || sigFetcher2 != null) {
                        ((HDMAddress) wallet).signTx(tx, password,
                                sigFetcher1 != null ? sigFetcher1 : sigFetcher2);
                    } else {
                        throw new RuntimeException("need sig fetcher to sign hdm tx");
                    }
                } else {
                    wallet.signTx(tx, password);
                }
                if (password != null) {
                    password.wipe();
                }
                if (!tx.verifySignatures()) {
                    error(0, getMessageFromException(null));

                    return;
                }

            }
            success(tx);

        } catch (Exception e) {
            if (password != null) {
                password.wipe();
            }
            if (e instanceof HDMSignUserCancelExcetion) {
                error(0, null);
                return;
            }
            e.printStackTrace();
            String msg = getMessageFromException(e);
            error(0, msg);
        }

    }

    public String getMessageFromException(Exception e) {
        if (e != null && e instanceof TxBuilderException) {
            return e.getMessage();
        } else if (e != null && e instanceof PasswordException) {
            return LocaliserUtils.getString("password_wrong");
        } else {
            return LocaliserUtils.getString("send_failed");
        }
    }


    public static final class HDMServerSignException extends RuntimeException {

        public HDMServerSignException(String msg) {
            super(msg);
        }
    }

    public static final class HDMSignUserCancelExcetion extends RuntimeException {

    }
}
