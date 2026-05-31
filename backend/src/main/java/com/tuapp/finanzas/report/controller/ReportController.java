package com.tuapp.finanzas.report.controller;

import com.tuapp.finanzas.report.service.ReportService;
import com.tuapp.finanzas.report.service.PdfReportService;

import java.time.LocalDate;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final PdfReportService pdfReportService;

    public ReportController(ReportService reportService, PdfReportService pdfReportService) {
        this.reportService = reportService;
        this.pdfReportService = pdfReportService;
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> generateReport(
                @RequestParam String type,
                @RequestParam LocalDate startDate,
                @RequestParam LocalDate endDate
    ) {

        return ResponseEntity.ok(
                reportService.generateTransactionReport(
                        type,
                        startDate,
                        endDate
                )
        );
    }

    @GetMapping("/transactions/pdf")
    public ResponseEntity<byte[]> generatePdf(
            @RequestParam String type,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {

        byte[] pdf =
                pdfReportService.generatePdfReport(
                        type,
                        startDate,
                        endDate
                );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=reporte.pdf"
                )
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .body(pdf);
    }
}