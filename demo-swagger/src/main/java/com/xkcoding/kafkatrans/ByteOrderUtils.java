package com.xkcoding.kafkatrans;

public class ByteOrderUtils {
    public static short ntohs(short value) {
        return (short) (((value & 0xFF00) >> 8) | ((value & 0x00FF) << 8));
    }

    public static int ntohl(int value) {
        return ((value & 0xFF000000) >> 24) |
               ((value & 0x00FF0000) >> 8) |
               ((value & 0x0000FF00) << 8) |
               ((value & 0x000000FF) << 24);
    }

    public static long ntohll(long value) {
        return ((value & 0xFF00000000000000L) >> 56) |
               ((value & 0x00FF000000000000L) >> 40) |
               ((value & 0x0000FF0000000000L) >> 24) |
               ((value & 0x000000FF00000000L) >> 8) |
               ((value & 0x00000000FF000000L) << 8) |
               ((value & 0x0000000000FF0000L) << 24) |
               ((value & 0x000000000000FF00L) << 40) |
               ((value & 0x00000000000000FFL) << 56);
    }

    public static short htons(short value) {
        return ntohs(value);
    }

    public static int htonl(int value) {
        return ntohl(value);
    }

    public static long htonll(long value) {
        return ntohll(value);
    }
} 