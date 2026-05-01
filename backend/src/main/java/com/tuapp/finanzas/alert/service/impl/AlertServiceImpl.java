@Service
public class AlertServiceImpl implements AlertService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final AlertRepository alertRepository;

    public AlertServiceImpl(
            BudgetRepository budgetRepository,
            TransactionRepository transactionRepository,
            AlertRepository alertRepository
    ) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
        this.alertRepository = alertRepository;
    }

    @Override
    public void evaluateBudget(Long userId, Long categoryId, int year, int month) {

        Optional<Budget> optionalBudget =
                budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                        userId, categoryId, month, year
                );

        if (optionalBudget.isEmpty()) return;

        Budget budget = optionalBudget.get();

        BigDecimal spent = transactionRepository
                .sumExpensesByCategory(userId, categoryId, year, month);

        BigDecimal limit = budget.getLimitAmount();

        if (limit.compareTo(BigDecimal.ZERO) == 0) return;

        BigDecimal percentage = spent
                .multiply(BigDecimal.valueOf(100))
                .divide(limit, 2, RoundingMode.HALF_UP);

        if (percentage.compareTo(BigDecimal.valueOf(80)) >= 0 &&
                percentage.compareTo(BigDecimal.valueOf(100)) < 0) {

            createAlert(userId, categoryId, percentage, "WARNING");
        }

        if (percentage.compareTo(BigDecimal.valueOf(100)) >= 0) {
            createAlert(userId, categoryId, percentage, "EXCEEDED");
        }
    }

    private void createAlert(Long userId, Long categoryId, BigDecimal percentage, String type) {
        Alert alert = new Alert();
        alert.setMessage("Budget exceeded for category");
        alert.setPercentage(percentage);
        alert.setType(type);
        alert.setCreatedAt(LocalDateTime.now());


        alertRepository.save(alert);
    }
}