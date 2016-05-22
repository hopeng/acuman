package com.acuman;

import com.acuman.api.ConsultationsApi;
import com.acuman.api.FileDownloadApi;
import com.acuman.api.Oauth2Api;
import com.acuman.api.PatientsApi;
import com.acuman.api.TcmDictLookupApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static spark.Spark.before;
import static spark.Spark.externalStaticFileLocation;
import static spark.Spark.halt;
import static spark.Spark.port;


public class Application {
    private static final Logger log = LogManager.getLogger(Application.class);


    public static void main(String[] args) {
// todo ssl  http://stackoverflow.com/a/36843005/843678
        if (Boolean.valueOf(System.getProperty("dev"))) {
            port(4568);
        } else if (Boolean.valueOf(System.getProperty("prod"))) {
            port(80);
        }
        externalStaticFileLocation("static/");

        PatientsApi.configure();
        ConsultationsApi.configure();
        TcmDictLookupApi.configure();
        Oauth2Api.configure();
        FileDownloadApi.configure();

        before( (request, response) -> {
            String uri = request.uri();
            if (uri.startsWith("/img/") || uri.startsWith("/oauth2callback")) {
                return;
            }

            boolean authorized = true; // todo
            if (!authorized) {
                halt(401, "You are not authorized!");
            }
        });
    }
}
