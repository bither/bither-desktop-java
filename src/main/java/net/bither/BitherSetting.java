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

package net.bither;

import net.bither.utils.DateUtils;

import java.awt.*;
import java.text.ParseException;
import java.util.Date;

public class BitherSetting {

    public static final String VERSION = "1.3.7";
    public static final int VERSION_CODE = 137;

    public static final int NOT_RELEVANT_PERCENTAGE_COMPLETE = -1;

    public static final String USER_PROPERTIES_HEADER_TEXT = "bither";
    public static final String MAIN_DIR = "Bither";

    public static final String BLOCKCHAIN_INFO_PREFIX = "http://blockchain.info/tx-index/";
    public static final String BLOCKMETA_TRANSACTION_PREFIX = "http://www.blockmeta.com/tx/";


    public static Date genesisBlockCreationDate;

    public static final int TOOLTIP_DISMISSAL_DELAY = 12000; // millisecs
    public static final int TABLE_BORDER = 3;
    public static final int STATUS_WIDTH_DELTA = 8;
    public static final int MINIMUM_ICON_HEIGHT = 16;
    public static final int HEIGHT_DELTA = 3;
    public static final String THREE_SPACER = "   "; // 3 spaces

    public static final double PROPORTION_OF_VERTICAL_SCREEN_TO_FILL = 0.75D;
    public static final double PROPORTION_OF_HORIZONTAL_SCREEN_TO_FILL = 0.82D;


    public static final String EXAMPLE_MEDIUM_FIELD_TEXT = "Typical text 00.12345678 BTC (000.01 XYZ)";


    public static final int WALLET_WIDTH_DELTA = 25;


    public static Color CREDIT_FOREGROUND_COLOR = Color.GREEN.darker().darker();
    public static Color DEBIT_FOREGROUND_COLOR = Color.RED.darker();


    public static final String USER_LANGUAGE_IS_DEFAULT = "isDefault";


    public static final int SCROLL_INCREMENT = 12;

    // User preference font.
    public static final String FONT = "font";

    public static final int PASSWORD_LENGTH_MAX = 43;
    public static final int PASSWORD_LENGTH_MIN = 6;

    public enum ECKeyType {
        Compressed, UNCompressed
    }


    static {
        try {
            genesisBlockCreationDate = DateUtils.getDateTimeForTimeZone("2009-01-03 18:15:05");
        } catch (ParseException e) {
            // Will never happen.
            e.printStackTrace();
        }
    }


}
