package com.acuman.service;

import com.couchbase.client.deps.com.fasterxml.jackson.core.JsonProcessingException;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.ObjectMapper;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.transcoder.JacksonTransformers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.utils.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.x;

/**
 * Create index before query: CREATE PRIMARY INDEX on acuman using VIEW
 */
public class AcuService {
    private static final Logger log = LogManager.getLogger(AcuService.class);

    public static final String DOCTOR = "HFANG";   // todo should come from session user
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
        log.info("closing couchbase client");
        cluster.disconnect();
    }

    public JsonObject newPatient(String json) {
        long nextSquence = bucket.counter(PATIENT_ID_SEQ, 1, 1).content();
        String id = PATIENT_PREFIX + nextSquence;

        JsonObject patient = JsonObject.fromJson(json);
        patient.put("doctor", DOCTOR);
        patient.put("patientId", id);
        patient.put("type", "PATIENT");
        JsonDocument result = bucket.insert(JsonDocument.create(id, patient));

        log.info("inserted patient: " + result.content());

        return result.content();
    }

    public JsonObject updatePatient(String json) {
        JsonObject patient = JsonObject.fromJson(json);
        Assert.notNull(patient.getString("doctor"));
        Assert.notNull(patient.getString("type"));
        String id = patient.getString("patientId");
        Assert.notNull(id);
        Assert.notNull(getPatient(id));

        JsonDocument result = bucket.upsert(JsonDocument.create(id, patient));

        log.info("updated patient: " + result.content());

        return result.content();
    }

    public JsonObject getPatient(String id) {
        return bucket.get(id, JsonDocument.class).content();
    }

    public JsonObject deletePatient(String id) {
        JsonObject result = bucket.remove(id).content();

        log.info("deleted patient: " + result);

        return result;
    }

    public List<JsonObject> getPatients(String doctor) {
        List<JsonObject> result;
        String condition = String.format("type='PATIENT' and doctor='%s'", doctor);
        N1qlQueryResult query = bucket.query(select("*").from(BUCKET_NAME).where(condition).limit(1000)); // todo paging

        result = query.allRows().stream()
                .map(row -> row.value().getObject(BUCKET_NAME))
                .collect(Collectors.toList());

        return result;
    }
}
