package net.bither.utils;

import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.utils.Utils;
import net.bither.model.OpenCLDevice;
import net.bither.platform.builder.OSUtils;

import java.io.*;
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

        public void getPrivateKey(String privateKey);

        public void onDifficulty(String difficile);

        public void error(String error);


    }

    private static final String ADDRESS_FORMAT = "Address: ";
    private static final String PRIVATE_FORMAT = "Privkey: ";
    private static final String PROGRESS_FORMAT = "\\[(.*)\\]\\[total (\\d+)\\]\\[Prob (\\d+\\.\\d+)%\\]\\[(\\d+)% in (.*)\\]";
    private static final String DIFFICULTY_FORMAT = "Difficulty: ";

    private final String VANITYGEN_MAC = "./vanitygen";
    private final String OCLVANITYGEN_MAC = "/oclvanitygen";
    private final String MAC_OS_PATH = "/vanitygen/mac/";

    private String input;
    private String openclConfig;
    private boolean useOpencl;
    private boolean igoreCase;
    private Process process = null;
    private IVanitygenListener vanitygenListener;

    public BitherVanitygen(String input, boolean useOpencl, boolean igoreCase, String openclConfig, IVanitygenListener vanitygenListener) {
        this.input = input;
        this.useOpencl = useOpencl;
        this.igoreCase = igoreCase;
        this.openclConfig = openclConfig;
        this.vanitygenListener = vanitygenListener;
    }

    public void generateAddress() {
        String command = "";


        if (OSUtils.isMac()) {
            command = BitherVanitygen.class.getResource(MAC_OS_PATH + VANITYGEN_MAC).getFile();


        } else if (OSUtils.isLinux()) {
            BitherVanitygen.class.getResource(MAC_OS_PATH + OCLVANITYGEN_MAC).getFile();

        } else if (OSUtils.isWindows()) {

        }
        if (Utils.isEmpty(command)) {
            if (this.vanitygenListener != null) {
                this.vanitygenListener.error("vanitygen not exist");
            }
            return;

        }
        if (useOpencl) {
            String params = "";
            if (igoreCase) {
                params = "-i";
            }
            command = Utils.format(".%s -D %s %s %s", command, params, openclConfig, input);
        } else {
            String params = "";
            if (igoreCase) {
                params = "-i";
            }
            command = Utils.format("%s %s %s", command, params, input);
        }
        runInRuntime(command);

    }

    private void runInRuntime(String command) {
        try {
            process = Runtime.getRuntime().exec(command);
            StreamWatch outWathch = new StreamWatch(process.getInputStream(), OUT_TYEP.OUT);
            StreamWatch errorWathch = new StreamWatch(process.getErrorStream(), OUT_TYEP.ERROR);
            outWathch.start();
            errorWathch.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static List<OpenCLDevice> getCLDevices() {
        return new ArrayList<OpenCLDevice>();
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
            System.out.println("");
            System.out.println(matcher.group(0) + "," + matcher.group(1)
                    + "," + matcher.group(2) + "," + matcher.group(3) + ","
                    + matcher.group(4) + "," + matcher.group(5));
            if (vanitygenListener!=null){
                vanitygenListener.onProgress(matcher.group(1),Long.valueOf(matcher.group(2)),
                        Double.valueOf(matcher.group(3)),Integer.valueOf(matcher.group(4)),matcher.group(5));
            }

        }

        if (line.contains(ADDRESS_FORMAT)) {
            if (vanitygenListener != null) {
                vanitygenListener.getAddress(line.replace(ADDRESS_FORMAT, "").trim());
            }
            return;
        }
        if (line.contains(PRIVATE_FORMAT)) {
            if (vanitygenListener != null) {
                vanitygenListener.getPrivateKey(line.replace(PRIVATE_FORMAT, "").trim());
            }
        }

    }

    public void stop() {
        if (process != null && process.isAlive()) {
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
                    if (BitherjSettings.LOG_DEBUG) {
                        System.out.println(">>>" + line);
                    }
                }
                is.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

        }

    }
}

