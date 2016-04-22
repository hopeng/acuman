package com.acuman;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.print.Book;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static spark.Spark.*;


public class Application {
    private static final Logger log = LogManager.getLogger(Application.class);

    private static final String API_VERSION = "/v1";
    private static final String API_PATIENTS = API_VERSION + "/patients";

    public static void main(String[] args) {
        AcuService acuService = new AcuService();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                acuService.destroy();
            }
        });

        externalStaticFileLocation("static/");
        post(API_PATIENTS, (request, response) -> {
            String patientJson = request.body();
            log.info("creating new patient {}", patientJson);
            JsonObject result = acuService.newPatient(patientJson);

            response.status(201);
            return result;
        });

        // Gets the book resource for the provided id
        get(API_PATIENTS + "/:id", (request, response) -> {
            String id = request.params(":id");
            JsonObject result = acuService.getPatient(id);

            if (result == null) {
                response.status(404);
                return "Cannot find patient by ID " + id;

            } else {
                return result;
            }
        });

        put(API_PATIENTS + "/:id", (request, response) -> {
            String id = request.params(":id");
            String json = request.body();
            JsonObject result = acuService.getPatient(id);

            if (result == null) {
                response.status(404);
                return "Cannot find patient by ID " + id;

            } else {
                return acuService.updatePatient(json);
            }
        });

        delete(API_PATIENTS + "/:id", (request, response) -> {
            String id = request.params(":id");

            JsonObject result;
            try {
                return acuService.deletePatient(id);
            } catch (DocumentDoesNotExistException e) {
                response.status(404);
                return "Cannot find patient by ID " + id;
            }
        });

        get(API_PATIENTS, (request, response) -> {
            String doctor = request.queryParams("doctor");
            List<JsonObject> result = acuService.getPatients(doctor);
            return result;
        });
    }
}
