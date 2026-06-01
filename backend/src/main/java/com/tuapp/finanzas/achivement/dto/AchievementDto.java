package com.tuapp.finanzas.achievement.dto;

import java.time.LocalDateTime;

/**
 * DTO que representa un logro desde la perspectiva del usuario autenticado.
 * Incluye tanto los logros obtenidos como los no obtenidos (unlocked = false),
 * permitiendo al frontend mostrar el progreso completo.
 */
public class AchievementDto {

    private Long id;
    private String code;
    private String name;
    private String description;

    /** true si el usuario ya desbloqueó este logro */
    private boolean unlocked;

    /** Fecha en que se desbloqueó. null si unlocked = false */
    private LocalDateTime unlockedAt;

    /** true si este logro es el título activo actualmente seleccionado */
    private boolean active;

    public AchievementDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }

    public LocalDateTime getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(LocalDateTime unlockedAt) { this.unlockedAt = unlockedAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
