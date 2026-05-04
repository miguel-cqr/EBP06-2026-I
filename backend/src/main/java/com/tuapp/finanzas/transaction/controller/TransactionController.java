package com.tuapp.finanzas.transaction.controller;

import com.tuapp.finanzas.transaction.dto.TransactionDto;
import com.tuapp.finanzas.user.entity.User;
import com.tuapp.finanzas.user.service.UserLookup;
import jakarta.validation.Valid;
import com.tuapp.finanzas.transaction.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserLookup userLookup;

    public TransactionController(TransactionService transactionService, UserLookup userLookup) {
        this.transactionService = transactionService;
        this.userLookup = userLookup;
    }

    @GetMapping
    public ResponseEntity<List<TransactionDto>> list() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userLookup.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(transactionService.findByUserId(user.getId()));
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
        return ResponseEntity.ok(transactionService.createExpense(dto));
    }

    @GetMapping("/balance")
    public ResponseEntity<Double> getBalance() {
        return ResponseEntity.ok(transactionService.getBalance());
    }
}
