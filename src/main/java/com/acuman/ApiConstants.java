package com.acuman;

public interface ApiConstants {

    String API_VERSION = "/v1";
    String API_CONSULTATIONS = API_VERSION + "/consults";
    String API_PATIENTS = API_VERSION + "/patients";
    String API_HEALTHCHECK = API_VERSION + "/healthcheck";
    /**
     * @deprecated
     */
    String API_TCM_DICT = API_VERSION + "/tcmdict";
    String API_TCM_ZhEn_WORD = API_VERSION + "/tcm-zh-en-words";
    String API_DOWNLOAD_PATIENTS = API_VERSION + "/download-patients";
}
