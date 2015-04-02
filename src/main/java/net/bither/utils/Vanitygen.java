package net.bither.utils;

public class Vanitygen {
    static {
        System.loadLibrary("vanitygen");
    }

    public static native int generateAddress(String address, boolean ignore);

    public static native String[] getPrivateKey();

    public static native double[] getProgress();

    public static native void quit();

    public static boolean ISRUNNING = true;

    public static void main(String[] args) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                ISRUNNING = true;
                generateAddress("1PQPP", true);
                String[] strings = getPrivateKey();
                if (strings != null) {
                    for (String str : strings)
                        System.out.println("java :" + str);
                }
                ISRUNNING = false;

            }
        }
        ).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (ISRUNNING) {
                    double[] doubleArray = getProgress();
                    if (doubleArray != null) {
                        String string = "";
                        for (int i = 0; i < doubleArray.length; i++) {
                            string = string + "," + doubleArray[i];
                            if (i == doubleArray.length - 1) {
                                // System.out.println(string);
                                string = "";
                            }
                        }
                    }
                    try {
                        Thread.sleep(200);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }
}
