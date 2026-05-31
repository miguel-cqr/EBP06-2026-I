package com.tuapp.finanzas.user.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Representa un token temporal de un solo uso para el flujo de recuperación
 * de contraseña. Cada solicitud de reset genera un registro independiente,
 * lo que permite auditoría y limpieza sin afectar la entidad User.
 *
 * Tabla generada por Hibernate: password_reset_tokens
 */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * UUID aleatorio enviado por email al usuario.
     * UNIQUE garantiza que no puedan existir dos solicitudes con el mismo token,
     * lo que sería un problema de seguridad crítico.
     */
    @Column(nullable = false, unique = true)
    private String token;

    /**
     * Usuario propietario del token.
     * ManyToOne porque un mismo usuario puede haber solicitado el reset varias veces
     * (por ejemplo, si no recibió el primer email). Cada solicitud es un registro nuevo.
     * FetchType.LAZY evita cargar el User completo en cada consulta del token.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Momento exacto en que el token deja de ser válido.
     * Se calcula al crear el token como: LocalDateTime.now() + 30 minutos.
     * El repositorio usa este campo para rechazar tokens vencidos en la consulta,
     * sin necesidad de cargar el objeto y comparar en Java.
     */
    @Column(nullable = false)
    private LocalDateTime expirationDate;

    /**
     * Indica si el token ya fue consumido.
     * Se marca true inmediatamente después de que el usuario cambia su contraseña.
     * Esto previene que el mismo enlace del email pueda usarse más de una vez,
     * incluso si todavía no ha expirado por tiempo.
     */
    @Column(nullable = false)
    private boolean used = false;

    /**
     * Marca temporal de creación para auditoría.
     * Permite en el futuro implementar limpieza automática de tokens viejos
     * mediante un job programado (@Scheduled).
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // ──────────────────────────────────────────────────────────────
    // Constructor por defecto requerido por JPA
    // ──────────────────────────────────────────────────────────────

    public PasswordResetToken() {}

    // ──────────────────────────────────────────────────────────────
    // Constructor de conveniencia para crear tokens nuevos.
    // No incluye 'id' porque lo asigna la base de datos,
    // ni 'used' porque siempre empieza en false.
    // ──────────────────────────────────────────────────────────────

    public PasswordResetToken(String token, User user, LocalDateTime expirationDate, LocalDateTime createdAt) {
        this.token = token;
        this.user = user;
        this.expirationDate = expirationDate;
        this.createdAt = createdAt;
    }

    // ──────────────────────────────────────────────────────────────
    // Getters y setters explícitos — sin Lombok, igual que User.java
    // ──────────────────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
