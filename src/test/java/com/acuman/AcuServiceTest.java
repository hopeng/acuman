package com.acuman;

import com.acuman.service.AcuService;
import com.couchbase.client.java.document.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import spark.utils.Assert;

import java.util.List;

import static com.acuman.service.AcuService.DOCTOR;

public class AcuServiceTest {
    private static final Logger log = LogManager.getLogger(AcuServiceTest.class);

    AcuService acuService = new AcuService();

    @Test
    public void testFindPatients() throws Exception {
        JsonObject result = acuService.getPatient("HFANG-PATIENT-14");
        log.info("one: " + result);

        List<JsonObject> searchResult = acuService.getPatients(DOCTOR);
        log.info("search: " + searchResult);
    }

    @Test
    public void test() {
        AcuService acuService = new AcuService();
        JsonObject p = acuService.newPatient("{\"initialVisit\":\"2016-04-21T11:21:10.430Z\",\"dob\":null}");
        JsonObject retrievedP = acuService.getPatient(p.getString("patientId"));
        log.info(p);
        log.info(retrievedP);
        Assert.isTrue(p.toString().equals(retrievedP.toString()), "not the same?!");
        List<JsonObject> result = acuService.getPatients(DOCTOR);

        log.info("search: " + result);
        acuService.destroy();
    }
}
