package com.tuapp.finanzas.budget.service.impl;

import com.tuapp.finanzas.budget.dto.BudgetDto;
import com.tuapp.finanzas.budget.entity.Budget;
import com.tuapp.finanzas.budget.repository.BudgetRepository;
import com.tuapp.finanzas.budget.service.BudgetService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;

    public BudgetServiceImpl(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @Override
    public BudgetDto create(BudgetDto dto) {
        if (dto.getLimitAmount() == null || dto.getLimitAmount().doubleValue() <= 0) {
            throw new IllegalArgumentException("limitAmount must be greater than zero");
        }
        Budget b = new Budget();
        b.setName(dto.getName());
        b.setLimitAmount(dto.getLimitAmount());
        Budget saved = budgetRepository.save(b);
        return toDto(saved);
    }

    @Override
    public List<BudgetDto> findAll() {
        return budgetRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public BudgetDto findById(Long id) {
        return budgetRepository.findById(id).map(this::toDto).orElse(null);
    }

    private BudgetDto toDto(Budget b) {
        return new BudgetDto(b.getId(), b.getName(), b.getLimitAmount());
    }


}
