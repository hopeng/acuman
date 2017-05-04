package com.acuman.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.acuman.ApiConstants.API_HEALTHCHECK;
import static spark.Spark.get;

public class HealthCheckApi {
    private static final Logger log = LogManager.getLogger(HealthCheckApi.class);

    public static void configure() {

        get(API_HEALTHCHECK, (request, response) -> {
            return "OK";
        });
    }

}
