package com.tuapp.finanzas.achievement.transaction.event;

/**
 * Evento publicado cada vez que se persiste una transacción
 * (ingreso o gasto). AchievementService lo escucha para evaluar
 * logros sin crear acoplamiento directo entre módulos.
 */
public class TransactionSavedEvent {

    private final Long userId;

    public TransactionSavedEvent(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}
