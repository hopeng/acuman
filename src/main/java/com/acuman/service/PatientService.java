package com.acuman.service;

import com.acuman.domain.ActionResult;
import com.couchbase.client.java.document.json.JsonObject;

import java.util.List;

public interface PatientService {

    JsonObject newPatient(String json);

    JsonObject updatePatient(String id, String json);

    JsonObject getPatient(String id);

    ActionResult deletePatient(String id);

    List<JsonObject> getPatients();
}
