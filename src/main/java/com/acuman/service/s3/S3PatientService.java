package com.acuman.service.s3;

import com.acuman.CbDocType;
import com.acuman.domain.Auditable;
import com.acuman.service.PatientService;
import com.acuman.util.AuthUtil;
import com.acuman.util.DateUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.couchbase.client.java.document.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.utils.Assert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Create index before query: CREATE PRIMARY INDEX on acuman using VIEW
 */
public class S3PatientService implements PatientService {
    private static final Logger log = LogManager.getLogger(S3PatientService.class);

    private static final String CONTENT_TYPE = "application/json";
    private static final String patientBucket = "acuman-fiona-huang";
    private static final String patientDir = "patients/";


    private static final String PATIENT_ID_SEQ = "patientIdSeq";
    AmazonS3 s3 = new AmazonS3Client();


    public S3PatientService() {
        for (String bucketName : asList(patientBucket, "mytest")) {
            if (!s3.doesBucketExist(bucketName)) {
                System.out.println("Creating bucket " + bucketName + "\n");
                s3.createBucket(bucketName);
            }
        }

        System.out.println("Listing buckets");
        for (com.amazonaws.services.s3.model.Bucket bucket : s3.listBuckets()) {
            System.out.println(" - " + bucket.getName());
        }
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
        PutObjectResult result = putObject(id, patient.toString());
        // todo how to ensure not update existing record?
        log.info("inserted patient, version=" + result.getVersionId());

        return patient;
    }

    private PutObjectResult putObject(String id, String json) {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(CONTENT_TYPE);
        metadata.setContentLength(bytes.length);
        return s3.putObject(patientBucket, patientDir + id, new ByteArrayInputStream(bytes), metadata);
    }

    // maintain putObejct
    private String generateId() {
        // https://forums.aws.amazon.com/thread.jspa?messageID=321320
        long nextSequence = 1L;
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
        PutObjectResult result = putObject(id, patient.toString());
        log.info("updated patient, version=" + result.getVersionId());

        return patient;
    }

    @Override
    public JsonObject getPatient(String id) {
        JsonObject result = null;

        S3Object obj = s3.getObject(patientBucket, patientDir + id);

        try {
            String json = IOUtils.toString(obj.getObjectContent());
            result = JsonObject.fromJson(json);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public JsonObject deletePatient(String id) {
        s3.deleteObject(patientBucket, patientDir + id);

        log.info("deleted patient: " + id);

        return null;
    }

    @Override
    public List<JsonObject> getPatients(String doctor) {
        ObjectListing patientList = s3.listObjects(new ListObjectsRequest()
                .withBucketName(patientBucket)
                .withPrefix(patientDir)
                .withDelimiter("/")
        );
        List<JsonObject> result = new ArrayList<>();
        for (S3ObjectSummary objectSummary : patientList.getObjectSummaries()) {
            JsonObject p = getPatient(objectSummary.getKey().replaceFirst("^.*/", ""));
            if (p != null) {
                result.add(p);
            }
        }

        return result;
    }
}
