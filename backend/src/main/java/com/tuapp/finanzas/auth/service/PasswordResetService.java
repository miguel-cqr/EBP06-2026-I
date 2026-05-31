package com.tuapp.finanzas.auth.service;

/**
 * Contrato del flujo de recuperación de contraseña.
 *
 * Vive en auth/service/ porque este flujo pertenece al dominio de
 * autenticación: el usuario no está logueado y está intentando recuperar
 * acceso. Es análogo a JwtService.java que ya existe en el mismo paquete.
 *
 * Declarar una interfaz separada de la implementación sigue el patrón
 * ya establecido en el proyecto (UserService / UserServiceImpl), y permite
 * en el futuro sustituir la implementación o crear un mock para pruebas
 * sin modificar el controller ni otros consumidores.
 */
public interface PasswordResetService {

    /**
     * Inicia el flujo de recuperación para el email dado.
     *
     * Genera un token UUID, lo persiste asociado al usuario y lo retorna.
     * En pasos posteriores este método también disparará el envío del email.
     *
     * @param email  Email del usuario que solicita el reset.
     * @return       El token generado (útil para testing hasta que SMTP esté listo).
     * @throws RuntimeException si no existe ningún usuario con ese email.
     */
    String requestPasswordReset(String email);

    /**
     * Ejecuta el cambio de contraseña usando el token recibido por email.
     *
     * Valida que el token exista, no haya expirado y no haya sido usado.
     * Actualiza la contraseña del usuario y marca el token como consumido.
     *
     * @param token        UUID recibido como parámetro del link en el email.
     * @param newPassword  Nueva contraseña en texto plano (se cifrará en pasos posteriores).
     * @throws RuntimeException si el token no existe, expiró o ya fue usado.
     */
    void resetPassword(String token, String newPassword);
}
