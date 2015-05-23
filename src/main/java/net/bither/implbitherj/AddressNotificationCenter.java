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

package net.bither.implbitherj;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class AddressNotificationCenter {
    public interface IAddressListener {
        public void addressLoadComplete();
    }

    private static List<IAddressListener> addressListenerList = new ArrayList<IAddressListener>();

    public static void notificationAddressLoadComplete() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (IAddressListener addressListener : addressListenerList) {
                    addressListener.addressLoadComplete();
                }
            }
        });

    }

    public static void addAddressListener(IAddressListener addressListener) {
        if (!addressListenerList.contains(addressListener)) {
            addressListenerList.add(addressListener);
        }
    }

    public static void removeAddressListener(IAddressListener addressListener) {
        addressListenerList.remove(addressListener);
    }
}
