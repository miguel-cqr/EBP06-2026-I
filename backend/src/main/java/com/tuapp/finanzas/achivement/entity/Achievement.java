package com.tuapp.finanzas.achievement.entity;

import jakarta.persistence.*;

/**
 * Catálogo de logros disponibles en la aplicación.
 * Los registros son inmutables y se inicializan al arrancar
 * via AchievementDataInitializer.
 */
@Entity
@Table(name = "achievement")
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Clave programática única. Usada en AchievementService
     * para identificar cada logro sin depender del ID de BD.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    public Achievement() {}

    public Achievement(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
