package com.acuman.api;

import com.acuman.service.PatientService;
import com.acuman.service.couchbase.CouchbasePatientService;
import com.acuman.util.AuthUtil;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static com.acuman.ApiConstants.API_PATIENTS;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

public class PatientsApi {
    private static final Logger log = LogManager.getLogger(PatientsApi.class);

    public static void configure() {
        PatientService patientService = new CouchbasePatientService();
        post(API_PATIENTS, (request, response) -> {
            String patientJson = request.body();
            log.info("creating new patient {}", patientJson);
            JsonObject result = patientService.newPatient(patientJson);

            response.status(201);
            return result;
        });

        get(API_PATIENTS + "/:id", (request, response) -> {
            String id = request.params(":id");
            JsonObject result = patientService.getPatient(id);

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
            JsonObject result = patientService.getPatient(id);

            if (result == null) {
                response.status(404);
                return "Cannot find patient by ID " + id;

            } else {
                return patientService.updatePatient(id, json);
            }
        });

        delete(API_PATIENTS + "/:id", (request, response) -> {
            String id = request.params(":id");

            try {
                patientService.deletePatient(id);
                response.status(204);

            } catch (DocumentDoesNotExistException e) {
                response.status(404);
            }
            return "";
        });

        get(API_PATIENTS, (request, response) -> {
            List<JsonObject> result = patientService.getPatients(AuthUtil.currentUser());
            return result;
        });
    }

}
