package com.tuapp.finanzas.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO que recibe el cuerpo del request POST /api/auth/forgot-password.
 *
 * Vive en auth/dto/ porque este flujo es de autenticación: el usuario
 * no está logueado y está intentando recuperar acceso a su cuenta.
 * Es análogo a LoginRequest.java que ya existe en el mismo paquete.
 *
 * Solo contiene el email — no se pide username ni ningún otro dato,
 * para mantener el proceso simple desde el frontend.
 */
public class ForgotPasswordRequest {

    /**
     * Email del usuario que solicita el reset.
     *
     * @NotBlank: rechaza null, cadena vacía y cadenas con solo espacios.
     * @Email: valida que el formato sea un email válido (contiene @, dominio, etc.)
     * antes de llegar al servicio. Esto evita consultas innecesarias a la base de datos
     * con valores que nunca podrían corresponder a un usuario.
     *
     * La validación se activa gracias a @Valid en el controller.
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;

    public ForgotPasswordRequest() {}

    public ForgotPasswordRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
