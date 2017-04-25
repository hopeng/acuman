package com.acuman.service.s3;

import com.acuman.CbDocType;
import com.acuman.domain.Auditable;
import com.acuman.service.PatientService;
import com.acuman.util.AuthUtil;
import com.acuman.util.DateUtils;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.couchbase.client.java.document.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.utils.Assert;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Create index before query: CREATE PRIMARY INDEX on acuman using VIEW
 * todo handle multiple buckets, one per doctor
 */
public class S3PatientService implements PatientService {
    private static final Logger log = LogManager.getLogger(S3PatientService.class);

    private static final String patientBucket = "acuman-" + AuthUtil.currentUser();
    private static final String patientDir = "patients/";

    private static final String PATIENT_ID_SEQ = "patientIdSeq";

    private S3Crud patientsCrud = new S3Crud(patientBucket);


    public S3PatientService() {
        System.out.println("Listing buckets");
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

        System.out.println("Uploading a new object to S3 from a file\n");
        PutObjectResult result = patientsCrud.putJson(patientDir + id, patient.toString());
        // todo how to ensure not update existing record?
        log.info("inserted patient, version=" + result.getVersionId());

        return patient;
    }

    // maintain putObejct
    private String generateId() {
        long seq = SequenceGenerator.getNext(patientsCrud, PATIENT_ID_SEQ);
        String id = CbDocType.Patient + "-" + String.format("%07d", seq);

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
        PutObjectResult result = patientsCrud.putJson(patientDir + id, patient.toString());
        log.info("updated patient, version=" + result.getVersionId());

        return patient;
    }

    @Override
    public JsonObject getPatient(String id) {
        JsonObject result = null;

        String json = patientsCrud.getString(patientDir + id);

        if (StringUtils.isNotEmpty(json)) {
            result = JsonObject.fromJson(json);

        }

        return result;
    }

    @Override
    public JsonObject deletePatient(String id) {
        patientsCrud.deleteObject(patientDir + id);

        log.info("deleted patient: " + id);

        return null;
    }

    @Override
    public List<JsonObject> getPatients(String doctor) {
        List<JsonObject> result = new ArrayList<>();

        List<String> list = patientsCrud.listNonFolderObjects(patientDir);
        list.forEach(json -> result.add(JsonObject.fromJson(json)));

        return result;
    }
}
