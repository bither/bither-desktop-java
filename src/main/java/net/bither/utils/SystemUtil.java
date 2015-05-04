package net.bither.utils;

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

    private static void callSystemGC() {

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

}
