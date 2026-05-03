package com.tuapp.finanzas.alert.controller;

import com.tuapp.finanzas.alert.entity.Alert;
import com.tuapp.finanzas.alert.repository.AlertRepository;
import com.tuapp.finanzas.user.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertRepository alertRepository;

    public AlertController(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @GetMapping
    public List<Alert> getAlerts(Authentication auth) {
        Long userId = ((User) auth.getPrincipal()).getId();
        return alertRepository.findByUserId(userId);
    }
}
