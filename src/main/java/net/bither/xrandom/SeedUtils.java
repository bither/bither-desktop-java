package net.bither.xrandom;

public class SeedUtils {

    public static byte seedInt8(long x) {
        return (byte) (x & 255);

    }

    public static byte[] seedInt16(long x) {
        byte[] bytes = new byte[2];
        bytes[0] = seedInt8(x);
        bytes[1] = seedInt8(x >> 8);
        return bytes;
    }

    public static byte[] seedInt32(long x) {
        byte[] bytes = new byte[4];
        bytes[0] = seedInt8(x);
        bytes[1] = seedInt8(x >> 8);
        bytes[2] = seedInt8(x >> 16);
        bytes[3] = seedInt8(x >> 24);

        return bytes;
    }

    public static byte[] seedTime() {
        return seedInt32(System.currentTimeMillis());
    }
}
