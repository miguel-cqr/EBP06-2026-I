package com.tuapp.finanzas.achievement.init;

import com.tuapp.finanzas.achievement.entity.Achievement;
import com.tuapp.finanzas.achievement.repository.AchievementRepository;
import com.tuapp.finanzas.achievement.service.AchievementService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Inserta los 4 logros del catálogo al arrancar la aplicación.
 * Es idempotente: verifica con findByCode antes de insertar,
 * por lo que puede ejecutarse en cada arranque sin crear duplicados.
 *
 * Implementa ApplicationRunner (no CommandLineRunner) para garantizar
 * que el contexto de Spring — incluyendo Hibernate y el schema —
 * está completamente inicializado antes de ejecutarse.
 */
@Component
public class AchievementDataInitializer implements ApplicationRunner {

    private final AchievementRepository achievementRepository;

    public AchievementDataInitializer(AchievementRepository achievementRepository) {
        this.achievementRepository = achievementRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        insertIfAbsent(
            AchievementService.CODE_PREMIUM_ADULT,
            "Modo adulto premium",
            "Lograste el equilibrio entre responsabilidad, control y ahorro. " +
            "Tu yo del pasado está orgulloso."
        );
        insertIfAbsent(
            AchievementService.CODE_PRO_SAVER,
            "Tacañito profesional",
            "Cada peso cuenta contigo. Encontraste la forma de ahorrar " +
            "hasta en los pequeños gastos."
        );
        insertIfAbsent(
            AchievementService.CODE_SURVIVAL_MODE,
            "Modo supervivencia",
            "Tus finanzas están bajo presión. Es momento de recuperar " +
            "estabilidad y crear un colchón financiero."
        );
        insertIfAbsent(
            AchievementService.CODE_UNCONTROLLED_SPENDING,
            "Gasto sin freno",
            "Los gastos impulsivos tomaron el volante. " +
            "Tu billetera pide una pausa urgente."
        );
    }

    private void insertIfAbsent(String code, String name, String description) {
        if (achievementRepository.findByCode(code).isEmpty()) {
            achievementRepository.save(new Achievement(code, name, description));
        }
    }
}
