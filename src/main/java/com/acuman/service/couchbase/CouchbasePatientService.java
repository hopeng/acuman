package com.acuman.service.couchbase;

import com.acuman.CouchBaseQuery;
import com.acuman.service.PatientService;
import com.acuman.util.DateUtils;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.utils.Assert;

import java.time.LocalDateTime;
import java.util.List;

import static com.couchbase.client.java.query.Select.select;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Create index before query: CREATE PRIMARY INDEX on acuman using VIEW
 */
public class CouchbasePatientService implements PatientService {
    private static final Logger log = LogManager.getLogger(CouchbasePatientService.class);

    public static final String DOCTOR = "HFANG";   // todo should come from session user

    private static final String PATIENT_PREFIX = "-PATIENT-";
    private static final String PATIENT_ID_SEQ = "patientIdSeq";

    private Bucket bucket = CouchBaseClient.getInstance().getBucket();
    private CouchBaseQuery couchBaseQuery;

    public CouchbasePatientService() {
        bucket = CouchBaseClient.getInstance().getBucket();
        couchBaseQuery = new CouchBaseQuery(bucket);
    }

    @Override
    public JsonObject newPatient(String json) {
        String id = generateId(DOCTOR);

        JsonObject patient = JsonObject.fromJson(json);
        patient.put("doctor", DOCTOR);
        patient.put("patientId", id);
        patient.put("type", "PATIENT");
        patient.put("createdDate", LocalDateTime.now().toString());
        patient.put("createdBy", DOCTOR);
        DateUtils.convertISODateToLocalDateString(patient, "dob");
        JsonDocument result = bucket.insert(JsonDocument.create(id, patient));

        log.info("inserted patient: " + result.content());

        return result.content();
    }

    private String generateId(String doctor) {
        long nextSquence = bucket.counter(PATIENT_ID_SEQ, 1, 1).content();
        String id = doctor + PATIENT_PREFIX + nextSquence;

        return id;
    }

    @Override
    public JsonObject updatePatient(String id, String json) {
        JsonObject patient = JsonObject.fromJson(json);
        Assert.isTrue(isNotEmpty(patient.getString("doctor")), "doctor cannot be updated to empty");
        Assert.isTrue(isNotEmpty(patient.getString("type")), "type cannot be updated to empty");
        String patientId = patient.getString("patientId");
        Assert.isTrue(id.equals(patientId), "provided id does not match json patientId");
        Assert.notNull(getPatient(id));

        patient.put("lastUpdatedDate", LocalDateTime.now().toString());
        patient.put("lastUpdatedBy", DOCTOR);
        DateUtils.convertISODateToLocalDateString(patient, "dob");
        JsonDocument result = bucket.upsert(JsonDocument.create(id, patient));

        log.info("updated patient: " + result.content());

        return result.content();
    }

    @Override
    public JsonObject getPatient(String id) {
        return bucket.get(id, JsonDocument.class).content();
    }

    @Override
    public JsonObject deletePatient(String id) {
        JsonObject result = bucket.remove(id).content();

        log.info("deleted patient: " + result);

        return result;
    }

    @Override
    public List<JsonObject> getPatients(String doctor) {
        List<JsonObject> result;
        String condition = String.format("type='PATIENT' and doctor='%s' order by createdDate desc", doctor);
        Statement statement = select("*").from(bucket.name()).where(condition).limit(1000); // todo paging

        result = couchBaseQuery.query(statement);

        return result;
    }
}
