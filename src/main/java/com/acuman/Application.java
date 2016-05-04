package com.acuman;

import com.acuman.api.ConsultationsApi;
import com.acuman.api.PatientsApi;
import com.acuman.api.TcmDictLookupApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static spark.Spark.externalStaticFileLocation;
import static spark.Spark.ipAddress;
import static spark.Spark.port;


public class Application {
    private static final Logger log = LogManager.getLogger(Application.class);


    public static void main(String[] args) {
        externalStaticFileLocation("static/");

        port(80);
        ipAddress("ec2-52-34-117-240.us-west-2.compute.amazonaws.com");
//        ipAddress("52.34.117.240");

        PatientsApi.configure();
        ConsultationsApi.configure();
        TcmDictLookupApi.configure();
    }
}
