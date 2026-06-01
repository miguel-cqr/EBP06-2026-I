package com.tuapp.finanzas.achievement.entity;

import com.tuapp.finanzas.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Registro permanente de un logro obtenido por un usuario.
 * La constraint UNIQUE (user_id, achievement_id) garantiza
 * que un logro no se otorgue dos veces al mismo usuario.
 */
@Entity
@Table(
    name = "user_achievement",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_user_achievement",
        columnNames = {"user_id", "achievement_id"}
    )
)
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @Column(name = "unlocked_at", nullable = false)
    private LocalDateTime unlockedAt;

    public UserAchievement() {}

    public UserAchievement(User user, Achievement achievement, LocalDateTime unlockedAt) {
        this.user = user;
        this.achievement = achievement;
        this.unlockedAt = unlockedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Achievement getAchievement() { return achievement; }
    public void setAchievement(Achievement achievement) { this.achievement = achievement; }

    public LocalDateTime getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(LocalDateTime unlockedAt) { this.unlockedAt = unlockedAt; }
}
