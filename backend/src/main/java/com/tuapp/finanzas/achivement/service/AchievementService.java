package com.tuapp.finanzas.achievement.service;

import com.tuapp.finanzas.achievement.dto.AchievementDto;
import com.tuapp.finanzas.achievement.entity.Achievement;
import com.tuapp.finanzas.achievement.entity.UserAchievement;
import com.tuapp.finanzas.achievement.repository.AchievementRepository;
import com.tuapp.finanzas.achievement.repository.UserAchievementRepository;
import com.tuapp.finanzas.balance.dto.MonthlyBalanceDto;
import com.tuapp.finanzas.balance.service.BalanceService;
import com.tuapp.finanzas.transaction.event.TransactionSavedEvent;
import com.tuapp.finanzas.user.entity.User;
import com.tuapp.finanzas.user.repository.UserRepository;
import com.tuapp.finanzas.user.service.UserLookup;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AchievementService {

    // Códigos de logros — fuente de verdad programática
    public static final String CODE_PREMIUM_ADULT          = "PREMIUM_ADULT";
    public static final String CODE_PRO_SAVER              = "PRO_SAVER";
    public static final String CODE_SURVIVAL_MODE          = "SURVIVAL_MODE";
    public static final String CODE_UNCONTROLLED_SPENDING  = "UNCONTROLLED_SPENDING";

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final BalanceService balanceService;
    private final UserLookup userLookup;
    private final UserRepository userRepository;

    public AchievementService(
            AchievementRepository achievementRepository,
            UserAchievementRepository userAchievementRepository,
            BalanceService balanceService,
            UserLookup userLookup,
            UserRepository userRepository) {
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.balanceService = balanceService;
        this.userLookup = userLookup;
        this.userRepository = userRepository;
    }

    // -------------------------------------------------------------------------
    // QUERY — Lista todos los logros con estado del usuario autenticado
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<AchievementDto> getAchievementsForCurrentUser() {
        User user = resolveCurrentUser();
        return buildAchievementList(user);
    }

    // -------------------------------------------------------------------------
    // COMMAND — Selecciona un logro desbloqueado como título activo
    // -------------------------------------------------------------------------

    @Transactional
    public void selectActiveAchievement(Long achievementId) {
        User user = resolveCurrentUser();

        // Valida que el logro exista en el catálogo
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new RuntimeException("Logro no encontrado"));

        // Valida que el usuario realmente lo haya desbloqueado
        boolean unlocked = userAchievementRepository
                .existsByUserIdAndAchievementId(user.getId(), achievement.getId());

        if (!unlocked) {
            throw new RuntimeException(
                "No puedes seleccionar un logro que no has desbloqueado todavía");
        }

        user.setActiveAchievementId(achievementId);
        userRepository.save(user);
    }

    // -------------------------------------------------------------------------
    // COMMAND — Deselecciona el título activo actual
    // -------------------------------------------------------------------------

    @Transactional
    public void clearActiveAchievement() {
        User user = resolveCurrentUser();
        user.setActiveAchievementId(null);
        userRepository.save(user);
    }

    // -------------------------------------------------------------------------
    // EVENT LISTENER — Triggered after each transaction save
    // @Async runs evaluation in a separate thread so it never blocks
    // the original transaction operation
    // -------------------------------------------------------------------------

    @Async
    @EventListener
    @Transactional
    public void onTransactionSaved(TransactionSavedEvent event) {
        try {
            evaluateAchievements(event.getUserId());
        } catch (Exception e) {
            // La evaluación de logros nunca debe interrumpir el flujo principal.
            // En producción este catch debería loggear con un logger adecuado.
            System.err.println("[AchievementService] Error evaluando logros para userId="
                + event.getUserId() + ": " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // CORE — Evalúa todos los logros para un usuario dado
    // -------------------------------------------------------------------------

    @Transactional
    public void evaluateAchievements(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userId));

        LocalDate today = LocalDate.now();
        int currentYear  = today.getYear();
        int currentMonth = today.getMonthValue();

        evaluateGastoSinFreno(user, currentYear, currentMonth);
        evaluateModoSupervivencia(user, currentYear, currentMonth);
        evaluateTacanitoProfesional(user, currentYear, currentMonth);
        evaluateModoAdultoPremium(user, currentYear, currentMonth);
    }

    // -------------------------------------------------------------------------
    // EVALUACIÓN — Gasto sin freno
    // Condición: gastos >= 130% de ingresos en el mes actual
    // -------------------------------------------------------------------------

    private void evaluateGastoSinFreno(User user, int year, int month) {
        String code = CODE_UNCONTROLLED_SPENDING;
        if (alreadyUnlocked(user.getId(), code)) return;

        MonthlyBalanceDto balance = balanceService.getMonthlyBalance(user.getId(), year, month);
        if (isZeroOrNull(balance.getIncome())) return;

        // expense >= income * 1.30
        BigDecimal threshold = balance.getIncome()
                .multiply(new BigDecimal("1.30"));

        if (balance.getExpense().compareTo(threshold) >= 0) {
            grantAchievement(user, code);
        }
    }

    // -------------------------------------------------------------------------
    // EVALUACIÓN — Modo supervivencia
    // Condición: gastos >= 90% de ingresos en 2 meses consecutivos
    // -------------------------------------------------------------------------

    private void evaluateModoSupervivencia(User user, int year, int month) {
        String code = CODE_SURVIVAL_MODE;
        if (alreadyUnlocked(user.getId(), code)) return;

        int consecutiveMonths = 0;
        for (int i = 0; i < 2; i++) {
            MonthYear my = monthsAgo(year, month, i);
            MonthlyBalanceDto balance = balanceService
                    .getMonthlyBalance(user.getId(), my.year(), my.month());

            if (isZeroOrNull(balance.getIncome())) break;

            BigDecimal threshold = balance.getIncome()
                    .multiply(new BigDecimal("0.90"));

            if (balance.getExpense().compareTo(threshold) >= 0) {
                consecutiveMonths++;
            } else {
                break;
            }
        }

        if (consecutiveMonths >= 2) {
            grantAchievement(user, code);
        }
    }

    // -------------------------------------------------------------------------
    // EVALUACIÓN — Tacañito profesional
    // Condición: ahorro >= 20% de ingresos en 2 meses consecutivos
    // ahorro = income - expense; ahorro/income >= 0.20
    // -------------------------------------------------------------------------

    private void evaluateTacanitoProfesional(User user, int year, int month) {
        String code = CODE_PRO_SAVER;
        if (alreadyUnlocked(user.getId(), code)) return;

        int consecutiveMonths = 0;
        for (int i = 0; i < 2; i++) {
            MonthYear my = monthsAgo(year, month, i);
            MonthlyBalanceDto balance = balanceService
                    .getMonthlyBalance(user.getId(), my.year(), my.month());

            if (isZeroOrNull(balance.getIncome())) break;

            BigDecimal savings = balance.getIncome().subtract(balance.getExpense());
            BigDecimal savingsRate = savings.divide(balance.getIncome(), 4, RoundingMode.HALF_UP);

            if (savingsRate.compareTo(new BigDecimal("0.20")) >= 0) {
                consecutiveMonths++;
            } else {
                break;
            }
        }

        if (consecutiveMonths >= 2) {
            grantAchievement(user, code);
        }
    }

    // -------------------------------------------------------------------------
    // EVALUACIÓN — Modo adulto premium
    // Condición: balance positivo (income > expense) en 3 meses consecutivos
    // -------------------------------------------------------------------------

    private void evaluateModoAdultoPremium(User user, int year, int month) {
        String code = CODE_PREMIUM_ADULT;
        if (alreadyUnlocked(user.getId(), code)) return;

        int consecutiveMonths = 0;
        for (int i = 0; i < 3; i++) {
            MonthYear my = monthsAgo(year, month, i);
            MonthlyBalanceDto balance = balanceService
                    .getMonthlyBalance(user.getId(), my.year(), my.month());

            if (isZeroOrNull(balance.getIncome())) break;

            // Balance positivo: ingresos estrictamente mayores que gastos
            if (balance.getIncome().compareTo(balance.getExpense()) > 0) {
                consecutiveMonths++;
            } else {
                break;
            }
        }

        if (consecutiveMonths >= 3) {
            grantAchievement(user, code);
        }
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    /**
     * Otorga un logro al usuario si no lo tiene ya.
     * Usa el código del catálogo para buscar la entidad Achievement.
     */
    private void grantAchievement(User user, String code) {
        Achievement achievement = achievementRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException(
                    "Logro no encontrado en catálogo: " + code));

        // Segunda línea de defensa contra duplicados (primera es la constraint UNIQUE en BD)
        if (userAchievementRepository.existsByUserIdAndAchievementId(
                user.getId(), achievement.getId())) {
            return;
        }

        UserAchievement ua = new UserAchievement(user, achievement, LocalDateTime.now());
        userAchievementRepository.save(ua);
    }

    /**
     * Construye la lista completa de logros (obtenidos + no obtenidos)
     * para el usuario dado.
     */
    private List<AchievementDto> buildAchievementList(User user) {
        List<Achievement> allAchievements = achievementRepository.findAll();
        List<UserAchievement> userAchievements = userAchievementRepository
                .findByUserId(user.getId());

        // Map achievementId → UserAchievement para lookup O(1)
        Map<Long, UserAchievement> unlockedMap = userAchievements.stream()
                .collect(Collectors.toMap(
                        ua -> ua.getAchievement().getId(),
                        ua -> ua
                ));

        List<AchievementDto> result = new ArrayList<>();
        for (Achievement achievement : allAchievements) {
            AchievementDto dto = new AchievementDto();
            dto.setId(achievement.getId());
            dto.setCode(achievement.getCode());
            dto.setName(achievement.getName());
            dto.setDescription(achievement.getDescription());

            UserAchievement ua = unlockedMap.get(achievement.getId());
            if (ua != null) {
                dto.setUnlocked(true);
                dto.setUnlockedAt(ua.getUnlockedAt());
            } else {
                dto.setUnlocked(false);
                dto.setUnlockedAt(null);
            }

            dto.setActive(achievement.getId().equals(user.getActiveAchievementId()));
            result.add(dto);
        }

        return result;
    }

    /** Verifica si el usuario ya tiene un logro por su código de catálogo */
    private boolean alreadyUnlocked(Long userId, String code) {
        return achievementRepository.findByCode(code)
                .map(a -> userAchievementRepository
                        .existsByUserIdAndAchievementId(userId, a.getId()))
                .orElse(false);
    }

    /** Retorna true si el valor es null o cero — protege las divisiones */
    private boolean isZeroOrNull(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) == 0;
    }

    /** Calcula el año y mes correspondiente a N meses atrás */
    private MonthYear monthsAgo(int year, int month, int monthsBack) {
        LocalDate date = LocalDate.of(year, month, 1).minusMonths(monthsBack);
        return new MonthYear(date.getYear(), date.getMonthValue());
    }

    /** Resuelve el usuario autenticado desde el SecurityContext */
    private User resolveCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("Usuario no autenticado");
        }
        return userLookup.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    /** Record interno para transportar año+mes sin crear una clase extra */
    private record MonthYear(int year, int month) {}
}
