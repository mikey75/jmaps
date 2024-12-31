package net.wirelabs.jmaps.map.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpUtils {

    public static String queryParam(boolean first, String param, String value) {
        return "?" + encode(param) + "=" + encode(value);
    }

    public static String queryParam(String param, String value) {
        return "&" + encode(param) + "=" +encode(value);
    }

    private static String encode(String str) {
        // as per http docs
        // query params should escape/encode following chars
        // " !"#$&'(),/:;<=>?@[]\^`{|}~"
        char[] toEncode = new char[] {' ','!','"','#','$','&','\'','(',')','/',':',';','<','=','>','?','@','[',']','\\','^','`','{','|','}','~'};
        for (char c: toEncode) {
            str = str.replace(String.valueOf(c), "%" + charToHex(c));
        }
        return str;

    }

    private static String charToHex(char c) {

        byte hi = (byte) (c >>> 8);
        byte lo = (byte) (c & 0xff);
        if (hi == 0) {
            return byteToHex(lo);
        } else {
            return byteToHex(hi) + byteToHex(lo);
        }
    }

    private static String byteToHex(byte b) {

        char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
        return new String(array);
    }
}
