package com.privacy2345.droidprivacy.util;

import android.text.TextUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 字符串工具类
 * 提供字符串转换、分割、连接等常用操作
 * 主要功能：
 * 1. 对象转字符串 - 支持各种数据类型和数组的字符串转换
 * 2. 数组连接 - 将数组元素连接成字符串
 * 3. 字符串分割 - 将字符串分割为Set集合
 *
 * @author : zhongjy@2345.com
 */
public class StringHelper {
    public static String convertToString(Object obj) {
        if (obj == null) {
            return "null";
        }

        if (obj.getClass().isArray()) {
            if (obj instanceof int[]) {
                return Arrays.toString((int[]) obj);
            } else if (obj instanceof long[]) {
                return Arrays.toString((long[]) obj);
            } else if (obj instanceof double[]) {
                return Arrays.toString((double[]) obj);
            } else if (obj instanceof float[]) {
                return Arrays.toString((float[]) obj);
            } else if (obj instanceof boolean[]) {
                return Arrays.toString((boolean[]) obj);
            } else if (obj instanceof char[]) {
                return Arrays.toString((char[]) obj);
            } else if (obj instanceof byte[]) {
                return new String((byte[]) obj);
            } else if (obj instanceof short[]) {
                return Arrays.toString((short[]) obj);
            } else {
                return Arrays.deepToString((Object[]) obj);
            }
        }

        return obj.toString();
    }

    public static String join(Object[] array, String separator) {
        if (array == null) {
            return null;
        }
        if (separator == null) {
            separator = "";
        }
        int arraySize = array.length;
        int bufSize = (arraySize == 0) ? 0 : arraySize * ((array[0] == null ? 16 : array[0].toString().length()) + separator.length());
        StringBuilder buf = new StringBuilder(bufSize);

        for (int i = 0; i < arraySize; i++) {
            if (i > 0) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    public static Set<String> splitToSet(String input, String delimiter, Set<String> defCollection) {
        if (TextUtils.isEmpty(input) || TextUtils.isEmpty(delimiter)) {
            return defCollection;
        }
        Set<String> set = new HashSet<>();
        String[] parts = input.split(delimiter);

        Collections.addAll(set, parts);
        return set;
    }
}
