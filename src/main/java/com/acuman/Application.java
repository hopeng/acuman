package com.acuman;

import com.acuman.api.ConsultationsApi;
import com.acuman.api.PatientsApi;
import com.acuman.api.TcmWordLookupApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static spark.Spark.externalStaticFileLocation;


public class Application {
    private static final Logger log = LogManager.getLogger(Application.class);


    public static void main(String[] args) {
        externalStaticFileLocation("static/");

        PatientsApi.configure();
        ConsultationsApi.configure();
        TcmWordLookupApi.configure();
    }
}
