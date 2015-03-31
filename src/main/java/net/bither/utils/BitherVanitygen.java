package net.bither.utils;

import net.bither.bitherj.utils.Utils;
import net.bither.model.OpenCLDevice;

import java.util.ArrayList;
import java.util.List;

public class BitherVanitygen {

    private String input;
    private String openclConfig;
    private boolean useOpencl;
    private boolean igoreCase;

    public BitherVanitygen(String input, boolean useOpencl, boolean igoreCase, String openclConfig) {
        this.input = input;
        this.useOpencl = useOpencl;
        this.igoreCase = igoreCase;
        this.openclConfig = openclConfig;
    }

    public static List<OpenCLDevice> getCLDevices() {
        List<OpenCLDevice> openCLDevices = new ArrayList<OpenCLDevice>();
        String[] strings = OclVanitygen.getDevices();
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


    public int generateAddress() {
        if (useOpencl) {
            return OclVanitygen.oclGenerateAddress(input + "," + openclConfig, igoreCase);
        } else {
            return Vanitygen.generateAddress(input, igoreCase);

        }

    }

    public double[] getProgress() {
        if (useOpencl) {
            return OclVanitygen.oclGetProgress();

        } else {
            return Vanitygen.getProgress();
        }

    }

    public String getDifficulty() {
        if (useOpencl) {
            return OclVanitygen.oclGetDifficulty();
        } else {
            return Vanitygen.getDifficulty();
        }

    }

    public String[] getPrivateKey() {
        if (useOpencl) {
            return OclVanitygen.oclGetPrivateKey();
        } else {
            return Vanitygen.getPrivateKey();
        }


    }

    public void stop() {

    }
}
