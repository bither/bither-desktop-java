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


import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.Out;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.exception.ScriptException;
import net.bither.bitherj.exception.TxBuilderException;
import net.bither.bitherj.script.Script;
import net.bither.bitherj.utils.Utils;

import javax.annotation.Nonnull;
import java.util.List;

public class WalletUtils {

    public static final char CHAR_THIN_SPACE = '\u2009';

    public static String formatHash(@Nonnull final String address,
                                    final int groupSize, final int lineSize) {
        return formatHash(address, groupSize, lineSize, CHAR_THIN_SPACE);
    }

    public static boolean isInternal(@Nonnull final Tx tx) {
        if (tx.isCoinBase()) {
            return false;
        }

        final List<Out> outputs = tx.getOuts();
        if (outputs.size() != 1) {
            return false;
        }

        try {
            final Out output = outputs.get(0);
            final Script scriptPubKey = output.getScriptPubKey();
            if (!scriptPubKey.isSentToRawPubKey()) {
                return false;
            }

            return true;
        } catch (final ScriptException x) {
            return false;
        }
    }

    public static String formatHash(@Nonnull final String address, final int groupSize,
                                    final int lineSize, final char groupSeparator) {
        String str = "";
        final int len = address.length();
        for (int i = 0;
             i < len;
             i += groupSize) {
            final int end = i + groupSize;
            final String part = address.substring(i, end < len ? end : len);

            str += part;
            if (end < len) {
                final boolean endOfLine = lineSize > 0 && end % lineSize == 0;
                str += endOfLine ? '\n' : groupSeparator;
            }
        }

        return str;
    }

    public static void initTxBuilderException() {
        for (TxBuilderException.TxBuilderErrorType type : TxBuilderException.TxBuilderErrorType
                .values()) {
            String format = LocaliserUtils.getString("send_failed");
            switch (type) {
                case TxNotEnoughMoney:
                    format = LocaliserUtils.getString("send_failed_missing_btc");
                    break;
                case TxDustOut:
                    format = LocaliserUtils.getString("send_failed_dust_out_put");
                    break;
                case TxWaitConfirm:
                    format = LocaliserUtils.getString("send_failed_pendding");
                    break;
            }
            type.registerFormatString(format);
        }
    }

    public static Address findPrivateKey(String address) {
        for (Address bitherAddressWithPrivateKey : AddressManager.getInstance().getPrivKeyAddresses()) {

            if (Utils.compareString(address,
                    bitherAddressWithPrivateKey.getAddress())) {
                return bitherAddressWithPrivateKey;
            }
        }
        return null;
    }


}
