package com.tuapp.finanzas.auth.service.impl;

import com.tuapp.finanzas.auth.service.PasswordResetService;
import com.tuapp.finanzas.user.entity.PasswordResetToken;
import com.tuapp.finanzas.user.entity.User;
import com.tuapp.finanzas.user.repository.PasswordResetTokenRepository;
import com.tuapp.finanzas.user.repository.UserRepository;
import com.tuapp.finanzas.user.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetServiceImpl.class);
    private static final int TOKEN_EXPIRY_MINUTES = 30;

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public PasswordResetServiceImpl(
            PasswordResetTokenRepository passwordResetTokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            NotificationService notificationService
    ) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    /**
     * Inicia el flujo de recuperación para el email dado.
     *
     * @Transactional cubre la invalidación de tokens anteriores y la creación
     * del nuevo como una unidad atómica. Si algo falla, ninguno de los dos
     * cambios queda persistido.
     */
    @Override
    @Transactional
    public String requestPasswordReset(String email) {
        logger.info("Password reset requested for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Password reset requested for non-existent email: {}", email);
                    return new RuntimeException("No existe ningún usuario con ese email");
                });

        // Invalidar tokens anteriores del mismo usuario antes de crear uno nuevo.
        // Garantiza que solo el token más reciente sea válido en cada momento,
        // evitando que solicitudes repetidas acumulen tokens activos simultáneos.
        passwordResetTokenRepository.invalidatePreviousTokens(user);
        logger.info("Previous tokens invalidated for user id: {}", user.getId());

        String token = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = now.plusMinutes(TOKEN_EXPIRY_MINUTES);

        PasswordResetToken resetToken = new PasswordResetToken(token, user, expiration, now);
        passwordResetTokenRepository.save(resetToken);

        logger.info("Password reset token created for user id: {}, expires at: {}", user.getId(), expiration);

        // El envío de email es un efecto secundario: su fallo no revierte
        // la transacción ni interrumpe la respuesta al cliente.
        try {
            notificationService.sendPasswordResetEmail(user.getEmail(), token);
        } catch (RuntimeException ex) {
            logger.error("Email delivery failed for user id: {}. Token is still valid.", user.getId());
        }

        // El token se retorna para pruebas con Postman mientras no haya SMTP.
        // TODO: cuando el frontend confirme que el flujo por email funciona,
        // eliminar el campo "resetToken" de la respuesta en AuthController.
        return token;
    }

    /**
     * Ejecuta el cambio de contraseña usando el token recibido.
     * @Transactional garantiza que el update de contraseña y el marcado del
     * token como usado sean atómicos — o ambos ocurren, o ninguno.
     */
    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        logger.info("Password reset attempt with token: {}", token);

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    logger.warn("Password reset attempted with non-existent token: {}", token);
                    return new RuntimeException("Token de recuperación inválido");
                });

        if (resetToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            logger.warn("Password reset attempted with expired token for user id: {}",
                    resetToken.getUser().getId());
            throw new RuntimeException("El token de recuperación ha expirado");
        }

        if (resetToken.isUsed()) {
            logger.warn("Password reset attempted with already-used token for user id: {}",
                    resetToken.getUser().getId());
            throw new RuntimeException("Este token de recuperación ya fue utilizado");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        logger.info("Password successfully reset for user id: {}", user.getId());
    }
}