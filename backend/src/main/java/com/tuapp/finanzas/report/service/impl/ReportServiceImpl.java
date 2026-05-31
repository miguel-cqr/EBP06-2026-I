package com.tuapp.finanzas.report.service.impl;

import com.tuapp.finanzas.report.dto.ReportResponseDto;
import com.tuapp.finanzas.report.service.ReportService;
import com.tuapp.finanzas.transaction.dto.TransactionDto;
import com.tuapp.finanzas.transaction.entity.Transaction;
import com.tuapp.finanzas.transaction.entity.Transaction.TransactionType;
import com.tuapp.finanzas.transaction.repository.TransactionRepository;
import com.tuapp.finanzas.user.entity.User;
import com.tuapp.finanzas.user.service.UserLookup;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private final TransactionRepository transactionRepository;
    private final UserLookup userLookup;

    public ReportServiceImpl(
            TransactionRepository transactionRepository,
            UserLookup userLookup
    ) {
        this.transactionRepository = transactionRepository;
        this.userLookup = userLookup;
    }

    @Override
    public ReportResponseDto generateTransactionReport(
            String type,
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la fecha final"
            );
        }

        var auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        User user = userLookup.findByUsername(auth.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));


        OffsetDateTime start = startDate.atStartOfDay().atOffset(ZoneOffset.UTC);

        OffsetDateTime end = endDate.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);

        List<TransactionDto> transactions;

        TransactionType transactionType = null;


        if ("ALL".equalsIgnoreCase(type)) {
        transactionType = null;
        transactions =
                transactionRepository
                        .findByUserIdAndDateBetweenOrderByDateDesc(
                                user.getId(),
                                start,
                                end
                        )
                        .stream()
                        .map(this::toDto)
                        .toList();


        } else {
        transactionType =
                TransactionType.valueOf(type.toUpperCase());
                
        transactions =
                transactionRepository
                        .findByUserIdAndTypeAndDateBetweenOrderByDateDesc(
                                user.getId(),
                                transactionType,
                                start,
                                end
                        )
                        .stream()
                        .map(this::toDto)
                        .toList();

        }

        BigDecimal totalIncome = transactions.stream()
        .filter(tx -> "INCOME".equalsIgnoreCase(tx.getType()))
        .map(TransactionDto::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
        .filter(tx -> "EXPENSE".equalsIgnoreCase(tx.getType()))
        .map(TransactionDto::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalIncome.subtract(totalExpense);

        BigDecimal total = transactions.stream()
                .map(TransactionDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ReportResponseDto response =
                new ReportResponseDto();

        response.setType(type.toUpperCase());
        response.setStartDate(startDate);
        response.setEndDate(endDate);
        response.setTotal(total);
        response.setTransactions(transactions);
        response.setTotalIncome(totalIncome);
        response.setTotalExpense(totalExpense);
        response.setBalance(balance);
        response.setFullName(user.getFullName());
        response.setCurrency(user.getCurrency());

        if (transactions.isEmpty()) {

            if (transactionType == TransactionType.EXPENSE) {
                response.setMessage(
                        "No existen gastos registrados"
                );
            } else {
                response.setMessage(
                        "No existen ingresos registrados"
                );
            }
        }

        return response;
    }

    private TransactionDto toDto(Transaction transaction) {

        TransactionDto dto = new TransactionDto();

        dto.setType(transaction.getType().name());
        dto.setId(transaction.getId());
        dto.setAmount(transaction.getAmount());
        dto.setDescription(transaction.getDescription());
        dto.setDate(transaction.getDate());

        if (transaction.getCategory() != null) {
            dto.setCategoryId(
                    transaction.getCategory().getId()
            );
        }

        if (transaction.getUser() != null) {
            dto.setUserId(
                    transaction.getUser().getId()
            );
        }

        return dto;
    }
}