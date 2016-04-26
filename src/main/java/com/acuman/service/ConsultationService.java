package com.acuman.service;

import com.couchbase.client.java.document.json.JsonObject;

import java.util.List;

public interface ConsultationService {


    JsonObject newConsultation(String json, String patientId);

    JsonObject updateConsultation(String id, String json);

    JsonObject getConsultation(String id);

    JsonObject deleteConsultation(String id);

    List<JsonObject> getConsultations(String patientId);

}
