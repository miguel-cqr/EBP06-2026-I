package com.tuapp.finanzas.report.service;

import com.tuapp.finanzas.report.dto.ReportResponseDto;
import java.time.LocalDate;

public interface ReportService {

    ReportResponseDto generateTransactionReport(
            String type,
            LocalDate startDate,
            LocalDate endDate
    );
}