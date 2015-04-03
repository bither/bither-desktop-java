
package net.bither.utils;

import net.bither.bitherj.utils.Utils;
import net.bither.model.OpenCLDevice;

import java.util.ArrayList;
import java.util.List;

public class OclVanitygen {
    static {
        System.loadLibrary("oclvanitygen");
    }

    public static native int oclGenerateAddress(String address, String equipment, boolean ignore);

    public static native String[] oclGetPrivateKey();

    public static native double[] oclGetProgress();

    private static native String[] getDevices();

    public static native void oclQuit();

    public static boolean ISRUNNING = true;

    public static List<OpenCLDevice> getCLDevices() {
        List<OpenCLDevice> openCLDevices = new ArrayList<OpenCLDevice>();
        String[] strings = getDevices();
        for (String str : strings) {
            int platform = 0;
            String platformName = "";
            int device = 0;
            String deviceName = "";
            String[] splitStrings = str.split(",");
            for (int i = 0; i < splitStrings.length; i++) {
                String[] platforms = splitStrings[i].split(":");
                if (i == 0) {
                    if (Utils.isInteger(platforms[0])) {
                        platform = Integer.valueOf(platforms[0]);

                    }
                    platformName = platforms[1];
                } else {
                    if (Utils.isInteger(platforms[0])) {
                        device = Integer.valueOf(platforms[0]);
                    }
                    deviceName = platforms[1];
                    OpenCLDevice openCLDevice = new OpenCLDevice(platform, device, platformName, deviceName);
                    openCLDevices.add(openCLDevice);
                }
            }

        }
        return openCLDevices;
    }


    public static void main(String[] args) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                ISRUNNING = true;
                String[] devices = getDevices();
                for (int i = 0; i < devices.length; i++) {
                    System.out.println("java-device:" + devices[i]);
                }
//                oclGenerateAddress("1PQPP", 0, true);
//                String[] strings = oclGetPrivateKey();
//                if (strings != null) {
//                    for (String str : strings)
//                        System.out.println("java :" + str);
//                }
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
