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

import net.bither.BitherSetting;
import net.bither.bitherj.utils.Utils;
import net.bither.model.OpenCLDevice;
import net.bither.platform.builder.OSUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BitherVanitygen {

    private enum OUT_TYEP {
        OUT, ERROR;

    }

    public interface IVanitygenListener {
        public void onProgress(String speed, long generated, double progress, int nextPossibility, String nextTimePeriodSeconds);

        public void getAddress(String address);

        public void getPrivateKey(String privateKey, long useTime);

        public void onDifficulty(String difficile);

        public void error(String error);


    }

    private static final String ADDRESS_FORMAT = "Address: ";
    private static final String PRIVATE_FORMAT = "Privkey: ";
    private static final String PROGRESS_FORMAT = "\\[(.*)\\]\\[total (\\d+)\\]\\[Prob (\\d+\\.\\d+)%\\]\\[(\\d+)% in (.*)\\]";
    private static final String DIFFICULTY_FORMAT = "Difficulty: ";

    private static final String AVAILABLE_OPENCL_FORMAT = "\\d+:\\s*\\[.*\\]";
    private static final String MAC_LINUX_VANITYGEN = "vanitygen";
    private static final String MAC_LINUX_OCLVANITYGEN = "oclvanitygen";
    private static final String WINDOWS_VANITYGEN = "vanitygen.exe";
    private static final String WINDOWS_VANITYGEN_64 = "vanitygen64.exe";
    private static final String WINDOWS_OCLVANITYGEN = "oclvanitygen.exe";
    private static final String WINDOWS_OCLVANITYGEN_64 = "oclvanitygen64.exe";

    private static final String WINDOWS_PATH = "/vanitygen/windows/";
    private static final String MAC_OS_PATH = "/vanitygen/mac/";
    private static final String LINUX_PATH = "/vanitygen/linux/";

    private String input;
    private String openclConfig;
    private boolean useOpencl;
    private boolean igoreCase;
    private Process process = null;
    private IVanitygenListener vanitygenListener;
    private boolean generatedKey = false;
    private long beginTime;
    private int threadNum;
    private BitherSetting.ECKeyType ecKeyType;

    public BitherVanitygen(String input, boolean useOpencl, boolean igoreCase, int threadNum
            , BitherSetting.ECKeyType ecKeyType,
                           String openclConfig, IVanitygenListener vanitygenListener) {
        this.input = input;
        this.useOpencl = useOpencl;
        this.igoreCase = igoreCase;
        this.threadNum = threadNum;
        this.ecKeyType = ecKeyType;
        this.openclConfig = openclConfig;
        this.vanitygenListener = vanitygenListener;
    }

    public void generateAddress() {
        beginTime = System.currentTimeMillis();
        String path = "";
        if (OSUtils.isMac()) {
            path = getFilePath(MAC_OS_PATH + MAC_LINUX_VANITYGEN);
            if (useOpencl) {
                path = getFilePath(MAC_OS_PATH + MAC_LINUX_OCLVANITYGEN);
            }

        } else if (OSUtils.isLinux()) {
            path = getFilePath(LINUX_PATH + MAC_LINUX_VANITYGEN);
            if (useOpencl) {
                path = getFilePath(LINUX_PATH + MAC_LINUX_OCLVANITYGEN);
            }


        } else if (OSUtils.isWindows()) {
            if (SystemUtil.isSystem32()) {
                path = getFilePath(WINDOWS_PATH + WINDOWS_VANITYGEN);
                System.out.println("system 32");
            } else {
                System.out.println("system 64");
                path = getFilePath(WINDOWS_PATH + WINDOWS_VANITYGEN_64);
            }

            if (useOpencl) {
                if (SystemUtil.isSystem32()) {
                    path = getFilePath(WINDOWS_PATH + WINDOWS_OCLVANITYGEN);
                } else {
                    path = getFilePath(WINDOWS_PATH + WINDOWS_OCLVANITYGEN_64);
                }
            }
        }
        if (Utils.isEmpty(path)) {
            if (this.vanitygenListener != null) {
                this.vanitygenListener.error("vanitygen not exist");
            }
            return;

        }
        List<String> params = new ArrayList<String>();
        params.add(path);
        if (igoreCase) {
            params.add("-i");
        }
        if (useOpencl) {
            params.add("-D " + openclConfig);
        }
        if (!useOpencl) {
            if (threadNum > 0 && threadNum <= SystemUtil.getAvailableProcessors()) {
                params.add("-t " + threadNum);
            }
        }
        if (!OSUtils.isWindows() || !SystemUtil.isSystem32()) {
            if (ecKeyType == BitherSetting.ECKeyType.UNCompressed) {
                params.add("-F" + "uncompressed");
            }
        }
        params.add(input);
        String[] array = new String[params.size()];
        array = params.toArray(array);
        runInRuntime(array);

    }

    private static String getFilePath(String str) {
        return System.getProperty("user.dir") + str;

    }

    private void runInRuntime(String[] commands) {
        try {
            process = Runtime.getRuntime().exec(commands);
            StreamWatch outWathch = new StreamWatch(process.getInputStream(), OUT_TYEP.OUT);
            StreamWatch errorWathch = new StreamWatch(process.getErrorStream(), OUT_TYEP.ERROR);
            outWathch.start();
            errorWathch.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static List<OpenCLDevice> getCLDevices() {
        List<OpenCLDevice> openCLDevices = new ArrayList<OpenCLDevice>();
        String command = "";
        if (OSUtils.isMac()) {
            command = getFilePath(MAC_OS_PATH + MAC_LINUX_OCLVANITYGEN);


        } else if (OSUtils.isLinux()) {
            command = getFilePath(LINUX_PATH + MAC_LINUX_OCLVANITYGEN);


        } else if (OSUtils.isWindows()) {
            command = getFilePath(WINDOWS_PATH + WINDOWS_OCLVANITYGEN);
        }
        try {
            String line = null;
            BufferedReader stdout = null;

            //list the files and directorys under C:\
            Process p = Runtime.getRuntime().exec(new String[]{command, "-d 1000", "1LLL"});

            stdout = new BufferedReader(new InputStreamReader(p
                    .getErrorStream()));
            List<String> availableList = new ArrayList<String>();
            while ((line = stdout.readLine()) != null) {
                if (matcherAvailable(line)) {
                    availableList.add(line);
                }
            }

            stdout.close();
            int platform = 0;
            String platformName = "";
            int device = 0;
            String deviceName = "";
            for (String str : availableList) {
                if (str.startsWith("  ")) {
                    String[] devices = str.split(":");
                    device = Integer.valueOf(devices[0].trim());
                    deviceName = devices[1].trim();
                    OpenCLDevice openCLDevice = new OpenCLDevice(platform, device, platformName, deviceName);
                    openCLDevices.add(openCLDevice);

                } else {
                    String[] platforms = str.split(":");
                    platform = Integer.valueOf(platforms[0].trim());
                    platformName = platforms[1].trim();

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return openCLDevices;
    }

    private static boolean matcherAvailable(String str) {
        Pattern p = Pattern.compile(AVAILABLE_OPENCL_FORMAT);
        Matcher matcher = p.matcher(str);
        return matcher.find();


    }

    private void dealErrorWithString(String line) {
        if (Utils.isEmpty(line)) {
            return;
        }
        if (line.contains(DIFFICULTY_FORMAT)) {
            if (vanitygenListener != null) {

                vanitygenListener.onDifficulty(line.replace(DIFFICULTY_FORMAT, "").trim());
            }
            return;
        }

    }

    private void dealOutWithString(String line) {
        if (Utils.isEmpty(line)) {
            return;
        }
        Pattern p = Pattern.compile(PROGRESS_FORMAT);
        Matcher matcher = p.matcher(line);
        if (matcher.find()) {

            if (vanitygenListener != null) {
                vanitygenListener.onProgress(matcher.group(1), Long.valueOf(matcher.group(2)),
                        Double.valueOf(matcher.group(3)), Integer.valueOf(matcher.group(4)), matcher.group(5));
            }

        }

        if (line.contains(ADDRESS_FORMAT)) {
            if (vanitygenListener != null) {
                vanitygenListener.getAddress(line.replace(ADDRESS_FORMAT, "").trim());
            }
            return;
        }
        if (line.contains(PRIVATE_FORMAT)) {
            generatedKey = true;
            if (vanitygenListener != null) {
                vanitygenListener.getPrivateKey(line.replace(PRIVATE_FORMAT, "").trim(), (System.currentTimeMillis() - beginTime));
            }
        }

    }

    public void stop() {
        if (process != null) {
            process.destroy();
        }
    }

    private class StreamWatch extends Thread {
        InputStream is;
        private OUT_TYEP outTyep;

        StreamWatch(InputStream is, OUT_TYEP outTyep) {
            this.is = is;

            this.outTyep = outTyep;
        }


        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (outTyep == OUT_TYEP.OUT) {
                        dealOutWithString(line);
                    } else {
                        dealErrorWithString(line);
                    }
                    LogUtil.printlnOut("line:" + line);

                }
                is.close();
                if (this.outTyep == OUT_TYEP.OUT) {
                    if (!generatedKey) {
                        if (vanitygenListener != null) {
                            vanitygenListener.error(LocaliserUtils.getString("vanity_generated_failed"));
                        }
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

        }

    }
}

