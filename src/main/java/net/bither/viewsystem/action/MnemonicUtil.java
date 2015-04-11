/**
 * Copyright 2011 multibit.org
 *
 * Licensed under the MIT license (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://opensource.org/licenses/mit-license.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.bither.viewsystem.action;

import net.bither.utils.LocaliserUtils;

import javax.swing.*;
import java.util.MissingResourceException;

public class MnemonicUtil {


    /**
     * get the mnemonic key code for the passed in internationalisation key
     *
     * @param key
     * @return
     */
    public int getMnemonic(String key) {
        if (LocaliserUtils.getLocaliser() != null) {
            try {
                return KeyStroke.getKeyStroke(LocaliserUtils.getLocaliser().getString(key)).getKeyCode();
            } catch (NullPointerException npe) {
                return 0;
            } catch (ClassCastException cce) {
                return 0;
            } catch (MissingResourceException mre) {
                return 0;
            }
        } else {
            return 0;
        }
    }
}
