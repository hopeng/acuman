package com.acuman.service.couchbase;

import com.acuman.CouchBaseQuery;
import com.acuman.service.ConsultationService;
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

import static com.acuman.service.couchbase.CouchbasePatientService.DOCTOR;
import static com.couchbase.client.java.query.Select.select;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class CouchBaseConsultationService implements ConsultationService {
    private static final Logger log = LogManager.getLogger(CouchBaseConsultationService.class);

    private static final String CONSULTATION_PREFIX =  "-CONSULT-";
    private static final String CONSULTATION_ID_SEQ = "consultIdSeq";

    private Bucket bucket;
    private CouchBaseQuery couchBaseQuery;

    public CouchBaseConsultationService() {
        bucket = CouchBaseClient.getInstance().getBucket();
        couchBaseQuery = new CouchBaseQuery(bucket);
    }

    @Override
    public JsonObject newConsultation(String json, String patientId) {
        String id = generateConsultId(patientId);

        JsonObject consultation = JsonObject.fromJson(json);
        consultation.put("doctor", DOCTOR);
        consultation.put("patientId", patientId);
        consultation.put("consultId", id);
        consultation.put("type", "CONSULTATION");
        consultation.put("createdDate", LocalDateTime.now().toString());
        consultation.put("createdBy", DOCTOR);
        DateUtils.convertISODateToLocalDateString(consultation, "visitedOn");
        JsonDocument result = bucket.insert(JsonDocument.create(id, consultation));

        log.info("inserted consultation: " + result.content());

        return result.content();
    }

    private String generateConsultId(String patientId) {
        long nextSquence = bucket.counter(CONSULTATION_ID_SEQ, 1, 1).content();
        String id = patientId + CONSULTATION_PREFIX + nextSquence;

        return id;
    }

    @Override
    public JsonObject updateConsultation(String id, String json) {
        JsonObject consultation = JsonObject.fromJson(json);
        Assert.isTrue(isNotEmpty(consultation.getString("doctor")), "doctor cannot be updated to empty");
        Assert.isTrue(isNotEmpty(consultation.getString("type")), "type cannot be updated to empty");
        String consultId = consultation.getString("consultId");
        Assert.isTrue(id.equals(consultId), "provided id does not match json consultId");
        Assert.notNull(getConsultation(id));

        consultation.put("lastUpdatedDate", LocalDateTime.now().toString());
        consultation.put("lastUpdatedBy", DOCTOR);
        DateUtils.convertISODateToLocalDateString(consultation, "visitedOn");
        JsonDocument result = bucket.upsert(JsonDocument.create(id, consultation));

        log.info("updated consultation: " + result.content());

        return result.content();
    }

    @Override
    public JsonObject getConsultation(String id) {
        return bucket.get(id, JsonDocument.class).content();
    }

    @Override
    public JsonObject deleteConsultation(String id) {
        JsonObject result = bucket.remove(id).content();

        log.info("deleted consultation: " + result);

        return result;
    }

    @Override
    public List<JsonObject> getConsultations(String patientId) {
        List<JsonObject> result;
        String condition = String.format("type='CONSULTATION' and patientId='%s' order by createdDate desc", patientId);
        Statement statement = select("*").from(bucket.name()).where(condition).limit(1000); // todo paging
        result = couchBaseQuery.query(statement);

        return result;
    }

}
