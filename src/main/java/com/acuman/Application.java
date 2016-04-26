package com.acuman;

import com.acuman.api.ConsultationsApi;
import com.acuman.api.PatientsApi;
import com.acuman.service.ConsultationService;
import com.acuman.service.PatientService;
import com.acuman.service.couchbase.CouchBaseConsultationService;
import com.acuman.service.couchbase.CouchbasePatientService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static spark.Spark.externalStaticFileLocation;


public class Application {
    private static final Logger log = LogManager.getLogger(Application.class);


    public static void main(String[] args) {
        externalStaticFileLocation("static/");

        PatientService patientService = new CouchbasePatientService();
        PatientsApi.configure(patientService);

        ConsultationService consultationService = new CouchBaseConsultationService();
        ConsultationsApi.configure(consultationService);
    }
}
