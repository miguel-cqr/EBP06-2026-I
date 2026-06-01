package com.tuapp.finanzas.achievement.repository;

import com.tuapp.finanzas.achievement.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    /**
     * Devuelve todos los logros obtenidos por un usuario.
     * Usado en AchievementService para construir la lista completa
     * (obtenidos + no obtenidos) que se envía al frontend.
     */
    List<UserAchievement> findByUserId(Long userId);

    /**
     * Verifica si el usuario ya obtuvo un logro específico.
     * Usado antes de persistir para evitar duplicados
     * (segunda línea de defensa después de la constraint UNIQUE en BD).
     */
    boolean existsByUserIdAndAchievementId(Long userId, Long achievementId);
}
