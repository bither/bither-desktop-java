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

import net.bither.bitherj.BitherjSettings.MarketType;
import net.bither.bitherj.utils.Utils;
import net.bither.preference.UserPreference;
import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.AbstractMap;
import java.util.HashMap;

public class ExchangeUtil {
    private ExchangeUtil() {

    }

    public static String[] exchangeNames = new String[]{
            "USD", "CNY", "EUR", "GBP", "JPY", "KRW", "CAD", "AUD"
    };

    public static Currency getCurrency(int index) {
        if (index >= 0 && index < Currency.values().length) {
            return Currency.values()[index];
        } else {
            return Currency.USD;
        }
    }

    public enum Currency {
        USD("USD", "$"),
        CNY("CNY", StringEscapeUtils.unescapeHtml("&yen;")),
        EUR("EUR", "€"),
        GBP("GBP", "£"),
        JPY("JPY", StringEscapeUtils.unescapeHtml("&yen;")),
        KRW("KRW", "₩"),
        CAD("CAD", "C$"),
        AUD("AUD", "A$");

        private String symbol;
        private String name;

        private Currency(String name, String symbol) {
            this.name = name;
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        public String getName() {
            return name;
        }
    }

    private static double mRate = -1;
    private static AbstractMap<Currency, Double> mCurrenciesRate = null;

    public static void setCurrenciesRate(JSONObject currenciesRateJSon) throws Exception {
        mCurrenciesRate = parseCurrenciesRate(currenciesRateJSon);
        File file = FileUtil.getCurrenciesRateFile();
        Utils.writeFile(currenciesRateJSon.toString().getBytes(), file);
    }

    public static AbstractMap<Currency, Double> getCurrenciesRate() {
        if (mCurrenciesRate == null) {
            File file = FileUtil.getCurrenciesRateFile();
            String rateString = Utils.readFile(file);
            try {
                JSONObject json = new JSONObject(rateString);
                mCurrenciesRate = parseCurrenciesRate(json);
            } catch (JSONException ex) {
                mCurrenciesRate = null;
            }
        }
        return mCurrenciesRate;
    }

    private static AbstractMap<Currency, Double> parseCurrenciesRate(JSONObject json) throws JSONException {
        HashMap<Currency, Double> currencyDoubleHashMap = new HashMap<Currency, Double>();
        currencyDoubleHashMap.put(Currency.USD, 1.0);
        for (Currency currency : Currency.values()) {
            if (!json.isNull(currency.getName())) {
                currencyDoubleHashMap.put(currency, json.getDouble(currency.getName()));
            }
        }
        return currencyDoubleHashMap;
    }

    public static double getRate(Currency currency) {
        Currency defaultCurrency = UserPreference.getInstance()
                .getDefaultCurrency();
        double rate = 1;
        if (currency != defaultCurrency) {
            double preRate = getCurrenciesRate().get(currency);
            double defaultRate = getCurrenciesRate().get(defaultCurrency);
            rate = defaultRate / preRate;
        }
        return rate;
    }

    public static double getRate(MarketType marketType) {
        Currency defaultCurrency = UserPreference.getInstance()
                .getDefaultCurrency();
        Currency currency = getExchangeType(marketType);
        double rate = 1;
        if (currency != defaultCurrency) {
            double preRate = getCurrenciesRate().get(currency);
            double defaultRate = getCurrenciesRate().get(defaultCurrency);
            rate = defaultRate / preRate;
        }
        return rate;
    }

    public static double getRate() {
        Currency defaultCurrency = UserPreference.getInstance()
                .getDefaultCurrency();
        return getCurrenciesRate().get(defaultCurrency);
    }

    public static Currency getExchangeType(MarketType marketType) {
        switch (marketType) {
            case HUOBI:
            case OKCOIN:
            case BTCCHINA:
            case CHBTC:
            case BTCTRADE:
                return Currency.CNY;
            case MARKET796:
            case BTCE:
            case BITSTAMP:
            case BITFINEX:
            case COINBASE:
                return Currency.USD;
            default:
                break;
        }
        return Currency.CNY;

    }

}
