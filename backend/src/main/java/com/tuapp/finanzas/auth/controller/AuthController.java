package com.tuapp.finanzas.auth.controller;

import com.tuapp.finanzas.auth.dto.AuthResponse;
import com.tuapp.finanzas.auth.dto.ForgotPasswordRequest;
import com.tuapp.finanzas.auth.dto.LoginRequest;
import com.tuapp.finanzas.auth.dto.ResetPasswordRequest;
import com.tuapp.finanzas.auth.service.PasswordResetService;
import com.tuapp.finanzas.user.dto.CreateUserRequest;
import com.tuapp.finanzas.user.dto.UserDto;
import com.tuapp.finanzas.user.service.UserLookup;
import com.tuapp.finanzas.user.service.UserService;
import com.tuapp.finanzas.auth.service.JwtService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final UserLookup userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordResetService passwordResetService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(
            UserService userService,
            UserLookup userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            PasswordResetService passwordResetService
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(java.util.Map.of("error", "Unauthorized"));
        }
        var opt = userRepository.findByUsername(auth.getName());
        if (opt.isPresent()) {
            var u = opt.get();
            return ResponseEntity.ok(new UserDto(u.getId(), u.getUsername(), u.getEmail(), u.getFullName(), u.getCurrency(), u.getRole()));
        }
        return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND).body(java.util.Map.of("error", "User not found"));
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("auth ok");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody CreateUserRequest req) {
        logger.info("Register request body: {}", req);
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("error", "Email already exists"));
        }
        UserDto created = userService.create(req);
        return ResponseEntity.ok(created);
    }

    // Debugging helper: echoes parsed body without validation
    @PostMapping("/echo")
    public ResponseEntity<CreateUserRequest> echo(@RequestBody CreateUserRequest req) {
        return ResponseEntity.ok(req);
    }

    // Debugging helper: return raw request body as text
    @PostMapping("/raw")
    public ResponseEntity<String> raw(@RequestBody String body) {
        try {
            logger.info("Raw body: {}", body);
            return ResponseEntity.ok(body);
        } catch (Exception ex) {
            logger.error("Error in raw endpoint", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        var maybeUser = userRepository.findByUsername(req.getUsername());
        if (maybeUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of("error", "Invalid credentials"));
        }
        var user = maybeUser.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of("error", "Invalid credentials"));
        }
        String token = jwtService.generateToken(user.getUsername());
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("token", token);
        resp.put("userId", user.getId());
        resp.put("username", user.getUsername());
        resp.put("fullName", user.getFullName());
        return ResponseEntity.ok(resp);
    }

    /**
     * Paso 1 del flujo: el usuario solicita un email de recuperación.
     *
     * SEGURIDAD — respuesta genérica intencional:
     * Este endpoint SIEMPRE devuelve 200 OK con el mismo mensaje,
     * independientemente de si el email existe en la base de datos o no.
     * Esto previene user enumeration attacks: un atacante no puede usar
     * este endpoint para descubrir qué emails están registrados en el sistema.
     *
     * El campo "resetToken" en la respuesta es TEMPORAL para facilitar pruebas
     * en Postman sin necesidad de acceder al email. Debe eliminarse cuando
     * la integración con Gmail SMTP esté activa y el token viaje solo por email.
     *
     * POST /api/auth/forgot-password
     * Body: { "email": "usuario@ejemplo.com" }
     * Respuesta exitosa: 200 OK siempre
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        logger.info("Password reset requested for email: {}", req.getEmail());

        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("message", "Si el email está registrado, recibirás un enlace de recuperación en breve.");

        try {
            // El servicio genera el token, lo persiste y lo retorna.
            // Si el email no existe, lanza RuntimeException — que capturamos
            // aquí para devolver igualmente 200 OK (respuesta genérica de seguridad).
            String resetToken = passwordResetService.requestPasswordReset(req.getEmail());

            // TODO: eliminar este campo cuando Gmail SMTP esté integrado.
            // Por ahora expone el token en la respuesta para poder probar
            // el endpoint POST /reset-password directamente desde Postman.
            resp.put("resetToken", resetToken);

        } catch (RuntimeException ex) {
            // Intencionalmente silenciado: no se altera la respuesta HTTP.
            // El log permite diagnosticar en servidor sin exponer información al cliente.
            logger.warn("Password reset requested for non-existent email: {}", req.getEmail());
        }

        return ResponseEntity.ok(resp);
    }

    /**
     * Paso 2 del flujo: el usuario envía el token y su nueva contraseña.
     *
     * El token llega desde el link del email (cuando SMTP esté activo) o
     * directamente desde Postman durante el desarrollo usando el valor
     * devuelto por /forgot-password.
     *
     * A diferencia de /forgot-password, este endpoint SÍ devuelve errores
     * específicos cuando el token es inválido, expirado o ya fue usado.
     * Eso es seguro porque el token en sí no revela información sobre
     * qué usuarios existen — solo dice si ese UUID específico es válido.
     *
     * POST /api/auth/reset-password
     * Body: { "token": "uuid", "newPassword": "...", "confirmPassword": "..." }
     * Respuesta exitosa: 200 OK
     * Respuesta de error: 400 Bad Request con mensaje específico
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        logger.info("Password reset attempt with token: {}", req.getToken());

        // Validación de confirmación: newPassword y confirmPassword deben coincidir.
        // Esta validación no puede hacerse con anotaciones de Bean Validation porque
        // requiere comparar dos campos del mismo objeto — es responsabilidad del controller
        // o del servicio. Se hace aquí para devolver un error HTTP claro antes de
        // llegar al servicio.
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("error", "Las contraseñas no coinciden"));
        }

        try {
            passwordResetService.resetPassword(req.getToken(), req.getNewPassword());
            logger.info("Password successfully reset using token: {}", req.getToken());
            return ResponseEntity.ok(java.util.Map.of("message", "Contraseña actualizada correctamente"));

        } catch (RuntimeException ex) {
            // Las RuntimeException del servicio tienen mensajes descriptivos:
            // "Token de recuperación inválido"
            // "El token de recuperación ha expirado"
            // "Este token de recuperación ya fue utilizado"
            // Se retornan directamente al cliente — son seguros de exponer
            // porque no revelan información sobre usuarios del sistema.
            logger.warn("Password reset failed for token {}: {}", req.getToken(), ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("error", ex.getMessage()));
        }
    }
}