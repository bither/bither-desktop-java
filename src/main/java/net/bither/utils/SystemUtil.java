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

package net.bither.utils;

import net.bither.bitherj.utils.Utils;

/**
 * Created by nn on 15/5/4.
 */
public class SystemUtil {

    public static long maxUsed = 0;

    public static void maxUsedSize() {
        Runtime runtime = Runtime.getRuntime();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();

        long used = total - free;
        if (used > maxUsed) {
            maxUsed = used;
            runtime.runFinalization();
            runtime.gc();
            LogUtil.printlnOut("maxUsed:" + Math.round(maxUsed / 1e3));
        }
    }

    public static void callSystemGC() {

        Runtime runtime = Runtime.getRuntime();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long max = runtime.maxMemory();
        long used = total - free;
        LogUtil.printlnOut("   " + Math.round(used / 1e3) + " KB used before GC,total :" + Math.round(total / 1e3) + ",free:" + Math.round(free / 1e3));


        runtime.runFinalization();
        runtime.gc();
        runtime = Runtime.getRuntime();
        total = runtime.totalMemory();
        free = runtime.freeMemory();
        max = runtime.maxMemory();
        used = total - free;
        LogUtil.printlnOut(Math.round(used / 1e3) + " KB used after GC,total :" + Math.round(total / 1e3) + ",free:" + Math.round(free / 1e3));

    }

    public static int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static boolean isSystem32() {
        return Utils.compareString("32", System.getProperty("sun.arch.data.model"));
    }

}
