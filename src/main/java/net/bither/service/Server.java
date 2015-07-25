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

package net.bither.service;

import net.bither.Bither;
import net.bither.BitherSetting;
import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.DesktopHDMKeychain;
import net.bither.bitherj.core.PeerManager;
import net.bither.bitherj.utils.Utils;
import net.bither.preference.UserPreference;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Server {
    private static Thread instanceListenerThread;
    private static boolean shutdownSocket = false;

    public static void main() throws IOException {
        int port = 8326;
        final ServerSocket server = new ServerSocket(port);
        instanceListenerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!shutdownSocket) {
                        Socket socket = server.accept();
                        new Thread(new Task(socket)).start();
                    }
                    if (!server.isClosed()) {
                        server.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        instanceListenerThread.start();
    }

    static class Task implements Runnable {

        private Socket socket;

        public Task(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                handleSocket();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void handleSocket() throws Exception {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Writer writer = new OutputStreamWriter(socket.getOutputStream());
            String command;
            while ((command = br.readLine()) != null) {
                String result;
                if (command.equals("exit")) {
                    result = "bye";
                    writer.write(result);
                    writer.write("\n");
                    writer.flush();
                    break;
                } else {
                    result = this.execute(command);
                    writer.write(result);
                    writer.write("\n");
                    writer.flush();
                }
            }
            writer.close();
            br.close();
            socket.close();
        }

        private String execute(String command) {
            String[] sub = command.split(" ");

            if (Utils.compareString(sub[0], "hello")) {
                return "hello";
            } else if (Utils.compareString(sub[0], "getbalance")) {
                return this.getBalance();
            } else if (Utils.compareString(sub[0], "getaddress")) {
                return this.getAddress();
            } else if (Utils.compareString(sub[0], "sendtx") && sub.length >= 3) {
                if (sub.length == 4) {
                    return this.sendTx(sub[1], sub[2], Integer.valueOf(sub[3]));
                } else {
                    return this.sendTx(sub[1], sub[2], null);
                }
            } else if (Utils.compareString(sub[0], "startpeer")) {
                return this.startPeer();
            } else {
                return Utils.format("not support command: %s", command);
            }
        }

        private String getBalance() {
            return "0";
        }

        private String getAddress() {
            if (AddressManager.getInstance().hasDesktopHDMKeychain()
                    && UserPreference.getInstance().getAppMode() == BitherjSettings.AppMode.HOT) {
                String address = AddressManager.getInstance().getDesktopHDMKeychains().get(0).externalAddress();
                JSONObject result = new JSONObject();
                result.put("address", address);
                return result.toString();
            }
            JSONObject result = new JSONObject();
            result.put("result", false);
            return result.toString();
        }

        private String startPeer() {
            if (!PeerManager.instance().isRunning()) {
                PeerManager.instance().start();
            }
            JSONObject result = new JSONObject();
            result.put("result", true);
            return result.toString();
        }

        private String sendTx(String sendRequest, String password, @Nullable Integer feeBaseMode) {
            if (AddressManager.getInstance().hasDesktopHDMKeychain()
                    && UserPreference.getInstance().getAppMode() == BitherjSettings.AppMode.HOT) {
                DesktopHDMKeychain keychain = AddressManager.getInstance().getDesktopHDMKeychains().get(0);
                JSONObject jsonObject = new JSONObject(sendRequest);
                JSONArray addressesJSonArray = jsonObject.getJSONArray("addresses");
                JSONArray amountsJSonArray = jsonObject.getJSONArray("amounts");

                HashMap<String, Long> sr = new HashMap<String, Long>();
                for (int i = 0; i < addressesJSonArray.length(); i++) {
                    String tmp = amountsJSonArray.getString(i);
                    sr.put(addressesJSonArray.getString(i), amountsJSonArray.getLong(i));
                }
                try {
                    keychain.getSendRequestList().put(sr);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    JSONObject result = new JSONObject();
                    result.put("error", e.getMessage());
                    return result.toString();
                }
                JSONObject result = new JSONObject();
                result.put("result", true);
                return result.toString();
            }
            JSONObject result = new JSONObject();
            result.put("result", false);
            return result.toString();
        }
    }

    public static void shutdownSocket() {
        shutdownSocket = true;
    }
}
