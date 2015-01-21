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
import net.bither.bitherj.api.GetExchangeTickerApi;
import net.bither.model.Ticker;
import net.bither.preference.UserPreference;
import net.bither.viewsystem.froms.MenuBar;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class BitherTimer {

    private MenuBar menuBarFrom;
    private Thread thread = null;

    private boolean isStop = false;

    public BitherTimer(MenuBar barFrom) {
        this.menuBarFrom = barFrom;
    }


    public void startTimer() {
        if (UserPreference.getInstance().getAppMode() == BitherjSettings.AppMode.COLD) {
            return;
        }
        if (thread == null) {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!isStop) {
                        getExchangeTicker();
                        try {
                            Thread.sleep(1 * 60 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
            thread.start();
        }
    }

    public void stopTimer() {
        isStop = true;
    }

    private void getExchangeTicker() {
        try {

            File file = FileUtil.getTickerFile();
            @SuppressWarnings("unchecked")
            List<Ticker> cacheList = (List<Ticker>) FileUtil.deserialize(file);
            if (cacheList != null) {
                MarketUtil.setTickerList(cacheList);
                this.menuBarFrom.updateTicker();

            }
            GetExchangeTickerApi getExchangeTickerApi = new GetExchangeTickerApi();
            getExchangeTickerApi.handleHttpGet();
            ExchangeUtil.setCurrenciesRate(getExchangeTickerApi.getCurrenciesRate());
            String str = getExchangeTickerApi.getResult();
            List<Ticker> tickers = Ticker.formatList(new JSONObject(str));
            if (tickers != null && tickers.size() > 0) {
                MarketUtil.setTickerList(tickers);
                FileUtil.serializeObject(file, tickers);
                this.menuBarFrom.updateTicker();
                //todo  BroadcastUtil.sendBroadcastMarketState(tickers);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
