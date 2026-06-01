package com.tuapp.finanzas.achievement.repository;

import com.tuapp.finanzas.achievement.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    /**
     * Usado por AchievementDataInitializer para verificar si un logro
     * ya existe antes de insertarlo, garantizando idempotencia en
     * cada arranque de la aplicación.
     */
    Optional<Achievement> findByCode(String code);
}
