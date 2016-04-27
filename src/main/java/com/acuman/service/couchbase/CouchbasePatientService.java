package com.acuman.service.couchbase;

import com.acuman.Utils;
import com.acuman.service.PatientService;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQueryResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.utils.Assert;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.acuman.service.couchbase.CouchBaseClient.BUCKET_NAME;
import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.x;
import static java.time.LocalDate.now;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
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

    @Override
    public JsonObject newPatient(String json) {
        String id = generateId(DOCTOR);

        JsonObject patient = JsonObject.fromJson(json);
        patient.put("doctor", DOCTOR);
        patient.put("patientId", id);
        patient.put("type", "PATIENT");
        patient.put("createdDate", LocalDateTime.now().toString());
        patient.put("createdBy", DOCTOR);
        Utils.convertISODateToLocalDateString(patient, "dob");
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
        Utils.convertISODateToLocalDateString(patient, "dob");
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
        N1qlQueryResult query = bucket.query(select("*").from(BUCKET_NAME).where(condition).limit(1000)); // todo paging

        result = query.allRows().stream()
                .map(row -> row.value().getObject(BUCKET_NAME))
                .collect(Collectors.toList());

        return result;
    }
}
