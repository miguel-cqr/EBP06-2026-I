package com.tuapp.finanzas.recommendation.service;

import com.tuapp.finanzas.recommendation.dto.RecommendationDto;
import com.tuapp.finanzas.transaction.entity.Transaction;
import com.tuapp.finanzas.transaction.repository.TransactionRepository;
import com.tuapp.finanzas.user.entity.User;
import com.tuapp.finanzas.user.service.UserLookup;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class RecommendationService {

    private final TransactionRepository transactionRepository;
    private final UserLookup userLookup;

    public RecommendationService(TransactionRepository transactionRepository, UserLookup userLookup) {
        this.transactionRepository = transactionRepository;
        this.userLookup = userLookup;
    }

    @Transactional(readOnly = true)
    public List<RecommendationDto> getRecommendations() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userLookup.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        LocalDate now = LocalDate.now();
        OffsetDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfMonth = now.plusMonths(1).withDayOfMonth(1).atStartOfDay().atOffset(ZoneOffset.UTC).minusSeconds(1);

        List<Transaction> expenses = transactionRepository.findByUserIdAndTypeAndDateBetweenOrderByDateDesc(
                user.getId(), Transaction.TransactionType.EXPENSE, startOfMonth, endOfMonth);

        if (expenses.isEmpty() || expenses.size() < 3) {
            return List.of(new RecommendationDto("General", "Necesitas registrar más movimientos (al menos 3 gastos este mes) para recibir sugerencias personalizadas."));
        }

        Map<String, BigDecimal> expensesByCategory = new HashMap<>();
        for (Transaction t : expenses) {
            if (t.getCategory() != null) {
                String catName = t.getCategory().getName();
                BigDecimal amount = t.getAmount();
                expensesByCategory.put(catName, expensesByCategory.getOrDefault(catName, BigDecimal.ZERO).add(amount));
            }
        }

        var topCategoryEntry = expensesByCategory.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        List<RecommendationDto> recommendations = new ArrayList<>();

        if (topCategoryEntry != null) {
            String topCategory = topCategoryEntry.getKey();
            String message = generateMessageForCategory(topCategory);
            recommendations.add(new RecommendationDto(topCategory, message));
        }

        return recommendations;
    }

    private String generateMessageForCategory(String category) {
        return switch (category.toLowerCase()) {
            case "alimentacion", "alimentación" -> "Tus gastos en Alimentación son los más altos este mes. Un buen consejo es planificar tus comidas de la semana y hacer una sola compra grande; esto suele ser más económico que comprar a diario o comer fuera.";
            case "transporte" -> "El Transporte representa tu mayor gasto actual. Si es posible, evalúa usar transporte público, compartir viajes con compañeros o usar bicicleta algunos días para reducir este costo.";
            case "vivienda" -> "La Vivienda es tu gasto principal. Revisa si puedes ahorrar un poco apagando las luces que no usas o desconectando aparatos electrónicos.";
            case "entretenimiento" -> "Has gastado más en Entretenimiento este mes. ¡Está muy bien divertirse! Pero intenta buscar también opciones gratuitas o de bajo costo para equilibrar tu bolsillo.";
            case "salud" -> "La Salud es tu categoría de mayor gasto. La salud es lo primero, así que asegúrate de mantener tus chequeos al día, pero revisa si cuentas con algún seguro o beneficio que pueda ayudarte con estos costos.";
            case "educacion", "educación" -> "Tu mayor gasto está en Educación. Esta es una excelente inversión a futuro. Revisa si existen becas, descuentos estudiantiles o materiales gratuitos que te ayuden a minimizar costos adicionales.";
            case "compras" -> "Las Compras lideran tus gastos. Antes de comprar algo nuevo, pregúntate si realmente lo necesitas o si puede esperar un poco. ¡La regla de esperar 24 horas ayuda mucho a evitar compras impulsivas!";
            default -> "Tus gastos en " + category + " son los más altos. Te sugerimos revisar el detalle de estos pagos y preguntarte si puedes reducir o aplazar alguno para el futuro.";
        };
    }
}