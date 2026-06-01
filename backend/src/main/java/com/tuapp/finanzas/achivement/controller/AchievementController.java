package com.tuapp.finanzas.achievement.controller;

import com.tuapp.finanzas.achievement.dto.AchievementDto;
import com.tuapp.finanzas.achievement.service.AchievementService;
import com.tuapp.finanzas.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints del sistema de logros.
 * Patrón idéntico al resto de controladores del proyecto:
 * thin controller, delega completamente al servicio.
 */
@RestController
@RequestMapping("/api/achievements")
public class AchievementController {

    private final AchievementService achievementService;

    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    /**
     * GET /api/achievements
     * Devuelve todos los logros del catálogo con su estado para el usuario
     * autenticado (unlocked, unlockedAt, active).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AchievementDto>>> getAchievements() {
        List<AchievementDto> achievements = achievementService.getAchievementsForCurrentUser();
        return ResponseEntity.ok(new ApiResponse<>(achievements, "Logros obtenidos correctamente"));
    }

    /**
     * PUT /api/achievements/{achievementId}/select
     * Selecciona un logro desbloqueado como título activo del usuario.
     * Devuelve 400 si el logro no ha sido desbloqueado.
     */
    @PutMapping("/{achievementId}/select")
    public ResponseEntity<ApiResponse<Void>> selectActiveAchievement(
            @PathVariable Long achievementId) {
        achievementService.selectActiveAchievement(achievementId);
        return ResponseEntity.ok(new ApiResponse<>(null, "Título activo actualizado correctamente"));
    }

    /**
     * DELETE /api/achievements/active
     * Elimina el título activo actual del usuario (sin borrar el logro).
     */
    @DeleteMapping("/active")
    public ResponseEntity<ApiResponse<Void>> clearActiveAchievement() {
        achievementService.clearActiveAchievement();
        return ResponseEntity.ok(new ApiResponse<>(null, "Título activo eliminado correctamente"));
    }
}
