package com.acuman;

import com.acuman.service.PatientService;
import com.acuman.service.couchbase.CouchbasePatientService;
import com.couchbase.client.java.document.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import spark.utils.Assert;

import java.util.List;

import static com.acuman.service.couchbase.CouchbasePatientService.DOCTOR;

public class AcuCouchbaseServiceTest {
    private static final Logger log = LogManager.getLogger(AcuCouchbaseServiceTest.class);

    PatientService patientService = new CouchbasePatientService();

    @Test
    public void testFindPatients() throws Exception {
        JsonObject result = patientService.getPatient("HFANG-PATIENT-14");
        log.info("one: " + result);

        List<JsonObject> searchResult = patientService.getPatients(DOCTOR);
        log.info("search: " + searchResult);
    }

    @Test
    public void test() {
        CouchbasePatientService acuService = new CouchbasePatientService();
        JsonObject p = acuService.newPatient("{\"initialVisit\":\"2016-04-21T11:21:10.430Z\",\"dob\":null}");
        JsonObject retrievedP = acuService.getPatient(p.getString("patientId"));
        log.info(p);
        log.info(retrievedP);
        Assert.isTrue(p.toString().equals(retrievedP.toString()), "not the same?!");
        List<JsonObject> result = acuService.getPatients(DOCTOR);

        log.info("search: " + result);
    }
}
