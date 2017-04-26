package com.acuman.service.couchbase;

import com.acuman.CbDocType;
import com.acuman.CouchBaseQuery;
import com.acuman.domain.Auditable;
import com.acuman.service.PatientService;
import com.acuman.util.AuthUtil;
import com.acuman.util.DateUtils;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.utils.Assert;

import java.util.List;

import static com.couchbase.client.java.query.Select.select;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * @deprecated use s3
 * Create index before query: CREATE PRIMARY INDEX on acuman using VIEW
 */
public class CouchbasePatientService implements PatientService {
    private static final Logger log = LogManager.getLogger(CouchbasePatientService.class);

    private static final String PATIENT_ID_SEQ = "patientIdSeq";

    private Bucket bucket = CouchBaseClient.getInstance().getBucket();
    private CouchBaseQuery couchBaseQuery;

    public CouchbasePatientService() {
        bucket = CouchBaseClient.getInstance().getBucket();
        couchBaseQuery = new CouchBaseQuery(bucket);
    }

    @Override
    public JsonObject newPatient(String json) {
        String id = generateId();

        JsonObject patient = JsonObject.fromJson(json);
        patient.put("doctor", AuthUtil.currentUser());
        patient.put("patientId", id);
        patient.put("type", "PATIENT");
        Auditable.preInsert(patient);
        DateUtils.convertISODateToLocalDateString(patient, "dob");
        JsonDocument result = bucket.insert(JsonDocument.create(id, patient));

        log.info("inserted patient: " + result.content());

        return result.content();
    }

    private String generateId() {
        long nextSequence = bucket.counter(PATIENT_ID_SEQ, 1, 1).content();
        String id = CbDocType.Patient + "-" + String.format("%07d", nextSequence);

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

        Auditable.preUpdate(patient);
        DateUtils.convertISODateToLocalDateString(patient, "dob");
        JsonDocument result = bucket.upsert(JsonDocument.create(id, patient));

        log.info("updated patient: " + result.content());

        return result.content();
    }

    @Override
    public JsonObject getPatient(String id) {
        JsonDocument jsonDocument = bucket.get(id, JsonDocument.class);
        return jsonDocument == null ? null : jsonDocument.content();
    }

    @Override
    public JsonObject deletePatient(String id) {
        JsonObject result = bucket.remove(id).content();

        log.info("deleted patient: " + result);

        return result;
    }

    @Override
    public List<JsonObject> getPatients() {
        List<JsonObject> result;
        String condition = String.format("type='PATIENT' and doctor='%s' order by createdDate desc", AuthUtil.currentUser());
        Statement statement = select("*").from(bucket.name()).where(condition).limit(1000); // todo paging

        result = couchBaseQuery.query(statement);

        return result;
    }
}
