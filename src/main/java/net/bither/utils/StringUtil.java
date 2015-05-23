/*
 *
 *  Copyright 2014 http://Bither.net
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package net.bither.utils;

import net.bither.bitherj.utils.Base58;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {


    public static boolean checkBackupFileOfCold(String fileName) {
        Pattern pattern = Pattern.compile("[^-]{6,6}_[^-]{6,6}.bak");
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    public static String validBicoinAddressBegin(String input) {
        final Pattern PATTERN_BITCOIN_ADDRESS = Pattern.compile("[^"
                + new String(Base58.ALPHABET) + "]{1,30}");
        Matcher matcher = PATTERN_BITCOIN_ADDRESS.matcher(input);
        if (!matcher.find()) {
            return null;
        } else {
            return matcher.toMatchResult().group();
        }


    }


}
