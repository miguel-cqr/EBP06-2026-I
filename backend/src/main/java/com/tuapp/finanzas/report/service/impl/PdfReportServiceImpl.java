package com.tuapp.finanzas.report.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.tuapp.finanzas.report.dto.ReportResponseDto;
import com.tuapp.finanzas.report.service.PdfReportService;
import com.tuapp.finanzas.report.service.ReportService;
import com.tuapp.finanzas.transaction.dto.TransactionDto;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PdfReportServiceImpl implements PdfReportService {

    private final ReportService reportService;

    public PdfReportServiceImpl(
            ReportService reportService
    ) {
        this.reportService = reportService;
    }

    @Override
    public byte[] generatePdfReport(
            String type,
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la fecha final"
            );
        }

        ReportResponseDto report =
                reportService.generateTransactionReport(
                        type,
                        startDate,
                        endDate
                );

        ByteArrayOutputStream out =
                new ByteArrayOutputStream();

        Document document =
                new Document(
                        PageSize.A4,
                        40,
                        40,
                        40,
                        40
                );

        PdfWriter.getInstance(
                document,
                out
        );

        document.open();

        Font titleFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        20
                );

        Font sectionFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        14
                );

        Font headerFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        12
                );

        Font normalFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA,
                        11
                );

        // =====================
        // TÍTULO
        // =====================

        Paragraph title =
                new Paragraph(
                        "Generación de Reportes",
                        titleFont
                );

        title.setAlignment(
                Element.ALIGN_LEFT
        );

        document.add(title);

        document.add(
                new Paragraph(" ")
        );

        // =====================
        // INFORMACIÓN GENERAL
        // =====================

        String reportTypeText =
                switch (report.getType()) {
                    case "INCOME" -> "Ingresos";
                    case "EXPENSE" -> "Gastos";
                    case "ALL" -> "General";
                    default -> report.getType();
                };

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "dd/MM/yyyy"
                );

        document.add(
                new Paragraph(
                        "Tipo: "
                                + reportTypeText
                                + "   |   Periodo: "
                                + report.getStartDate().format(formatter)
                                + " - "
                                + report.getEndDate().format(formatter)
                                + "   |   Registros: "
                                + report.getTransactions().size(),
                        normalFont
                )
        );

        document.add(
                new Paragraph(
                        "Usuario: "
                                + report.getFullName(),
                        normalFont
                )
        );

        document.add(
                new Paragraph(
                        "Moneda: "
                                + report.getCurrency(),
                        normalFont
                )
        );

        document.add(
                new Paragraph(
                        "Fecha de generación: "
                                + LocalDateTime.now().format(
                                DateTimeFormatter.ofPattern(
                                        "dd/MM/yyyy HH:mm"
                                )
                        ),
                        normalFont
                )
        );

        document.add(
                new Paragraph(" ")
        );

        // =====================
        // RESUMEN FINANCIERO
        // =====================

        if ("ALL".equalsIgnoreCase(report.getType())) {

        document.add(
                new Paragraph(
                        "Resumen Financiero",
                        sectionFont
                )
        );

        document.add(
                new Paragraph(" ")
        );

        PdfPTable summaryTable =
                new PdfPTable(3);

        summaryTable.setWidthPercentage(100);

        summaryTable.setWidths(
                new float[]{
                        1f,
                        1f,
                        1f
                }
        );

        summaryTable.addCell(
                new Phrase(
                        "Total ingresos\n"
                                + report.getTotalIncome(),
                        headerFont
                )
        );

        summaryTable.addCell(
                new Phrase(
                        "Total gastos\n"
                                + report.getTotalExpense(),
                        headerFont
                )
        );

        summaryTable.addCell(
                new Phrase(
                        "Balance\n"
                                + report.getBalance(),
                        headerFont
                )
        );

        document.add(summaryTable);

        } else {

        document.add(
                new Paragraph(
                        "Monto Total: "
                                + report.getTotal()
                                + " "
                                + (report.getCurrency() != null
                                ? report.getCurrency()
                                : ""),
                        sectionFont
                )
        );
        }

        document.add(
                new Paragraph(" ")
        );

        // =====================
        // DETALLE
        // =====================

        document.add(
                new Paragraph(
                        "Detalle de Transacciones",
                        sectionFont
                )
        );

        document.add(
                new Paragraph(" ")
        );

        if (report.getTransactions().isEmpty()) {

            document.add(
                    new Paragraph(
                            "No existen transacciones para el periodo seleccionado.",
                            normalFont
                    )
            );

        } else {

            PdfPTable table =
                    new PdfPTable(4);

            table.setWidthPercentage(
                    100
            );

            table.setWidths(
                    new float[]{
                            3f,
                            2f,
                            6f,
                            3f
                    }
            );

            table.addCell(
                    new Phrase(
                            "Fecha",
                            headerFont
                    )
            );

            table.addCell(
                    new Phrase(
                            "Tipo",
                            headerFont
                    )
            );

            table.addCell(
                    new Phrase(
                            "Descripción",
                            headerFont
                    )
            );

            table.addCell(
                    new Phrase(
                            "Monto",
                            headerFont
                    )
            );

            for (TransactionDto tx :
                    report.getTransactions()) {

                table.addCell(
                        tx.getDate() != null
                                ? tx.getDate()
                                .toLocalDate()
                                .toString()
                                : "-"
                );

                table.addCell(
                        tx.getType() != null
                                ? tx.getType().toString()
                                : "-"
                );

                table.addCell(
                        tx.getDescription() != null
                                ? tx.getDescription()
                                : "-"
                );

                table.addCell(
                        tx.getAmount() != null
                                ? tx.getAmount().toString()
                                : "0"
                );
            }

            document.add(
                    table
            );
        }

        document.add(
                new Paragraph(" ")
        );

        document.add(
                new Paragraph(" ")
        );

        document.close();

        return out.toByteArray();
    }
}