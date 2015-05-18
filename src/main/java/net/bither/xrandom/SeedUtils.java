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
