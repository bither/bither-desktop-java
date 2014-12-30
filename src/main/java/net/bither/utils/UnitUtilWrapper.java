/*
 *
 *  * Copyright 2014 http://Bither.net
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package net.bither.utils;


import net.bither.bitherj.utils.UnitUtil;
import net.bither.bitherj.utils.UnitUtil.BitcoinUnit;

import javax.swing.*;

/**
 * Created by songchenwen on 14-11-11.
 */
public class UnitUtilWrapper {
    public static enum BitcoinUnitWrapper {
        BTC(BitcoinUnit.BTC), bits(BitcoinUnit.bits),;
        public BitcoinUnit unit;
        public long satoshis;
        public String imgSlim;
        public String imgBold;
        public String imgBlack;
        public int boldAfterDot;
        private ImageIcon bmpSlim;
        private ImageIcon bmpBold;
        private ImageIcon bmpBlack;

        BitcoinUnitWrapper(BitcoinUnit unit) {
            this.unit = unit;
            satoshis = unit.satoshis;
            switch (unit) {
                case BTC:
                    boldAfterDot = 2;
                    imgSlim = "/images/symbol_btc_slim.png";
                    imgBold = "/images/symbol_btc.png";
                    imgBlack = "/images/symbol_btc_slim_black.png";
                    break;
                case bits:
                    boldAfterDot = 0;
                    imgSlim = "/images/symbol_bits_slim.png";
                    imgBold = "/images/symbol_bits.png";
                    imgBlack = "/images/symbol_bits_slim_black.png";
                    break;
            }
        }

        public ImageIcon getBmpSlim() {
            if (bmpSlim == null) {
                bmpSlim = ImageLoader.createImageIcon(imgSlim);
            }
            return bmpSlim;
        }

        public ImageIcon getBmpBold() {
            if (bmpBold == null) {
                bmpBold = ImageLoader.createImageIcon(imgBold);
            }
            return bmpBold;
        }

        public ImageIcon getBmpBlack() {
            if (bmpBlack == null) {
                bmpBlack = ImageLoader.createImageIcon(imgBlack);
            }
            return bmpBlack;
        }

        public static final BitcoinUnitWrapper getWrapper(BitcoinUnit unit) {
            switch (unit) {
                case BTC:
                    return BitcoinUnitWrapper.BTC;
                case bits:
                    return BitcoinUnitWrapper.bits;
            }
            return BitcoinUnitWrapper.BTC;
        }
    }

    private static final int MinBlackValue = 0;

    private static BitcoinUnitWrapper unit() {
        return BitcoinUnitWrapper.getWrapper(BitcoinUnit.BTC);
    }

    public static String formatValue(final long value) {
        return UnitUtil.formatValue(value, unit().unit);
    }
}
