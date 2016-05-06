package com.acuman.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static spark.Spark.get;

/**
 * Created by hopeng on 6/05/2016.
 */
public class Oauth2Api {
    private static final Logger log = LogManager.getLogger(Oauth2Api.class);


    public static void configure() {
        get("/oauth2callback", (request, response) -> {
            log.info("received oauth2 request ", request.body());
            return "";
        });
    }
}
