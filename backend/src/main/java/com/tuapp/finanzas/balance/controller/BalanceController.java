package com.tuapp.finanzas.balance.controller;

import com.tuapp.finanzas.balance.dto.MonthlyBalanceDto;
import com.tuapp.finanzas.balance.dto.YearlyBalanceDto;
import com.tuapp.finanzas.balance.service.BalanceService;
import com.tuapp.finanzas.user.entity.User;
import com.tuapp.finanzas.user.service.UserLookup;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/balance")
public class BalanceController {

    private final BalanceService balanceService;
    private final UserLookup userLookup;

    public BalanceController(BalanceService balanceService, UserLookup userLookup) {
        this.balanceService = balanceService;
        this.userLookup = userLookup;
    }

    @GetMapping("/monthly")
    public MonthlyBalanceDto monthly(
            @RequestParam int year,
            @RequestParam int month
    ) {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        User user = userLookup.findByUsername(auth.getName())
                .orElseThrow();

        return balanceService.getMonthlyBalance(user.getId(), year, month);
    }

    @GetMapping("/yearly")
    public YearlyBalanceDto yearly(@RequestParam int year) {

        var auth = SecurityContextHolder.getContext().getAuthentication();

        User user = userLookup.findByUsername(auth.getName())
                .orElseThrow();

        return balanceService.getYearlyBalance(user.getId(), year);
    }
}