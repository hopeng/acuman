package com.acuman.service.s3;

import com.acuman.CbDocType;
import com.acuman.domain.Auditable;
import com.acuman.service.ConsultationService;
import com.acuman.util.AuthUtil;
import com.acuman.util.DateUtils;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.couchbase.client.java.document.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.utils.Assert;

import java.util.ArrayList;
import java.util.List;

import static com.acuman.service.s3.S3CrudRepo.currentUserS3Crud;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Created by hopeng on 24/4/17.
 */
public class S3ConsultationService implements ConsultationService {
    private static final Logger log = LogManager.getLogger(S3ConsultationService.class);

    private static final String CONSULTS_PATH = "patients/%s/consults/%s";
    private static final String CONSULTATION_ID_SEQ = "consultIdSeq";


    private static final String CONSULTATION_PREFIX =  "-CONSULT-";


    public S3ConsultationService() {
    }

    @Override
    public JsonObject newConsultation(String json, String patientId) {
        String id = generateConsultId(patientId);

        JsonObject consultation = JsonObject.fromJson(json);
        consultation.put("doctor", AuthUtil.currentUser());
        consultation.put("patientId", patientId);
        consultation.put("consultId", id);
        consultation.put("type", CbDocType.Consult);
        Auditable.preInsert(consultation);
        DateUtils.convertISODateTimeToDateString(consultation, "visitedOn");

        String fullPath = format(CONSULTS_PATH, patientId, id);
        S3CrudRepo.currentUserS3Crud().putJson(fullPath, consultation.toString());
//        JsonDocument result = bucket.insert(JsonDocument.create(id, consultation));

        log.info("inserted consultation: " + fullPath);

        return consultation;
    }

    private String generateConsultId(String patientId) {
        long nextSeq = SequenceGenerator.getNext(currentUserS3Crud(), CONSULTATION_ID_SEQ);

//        long nextSequence = bucket.counter(CONSULTATION_ID_SEQ, 1, 1).content();

        return patientId + CONSULTATION_PREFIX + format("%07d", nextSeq);
    }

    @Override
    public JsonObject updateConsultation(String id, String json) {
        JsonObject consultation = JsonObject.fromJson(json);
        Assert.isTrue(isNotEmpty(consultation.getString("doctor")), "doctor cannot be updated to empty");
        Assert.isTrue(isNotEmpty(consultation.getString("type")), "type cannot be updated to empty");
        String consultId = consultation.getString("consultId");
        Assert.isTrue(id.equals(consultId), "provided id does not match json consultId");
        Assert.notNull(getConsultation(id));
        String patientId = consultation.getString("patientId");

        Auditable.preUpdate(consultation);
        DateUtils.convertISODateTimeToDateString(consultation, "visitedOn");

        String fullPath = format(CONSULTS_PATH, patientId, id);
        PutObjectResult result = currentUserS3Crud().putJson(fullPath, consultation.toString());
//        JsonDocument result = bucket.upsert(JsonDocument.create(id, consultation));

        log.info("updated consultation: " + fullPath);

        return consultation;
    }

    @Override
    public JsonObject getConsultation(String id) {
        String patientId = id.replaceFirst(CONSULTATION_PREFIX + ".*", "");
        String fullPath = format(CONSULTS_PATH, patientId, id);
        String json = currentUserS3Crud().getString(fullPath);

        return JsonObject.fromJson(json);
    }

    @Override
    public JsonObject deleteConsultation(String id) {
        String patientId = id.replaceFirst(CONSULTATION_PREFIX + ".*", "");
        String fullPath = format(CONSULTS_PATH, patientId, id);
        currentUserS3Crud().deleteObject(fullPath);
//        JsonObject result = bucket.remove(id).content();

        log.info("deleted consultation: " + id);

        return null;
    }

    @Override
    public List<JsonObject> getConsultations(String patientId) {
        List<JsonObject> result = new ArrayList<>();
//        String condition = format("type='CONSULTATION' and patientId='%s' order by createdDate desc", patientId);
//        Statement statement = select("*").from(bucket.name()).where(condition).limit(1000); // todo paging
//        result = couchBaseQuery.query(statement);
        List<String> list = currentUserS3Crud().listNonFolderObjects(format(CONSULTS_PATH, patientId, ""));
        list.forEach(json -> result.add(JsonObject.fromJson(json)));

        return result;
    }
}
