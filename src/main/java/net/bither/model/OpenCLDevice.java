package net.bither.model;

/**
 * Created by songchenwen on 15/3/27.
 */
public class OpenCLDevice {

    private final String AMD_DEVICE = "amd";
    private final String NVIDIA_DEVICE = "nvidia";
    private int platform;
    private int device;
    private long keyPerSecond;
    private String platformName;
    private String deviceName;

    public OpenCLDevice(int platform, int device, String platformName, String deviceName) {
        this.platform = platform;
        this.device = device;
        this.platformName = platformName;
        this.deviceName = deviceName;
    }

    public int getPlatform() {
        return platform;
    }

    public int getDevice() {
        return device;
    }

    public String getPlatformName() {
        return platformName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getConfigureString() {
        return "" + platform + ":" + device;
    }

    public void setKeyPerSecond(long keyPerSecond) {
        this.keyPerSecond = keyPerSecond;
    }

    public long getKeyPerSecond() {
        return keyPerSecond;
    }

    public boolean isGPU() {
        String deviceLowerName = this.deviceName.toLowerCase();
        if (deviceLowerName.contains(AMD_DEVICE)) {
            return true;
        }
        if (deviceLowerName.contains(NVIDIA_DEVICE)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OpenCLDevice) {
            OpenCLDevice o = (OpenCLDevice) obj;
            return o.getDevice() == getDevice() && o.getPlatform() == getPlatform();
        }
        return false;
    }
}
