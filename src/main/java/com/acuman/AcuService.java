package com.acuman;

import com.couchbase.client.deps.com.fasterxml.jackson.databind.ObjectMapper;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.JsonStringDocument;
import com.couchbase.client.java.document.RawJsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.utils.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.couchbase.client.java.query.Select.select;

public class AcuService {
    private static final Logger log = LogManager.getLogger(AcuService.class);

    private static final String DOCTOR = "HFANG";   // todo should come from session user
    private static final String PATIENT_PREFIX = DOCTOR + "-PATIENT-";
    private static final String PATIENT_ID_SEQ = "patientIdSeq";
    private static final String BUCKET_NAME = "acuman";

    private Cluster cluster;
    private Bucket bucket;

    public AcuService() {
        cluster = CouchbaseCluster.create();
        bucket = cluster.openBucket(BUCKET_NAME);
    }

    public void destroy() {
        cluster.disconnect();

    }

    public JsonObject newPatient(String json) {
        long nextSquence = bucket.counter(PATIENT_ID_SEQ, 1, 1).content();
        String id = PATIENT_PREFIX + nextSquence;

        JsonObject patient = JsonObject.fromJson(json);
        patient.put("doctor", DOCTOR);
        patient.put("id", id);
        patient.put("type", "PATIENT");
        JsonDocument result = bucket.insert(JsonDocument.create(id, patient));

        return result.content();
    }

    public String getPatient(String id) {
        return bucket.get(id, RawJsonDocument.class).content();
    }

    public List<JsonObject> getPatients(String doctor) {
        List<JsonObject> result = new ArrayList<>();
        N1qlQueryResult query = bucket.query(select("*").from(BUCKET_NAME).limit(1000)); // todo paging
        for (N1qlQueryRow row : query.allRows()) {
            result.add(row.value());
        }

        return result;
    }

    public static void main(String args[]) {
        AcuService acuService = new AcuService();
        JsonObject p = acuService.newPatient("{\"initialVisit\":\"2016-04-21T11:21:10.430Z\",\"dob\":null}");
        String retrievedP = acuService.getPatient(p.getString("id"));
        log.info(p);
        log.info(retrievedP);
        Assert.isTrue(p.toString().equals(retrievedP.toString()), "not the same?!");
        List<JsonObject> result = acuService.getPatients(DOCTOR);

        log.info("search: " + result);
        acuService.destroy();
    }
}
