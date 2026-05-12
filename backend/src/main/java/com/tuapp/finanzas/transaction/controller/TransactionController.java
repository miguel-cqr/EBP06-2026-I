// transaction/controller/TransactionController.java
package com.tuapp.finanzas.transaction.controller;

import com.tuapp.finanzas.transaction.dto.TransactionDto;
import com.tuapp.finanzas.transaction.facade.ExpenseFacade;
import com.tuapp.finanzas.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final ExpenseFacade expenseFacade; // ✅ inyectamos el facade

    public TransactionController(TransactionService transactionService,
                                 ExpenseFacade expenseFacade) {
        this.transactionService = transactionService;
        this.expenseFacade = expenseFacade;
    }

    @GetMapping
    public ResponseEntity<List<TransactionDto>> list() {
        return ResponseEntity.ok(transactionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> get(@PathVariable Long id) {
        TransactionDto t = transactionService.findById(id);
        return t == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(t);
    }

    @PostMapping
    public ResponseEntity<TransactionDto> create(@Valid @RequestBody TransactionDto dto) {
        return ResponseEntity.ok(transactionService.create(dto));
    }

    @PostMapping("/expense")
    public ResponseEntity<TransactionDto> createExpense(@RequestBody TransactionDto dto) {
        // ✅ El controlador delega al facade — no sabe nada de alertas
        return ResponseEntity.ok(expenseFacade.registerExpense(dto));
    }

    @GetMapping("/balance")
    public ResponseEntity<Double> getBalance() {
        return ResponseEntity.ok(transactionService.getBalance());
    }
}