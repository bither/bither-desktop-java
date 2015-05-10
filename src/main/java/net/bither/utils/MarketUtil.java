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

import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.BitherjSettings.MarketType;
import net.bither.model.Market;
import net.bither.model.Ticker;
import net.bither.preference.UserPreference;

import java.util.ArrayList;
import java.util.List;

public class MarketUtil {


    public static class MarketTypeMode {
        private MarketType marketType;

        public MarketTypeMode(MarketType marketType) {
            this.marketType = marketType;
        }

        public MarketType getMarketType() {
            return this.marketType;
        }

        @Override
        public String toString() {
            return getMarketName(this.marketType);
        }
    }


    public static String getMarketName(MarketType marketType) {
        String name = " ";
        switch (marketType) {
            case HUOBI:
                return LocaliserUtils.getString("market_name_huobi");
            case BITSTAMP:
                return LocaliserUtils.getString("market_name_bitstamp");
            case BTCE:
                return LocaliserUtils.getString("market_name_btce");
            case OKCOIN:
                return LocaliserUtils.getString("market_name_okcoin");
            case CHBTC:
                return LocaliserUtils.getString("market_name_chbtc");
            case BTCCHINA:
                return LocaliserUtils.getString("market_name_btcchina");
            case BITFINEX:
                return LocaliserUtils.getString("market_name_bitfinex");
            case MARKET796:
                return LocaliserUtils.getString("market_name_796");
            case COINBASE:
                return LocaliserUtils.getString("market_name_btctrade");
            case BTCTRADE:
                return LocaliserUtils.getString("market_name_coinbase");
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
