package com.acuman.api;

import com.acuman.ContentTypeConstants;
import com.acuman.service.ExcelExportService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;

import static com.acuman.ApiConstants.API_DOWNLOAD_PATIENTS;
import static spark.Spark.get;

public class FileDownloadApi {
    private static final Logger log = LogManager.getLogger(FileDownloadApi.class);

    public static void configure() {
        ExcelExportService excelExportService = new ExcelExportService();

        get(API_DOWNLOAD_PATIENTS, (request, response) -> {
            Workbook workBook = excelExportService.buildPatientsExcel();

//            todo set file name
            response.type(ContentTypeConstants.XLSX);
            workBook.write(response.raw().getOutputStream());

            return response;
        });

    }


}

