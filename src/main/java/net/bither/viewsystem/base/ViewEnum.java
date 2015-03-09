/**
 * Copyright 2011 multibit.org
 *
 * Licensed under the MIT license (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at
 *
 * http://opensource.org/licenses/mit-license.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.bither.viewsystem.base;

import net.bither.bitherj.BitherjSettings;
import net.bither.preference.UserPreference;

public enum ViewEnum {
    SAME_VIEW, // Not a real view - used to forward to the same view as calling
    UNKNOWN_VIEW,
    TRANSACTIONS_VIEW,
    COLD_WALLET_VIEW,
    SEND_BITCOIN_CONFIRM_VIEW, // obsolete - now done with Swing dialog


    OPEN_WALLET_VIEW, // obsolete - now done with Swing dialog
    SAVE_WALLET_AS_VIEW, // obsolete - now done with Swing dialog
    VALIDATION_ERROR_VIEW, // obsolete - now done with Swing dialog
    YOUR_WALLETS_VIEW,
    CREATE_BULK_ADDRESSES_VIEW, // obsolete


    ;

    public static ViewEnum DEFAULT_VIEW() {
        if (UserPreference.getInstance().getAppMode() == BitherjSettings.AppMode.COLD) {
            return COLD_WALLET_VIEW;
        }
        return TRANSACTIONS_VIEW;
    }


}
