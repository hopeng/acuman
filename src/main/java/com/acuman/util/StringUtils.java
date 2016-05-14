package com.acuman.util;

/**
 * Created by hopeng on 15/05/2016.
 */
public class StringUtils {
    private static final String NON_BREAKING_SPACE = "\u00A0";


    public static String trimNonBreaking(String str) {
        return str == null ? null : str.trim().replace(NON_BREAKING_SPACE, "");
    }
}
