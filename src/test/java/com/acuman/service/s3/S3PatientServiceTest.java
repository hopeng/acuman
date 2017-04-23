package com.acuman.service.s3;

import com.acuman.service.PatientService;
import com.acuman.util.AuthUtil;
import com.couchbase.client.java.document.json.JsonObject;
import org.junit.Test;

import java.util.List;

/**
 * Created by hopeng on 22/4/17.
 */
public class S3PatientServiceTest {
    PatientService service = new S3PatientService();

    @Test
    public void newPatient() throws Exception {
        String json = "{\n" +
                "  \"doctor\": \"fionafamilytcm\",\n" +
                "  \"firstName\": \"Isabel\",\n" +
                "  \"lastName\": \"Shih\",\n" +
                "  \"gender\": \"Male\",\n" +
//                "  \"dob\": \"1980-03-02\",\n" +
                "  \"healthFund\": \"HIF\"\n" +
                "}";
        JsonObject result = service.newPatient(json);

        System.out.println(result);
    }

    @Test
    public void updatePatient() throws Exception {
    }

    @Test
    public void getPatient() throws Exception {
        JsonObject p = service.getPatient("PATIENT-0000001");
        System.out.println(p);
    }

    @Test
    public void deletePatient() throws Exception {
        service.deletePatient("PATIENT-0000001");
    }

    @Test
    public void getPatients() throws Exception {
        List<JsonObject> result = service.getPatients(AuthUtil.currentUser());
        System.out.println(result);
    }
}