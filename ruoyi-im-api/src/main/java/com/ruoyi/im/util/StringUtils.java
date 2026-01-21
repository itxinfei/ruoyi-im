package com.ruoyi.im.util;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class StringUtils {
    public static final String EMPTY = "";

    public static int length(String str) {
        return str == null ? 0 : str.length();
    }

    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence str) {
        return !isEmpty(str);
    }

    public static boolean isBlank(CharSequence str) {
        int length;

        if (str != null && (length = str.length()) != 0) {
            for (int i = 0; i < length; ++i) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    public static boolean isNotBlank(CharSequence str) {
        return !isBlank(str);
    }

    public static boolean isNull(Object obj) {
        return obj == null || obj.equals("") || obj.equals("null");
    }

    public static boolean isNotNull(Object obj) {
        return !isNull(obj);
    }

    public static boolean equals(CharSequence cs1, CharSequence cs2) {
        return cs1 == cs2 || (cs1 != null && cs1.equals(cs2));
    }

    public static boolean equalsIgnoreCase(CharSequence str1, CharSequence str2) {
        return str1 == str2 || (str1 != null && str2 != null && str1.toString().equalsIgnoreCase(str2.toString()));
    }

    public static boolean inStringIgnoreCase(String str, String... strs) {
        if (str != null && strs != null) {
            for (String s : strs) {
                if (str.equalsIgnoreCase(trim(s))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }
}
