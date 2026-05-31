package com.tuapp.finanzas.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO que recibe el cuerpo del request POST /api/auth/reset-password.
 *
 * Vive en auth/dto/ por la misma razón que ForgotPasswordRequest:
 * es parte del flujo de autenticación sin JWT, donde el usuario presenta
 * el token recibido por email junto con su nueva contraseña.
 *
 * Contiene tres campos porque el frontend enviará los tres:
 * el token (que identifica la solicitud) y la nueva contraseña dos veces
 * (para que el usuario confirme que no cometió un error de tipeo).
 */
public class ResetPasswordRequest {

    /**
     * El UUID que llegó al usuario por email como parámetro del link.
     * Ejemplo del link en el email: https://frontend.com/reset-password?token=550e8400-...
     * El frontend extrae ese valor y lo incluye aquí.
     *
     * @NotBlank: evita que se envíe un token vacío que pasaría sin error
     * hasta la base de datos.
     */
    @NotBlank(message = "El token es obligatorio")
    private String token;

    /**
     * Nueva contraseña elegida por el usuario.
     *
     * @NotBlank: no puede estar vacía.
     * @Size(min = 8): regla mínima de seguridad consistente con lo que
     * típicamente se valida en el registro. Ajustar si UserService
     * aplica una regla distinta al crear usuarios.
     */
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String newPassword;

    /**
     * Confirmación de la nueva contraseña.
     * Se valida en el servicio que newPassword.equals(confirmPassword)
     * antes de persistir el cambio. Aquí solo se valida que no esté vacía;
     * la comparación lógica no es responsabilidad de las anotaciones de Bean Validation.
     */
    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    private String confirmPassword;

    public ResetPasswordRequest() {}

    public ResetPasswordRequest(String token, String newPassword, String confirmPassword) {
        this.token = token;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
