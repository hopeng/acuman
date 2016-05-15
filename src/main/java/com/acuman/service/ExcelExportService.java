package com.acuman.service;

import com.acuman.service.couchbase.CouchBaseConsultationService;
import com.acuman.service.couchbase.CouchbasePatientService;
import com.acuman.util.AuthUtil;
import com.couchbase.client.java.document.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by hopeng on 15/05/2016.
 * export patients and consultation history to excel file
 */
public class ExcelExportService {
    private static final Logger log = LogManager.getLogger(ExcelExportService.class);

    private PatientService patientService = new CouchbasePatientService();
    private ConsultationService consultationService = new CouchBaseConsultationService();

    public Workbook buildPatientsExcel() {
        String doctor = AuthUtil.currentUser();
        log.info("builidng patient excel export for " + doctor);
        Workbook wb = new XSSFWorkbook();
        Sheet patientSheet = wb.createSheet(doctor + " Patient List");

        List<JsonObject> patients = patientService.getPatients(doctor);
        if (patients.size() == 0) {
            return wb;
        }

        writeJsonObjectsToSheet(patientSheet, patients);

        for (JsonObject patient : patients) {
            String patientId = patient.getString("patientId");
            List<JsonObject> consults = consultationService.getConsultations(patientId);
            Sheet consultationSheet = wb.createSheet(patientId);
            writeJsonObjectsToSheet(consultationSheet, consults);
        }

        return wb;
    }

    private void writeJsonObjectsToSheet(Sheet sheet, List<JsonObject> jsonObjects) {
        // get all headers
        Set<String> headers = new LinkedHashSet<>();
        for (JsonObject jsonObject : jsonObjects) {
            headers.addAll(jsonObject.getNames());
        }

        // write header row
        int columnIndex = 0;
        Row row = sheet.createRow(0);
        for (String fieldName : headers) {
            row.createCell(columnIndex).setCellValue(fieldName);
            columnIndex++;
        }

        // write data row
        int rowIndex = 1;
        for (JsonObject jsonObject : jsonObjects) {
            row = sheet.createRow(rowIndex);
            columnIndex = 0;
            for (String fieldName : headers) {
                row.createCell(columnIndex).setCellValue(jsonObject.getString(fieldName));
                columnIndex++;
            }
            rowIndex++;
        }
    }
}
