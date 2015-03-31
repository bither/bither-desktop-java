
package net.bither.utils;

public class OclVanitygen {
    static {
        System.loadLibrary("oclvanitygen");
    }

    protected static native int oclGenerateAddress(String address, boolean ignore);

    protected static native String[] oclGetPrivateKey();

    protected static native double[] oclGetProgress();

    protected static native String oclGetDifficulty();

    protected static native String[] getDevices();

    protected static native void oclQuit();

    public static boolean ISRUNNING = true;


    public static void main(String[] args) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                ISRUNNING = true;
                System.out.println("test");
                oclGenerateAddress("1PQ,0:0", true);
                String[] strings = oclGetPrivateKey();
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
                    double[] doubleArray = oclGetProgress();
                    if (doubleArray != null) {
                        String string = "";
                        for (int i = 0; i < doubleArray.length; i++) {
                            string = string + "," + doubleArray[i];
                            if (i == doubleArray.length - 1) {
                                System.out.println(string);
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
