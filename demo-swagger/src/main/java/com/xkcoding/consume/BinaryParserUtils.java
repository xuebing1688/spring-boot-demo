package com.xkcoding.consume;

public class BinaryParserUtils {
    
    public static short swapShort(short value) {
        return (short) ((value << 8) | ((value >> 8) & 0xFF));
    }
    
    public static int swapInt(int value) {
        return ((value & 0xFF) << 24) |
               ((value & 0xFF00) << 8) |
               ((value & 0xFF0000) >> 8) |
               ((value >>> 24) & 0xFF);
    }
    
    public static long swapLong(long value) {
        return ((value & 0xFF) << 56) |
               ((value & 0xFF00) << 40) |
               ((value & 0xFF0000) << 24) |
               ((value & 0xFF000000) << 8) |
               ((value & 0xFF00000000L) >> 8) |
               ((value & 0xFF0000000000L) >> 24) |
               ((value & 0xFF000000000000L) >> 40) |
               ((value & 0xFF00000000000000L) >> 56);
    }
}
