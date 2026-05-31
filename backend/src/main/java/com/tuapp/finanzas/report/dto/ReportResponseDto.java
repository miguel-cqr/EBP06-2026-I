package com.tuapp.finanzas.report.dto;

import com.tuapp.finanzas.transaction.dto.TransactionDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReportResponseDto {

    private String type;

    private LocalDate startDate;
    
    private LocalDate endDate;

    private BigDecimal total;

    private List<TransactionDto> transactions;

    private String message;
    
    private String fullName;
    
    private String currency;

    private BigDecimal totalIncome;
    
    private BigDecimal totalExpense;
    
    private BigDecimal balance;

    public ReportResponseDto() {
    }

    public ReportResponseDto(
            String type,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal total,
            List<TransactionDto> transactions,
            String message,
            String fullName,
            String currency,
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal balance
    ) {
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.total = total;
        this.transactions = transactions;
        this.message = message;
        this.fullName = fullName;
        this.currency = currency;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.balance = balance;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<TransactionDto> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionDto> transactions) {
        this.transactions = transactions;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
}