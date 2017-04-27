package com.acuman.service.s3;

import com.acuman.CbDocType;
import com.acuman.domain.Auditable;
import com.acuman.service.PatientService;
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
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Create index before query: CREATE PRIMARY INDEX on acuman using VIEW
 */
public class S3PatientService implements PatientService {
    private static final Logger log = LogManager.getLogger(S3PatientService.class);

    private static final String PATIENTS_PATH = "patients/";

    private static final String PATIENT_ID_SEQ = "patientIdSeq";

    public S3PatientService() {
    }


    @Override
    public JsonObject newPatient(String json) {
        String id = generateId();

        JsonObject patient = JsonObject.fromJson(json);
        patient.put("doctor", AuthUtil.currentUser());
        patient.put("patientId", id);
        patient.put("type", "PATIENT");
        Auditable.preInsert(patient);
        DateUtils.convertISODateTimeToDateString(patient, "dob");

        System.out.println("Uploading a new object to S3 from a file\n");
        PutObjectResult result = currentUserS3Crud().putJson(PATIENTS_PATH + id, patient.toString());
        // todo how to ensure not update existing record?
        log.info("inserted patient: " + PATIENTS_PATH + id);

        return patient;
    }

    // maintain putObejct
    private String generateId() {
        long nextSeq = SequenceGenerator.getNext(currentUserS3Crud(), PATIENT_ID_SEQ);

        return CbDocType.Patient + "-" + String.format("%07d", nextSeq);
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
        DateUtils.convertISODateTimeToDateString(patient, "dob");
        PutObjectResult result = currentUserS3Crud().putJson(PATIENTS_PATH + id, patient.toString());
        log.info("updated patient:" + PATIENTS_PATH + id);

        return patient;
    }

    @Override
    public JsonObject getPatient(String id) {
        String json = currentUserS3Crud().getString(PATIENTS_PATH + id);

        return JsonObject.fromJson(json);
    }

    @Override
    public JsonObject deletePatient(String id) {
        currentUserS3Crud().deleteObject(PATIENTS_PATH + id);

        log.info("deleted patient: " + id);

        return null;
    }

    @Override
    public List<JsonObject> getPatients() {
        List<JsonObject> result = new ArrayList<>();

        List<String> list = currentUserS3Crud().listNonFolderObjects(PATIENTS_PATH);
        list.forEach(json -> result.add(JsonObject.fromJson(json)));

        return result;
    }
}
