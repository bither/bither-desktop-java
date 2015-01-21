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
import net.bither.model.Market;
import net.bither.model.Ticker;
import net.bither.preference.UserPreference;

import java.util.ArrayList;
import java.util.List;

public class MarketUtil {


    public static String[] marketNames = new String[]{
            LocaliserUtils.getString("marketName.bitstamp"), LocaliserUtils.getString("marketName.btce"),
            LocaliserUtils.getString("marketName.huobi"),
            LocaliserUtils.getString("marketName.okcoin"), LocaliserUtils.getString("marketName.btcchina"),
            LocaliserUtils.getString("marketName.chbtc"), LocaliserUtils.getString("marketName.bitfinex")
            , LocaliserUtils.getString("marketName.796")
    };


    public static MarketType getMarketType(int index) {
        index = index + 1;
        switch (index) {
            case 1:
                return MarketType.BITSTAMP;
            case 2:
                return MarketType.BTCE;
            case 3:
                return MarketType.HUOBI;
            case 4:
                return MarketType.OKCOIN;
            case 5:
                return MarketType.BTCCHINA;
            case 6:
                return MarketType.CHBTC;
            case 7:
                return MarketType.BITFINEX;
            case 8:
                return MarketType.MARKET796;

        }
        return MarketType.HUOBI;
    }

    public static String getMarketName(MarketType marketType) {
        String name = " ";
        switch (marketType) {
            case HUOBI:
                return LocaliserUtils.getString("marketName.huobi");

            case BITSTAMP:
                return LocaliserUtils.getString("marketName.bitstamp");
            case BTCE:
                return LocaliserUtils.getString("marketName.btce");
            case OKCOIN:
                return LocaliserUtils.getString("marketName.okcoin");
            case CHBTC:
                return LocaliserUtils.getString("marketName.chbtc");
            case BTCCHINA:
                return LocaliserUtils.getString("marketName.btcchina");
            case BITFINEX:
                return LocaliserUtils.getString("marketName.bitfinex");
            case MARKET796:
                return LocaliserUtils.getString("marketName.796");
            default:

                break;
        }
        return name;
    }


    private static ArrayList<Market> markets = new ArrayList<Market>();

    public static ArrayList<Market> getMarkets() {
        synchronized (markets) {
            if (markets.size() == 0) {
                for (MarketType marketType : MarketType.values()) {
                    markets.add(new Market(marketType));
                }
            }
            return markets;
        }
    }

    public static Market getMarket(MarketType marketType) {
        if (markets.size() == 0) {
            getMarkets();
        }
        synchronized (markets) {

            if (markets.size() > 0) {
                for (Market market : markets) {
                    if (market.getMarketType() == marketType) {
                        return market;
                    }
                }
            }
            return null;
        }

    }

    public static Market getDefaultMarket() {
        MarketType marketType = UserPreference.getInstance()
                .getDefaultMarket();
        Market market = getMarket(marketType);
        return market;
    }

    public static Ticker getTickerOfDefaultMarket() {
        Market market = getDefaultMarket();
        if (market != null) {
            return market.getTicker();
        }
        return null;

    }

    public static void setTickerList(List<Ticker> tickerList) {
        if (tickerList != null && tickerList.size() > 0) {
            synchronized (markets) {
                for (Ticker ticker : tickerList) {
                    Market market = getMarket(ticker.getMarketType());
                    if (market != null) {
                        market.setTicker(ticker);
                    }
                }
            }
        }

    }
}
