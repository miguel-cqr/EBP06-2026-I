package com.tuapp.finanzas.user.repository;

import com.tuapp.finanzas.user.entity.PasswordResetToken;
import com.tuapp.finanzas.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Busca un token por su valor string sin filtrar por estado.
     * Usado en resetPassword para obtener el token y luego verificar
     * su estado con mensajes de error específicos por caso.
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Busca un token válido: existe, no fue usado, y no ha expirado.
     * Una sola consulta cubre las tres condiciones de validez simultáneamente.
     */
    Optional<PasswordResetToken> findByTokenAndUsedFalseAndExpirationDateAfter(
            String token,
            LocalDateTime now
    );

    /**
     * Marca como usados todos los tokens activos (used = false) del usuario dado.
     *
     * Se llama en requestPasswordReset antes de generar un token nuevo,
     * garantizando que solo el token más reciente sea válido en cada momento.
     * Sin esto, solicitudes repetidas acumularían tokens activos simultáneos.
     *
     * @Modifying + @Query son obligatorios para operaciones UPDATE con JPQL.
     * @Transactional en el método del servicio que llama a este método cubre
     * la atomicidad de la invalidación + creación del token nuevo.
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.used = true WHERE t.user = :user AND t.used = false")
    void invalidatePreviousTokens(@Param("user") User user);

    /**
     * Elimina tokens expirados. Disponible para un job de limpieza futuro
     * (@Scheduled) que mantenga la tabla saneada.
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expirationDate < :now")
    void deleteByExpirationDateBefore(@Param("now") LocalDateTime now);
}