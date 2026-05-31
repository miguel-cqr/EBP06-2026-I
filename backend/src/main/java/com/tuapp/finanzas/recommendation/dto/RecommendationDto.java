package com.tuapp.finanzas.recommendation.dto;

public class RecommendationDto {
    private String category;
    private String message;

    public RecommendationDto() {}

    public RecommendationDto(String category, String message) {
        this.category = category;
        this.message = message;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}