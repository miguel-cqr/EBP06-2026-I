package com.tuapp.finanzas.report.service;

import java.time.LocalDate;

public interface PdfReportService {

    byte[] generatePdfReport(
            String type,
            LocalDate startDate,
            LocalDate endDate
    );
}
