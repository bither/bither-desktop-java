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

public class BlockNotificationCenter {
    public interface IBlockListener {
        public void blockChange();
    }

    private static List<IBlockListener> blockChangeList = new ArrayList<IBlockListener>();

    public static void notificationBlockChange() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (IBlockListener blockChange : blockChangeList) {
                    blockChange.blockChange();
                }
            }
        });

    }

    public static void addBlockChange(IBlockListener blockChange) {
        if (!blockChangeList.contains(blockChange)) {
            blockChangeList.add(blockChange);
        }
    }

    public static void removeBlockChange(IBlockListener blockChange) {
        blockChangeList.remove(blockChange);
    }
}
