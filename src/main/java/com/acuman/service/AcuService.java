package com.acuman.service;

import com.couchbase.client.java.document.json.JsonObject;

import java.util.List;

/**
 * Created by hopeng on 23/04/2016.
 */
public interface AcuService {

    JsonObject newPatient(String json);

    JsonObject updatePatient(String json);

    JsonObject getPatient(String id);

    JsonObject deletePatient(String id);

    List<JsonObject> getPatients(String doctor);
}
