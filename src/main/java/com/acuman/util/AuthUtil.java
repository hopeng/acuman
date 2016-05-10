package com.acuman.util;

/**
 * Created by hopeng on 11/05/2016.
 */
public class AuthUtil {
    private static final String DOCTOR = "fionafamilytcm";   // todo should come from session user


    public static String currentUser() {
        return DOCTOR;
    }
}
