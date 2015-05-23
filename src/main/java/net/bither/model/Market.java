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

package net.bither.model;


import net.bither.bitherj.BitherjSettings.MarketType;
import net.bither.utils.MarketUtil;


public class Market {

    private Ticker mTicker;
    private MarketType marketType;
    private boolean showDetail;

    public Market(MarketType marketType) {
        this.marketType = marketType;
    }



    public boolean isShowDetail() {

        return showDetail;
    }

    public void setShowDetail(boolean showDetail) {
        this.showDetail = showDetail;
    }

    public String getName() {
        return MarketUtil.getMarketName(getMarketType());
    }

    public MarketType getMarketType() {
        return marketType;
    }

    public int getMarketColor() {
        int resource = -1;
        switch (getMarketType()) {
//            case HUOBI:
//                resource = R.color.market_color_huobi;
//                break;
//            case BITSTAMP:
//                resource = R.color.market_color_bitstamp;
//                break;
//            case BTCE:
//                resource = R.color.market_color_btce;
//                break;
//            case OKCOIN:
//                resource = R.color.market_color_okcoin;
//                break;
//            case CHBTC:
//                resource = R.color.market_color_chbtc;
//                break;
//            case BTCCHINA:
//                resource = R.color.market_color_btcchina;
//                break;
//            case MARKET796:
//                resource = R.color.market_color_796;
//                break;
//            case BITFINEX:
//                resource = R.color.market_color_bitfinex;
//                break;
//            default:
//                resource = R.color.text_field_text_color;
//                break;
        }
        // return BitherApplication.mContext.getResources().getColor(resource);
        return resource;
    }


    public String getUrl() {
        return "http://" + getDomainName();
    }

    public String getDomainName() {
        switch (getMarketType()) {
            case HUOBI:
                return "huobi.com";
            case BITSTAMP:
                return "bitstamp.net";
            case BTCE:
                return "btc-e.com";
            case OKCOIN:
                return "okcoin.cn";
            case CHBTC:
                return "chbtc.com";
            case BTCCHINA:
                return "btcchina.com";
            case BITFINEX:
                return "bitfinex.com";
            case MARKET796:
                return "796.com";
            case BTCTRADE:
                return "btctrade.com";
            case COINBASE:
                return "coinbase.com";

            default:
                return null;
        }
    }

    public Ticker getTicker() {
        return mTicker;
    }

    public void setTicker(Ticker mTicker) {
        this.mTicker = mTicker;
    }


}
