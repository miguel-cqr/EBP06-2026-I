package com.tuapp.finanzas.recommendation.controller;

import com.tuapp.finanzas.recommendation.dto.RecommendationDto;
import com.tuapp.finanzas.recommendation.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public ResponseEntity<List<RecommendationDto>> getRecommendations() {
        return ResponseEntity.ok(recommendationService.getRecommendations());
    }
}