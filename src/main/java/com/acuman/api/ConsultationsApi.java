package com.acuman.api;

import com.acuman.service.ConsultationService;
import com.acuman.service.s3.S3ConsultationService;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.utils.Assert;

import java.util.List;

import static com.acuman.ApiConstants.API_CONSULTATIONS;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

public class ConsultationsApi {
    private static final Logger log = LogManager.getLogger(ConsultationsApi.class);

    public static void configure() {
        ConsultationService service = new S3ConsultationService();

        post(API_CONSULTATIONS, (request, response) -> {
            String patientId = request.queryParams("patientId");
            Assert.isTrue(StringUtils.isNotEmpty(patientId), "patientId query param is required");

            String json = request.body();
            log.info("creating new consultation {}", json);
            JsonObject result = service.newConsultation(json, patientId);

            response.status(201);
            return result;
        });

        get(API_CONSULTATIONS + "/:id", (request, response) -> {
            String id = request.params(":id");
            JsonObject result = service.getConsultation(id);

            if (result == null) {
                response.status(404);
                return "Cannot find consultation by ID " + id;

            } else {
                return result;
            }
        });

        put(API_CONSULTATIONS + "/:id", (request, response) -> {
            String id = request.params(":id");
            String json = request.body();
            JsonObject result = service.getConsultation(id);

            if (result == null) {
                response.status(404);
                return "Cannot find consultation by ID " + id;

            } else {
                return service.updateConsultation(id, json);
            }
        });

        delete(API_CONSULTATIONS + "/:id", (request, response) -> {
            String id = request.params(":id");

            try {
                service.deleteConsultation(id);
                response.status(204);

            } catch (DocumentDoesNotExistException e) {
                response.status(404);
            }
            return "";
        });

        get(API_CONSULTATIONS, (request, response) -> {
            String patientId = request.queryParams("patientId");
            Assert.isTrue(StringUtils.isNotEmpty(patientId), "patientId query param is required");

            List<JsonObject> result = service.getConsultations(patientId);
            return result;
        });
    }
}
