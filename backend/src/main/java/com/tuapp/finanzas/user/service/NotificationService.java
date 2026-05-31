package com.tuapp.finanzas.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:}")
    private String fromAddress;

    @Value("${app.password-reset.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Devuelve true únicamente si MAIL_USERNAME está definido en el entorno.
     * Con valor vacío (fallback por defecto), el envío se omite sin lanzar excepción,
     * lo que garantiza que la aplicación arranque correctamente sin configuración SMTP.
     */
    private boolean isSmtpConfigured() {
        return fromAddress != null && !fromAddress.isBlank();
    }

    /**
     * Notificación de cambio de contraseña exitoso.
     * Llamado desde UserServiceImpl.updatePassword() al finalizar un cambio autenticado.
     * Si SMTP no está configurado, solo loguea — no interrumpe el flujo.
     */
    public void notifyPasswordChanged(String email) {
        if (!isSmtpConfigured()) {
            logger.info("NOTIFICATION (SMTP not configured): Password changed for: {}", email);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(email);
            message.setSubject("Tu contraseña ha sido actualizada");
            message.setText(
                "Hola,\n\n" +
                "La contraseña de tu cuenta ha sido cambiada exitosamente.\n\n" +
                "Si no realizaste este cambio, contacta a soporte de inmediato.\n\n" +
                "Equipo de Finanzas"
            );
            mailSender.send(message);
            logger.info("Password changed notification sent to: {}", email);
        } catch (MailException ex) {
            // Fallo de entrega no interrumpe el flujo — el cambio ya fue persistido.
            logger.error("Failed to send password changed notification to {}: {}", email, ex.getMessage());
        }
    }

    /**
     * Envía el email con el link de recuperación de contraseña.
     *
     * El link apunta al frontend: {frontendUrl}/reset-password?token={resetToken}
     * El frontend extrae el token del query param y lo envía a POST /api/auth/reset-password.
     *
     * Comportamiento según entorno:
     *   - SMTP no configurado → loguea advertencia y retorna sin error.
     *     El token sigue disponible en la respuesta HTTP del endpoint /forgot-password
     *     para pruebas con Postman.
     *   - SMTP configurado, envío falla → lanza RuntimeException.
     *     PasswordResetServiceImpl la captura en try/catch y loguea el fallo
     *     sin interrumpir la respuesta HTTP al cliente.
     *   - SMTP configurado, envío exitoso → email entregado.
     *
     * @param toEmail    Email de destino del usuario registrado.
     * @param resetToken UUID generado por PasswordResetServiceImpl.
     */
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        if (!isSmtpConfigured()) {
            logger.warn("SMTP not configured — password reset email not sent to: {}. " +
                        "Token is available in the HTTP response for testing.", toEmail);
            return;
        }

        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
        logger.info("Sending password reset email to: {}", toEmail);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("Recuperación de contraseña");
            message.setText(
                "Hola,\n\n" +
                "Recibimos una solicitud para restablecer la contraseña de tu cuenta.\n\n" +
                "Haz clic en el siguiente enlace para crear una nueva contraseña:\n" +
                resetLink + "\n\n" +
                "Este enlace es válido por 30 minutos y solo puede usarse una vez.\n\n" +
                "Si no realizaste esta solicitud, puedes ignorar este mensaje.\n\n" +
                "Equipo de Finanzas"
            );
            mailSender.send(message);
            logger.info("Password reset email sent successfully to: {}", toEmail);
        } catch (MailException ex) {
            logger.error("Failed to send password reset email to {}: {}", toEmail, ex.getMessage());
            // Se relanza para que PasswordResetServiceImpl lo registre como fallo
            // de entrega. El cliente sigue recibiendo 200 OK porque ese servicio
            // captura esta excepción en su propio try/catch.
            throw new RuntimeException("No se pudo enviar el email de recuperación", ex);
        }
    }
}