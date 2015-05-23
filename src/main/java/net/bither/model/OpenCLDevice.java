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
