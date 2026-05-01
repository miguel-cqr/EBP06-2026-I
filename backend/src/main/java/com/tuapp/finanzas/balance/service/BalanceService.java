public interface BalanceService {
    MonthlyBalanceDto getMonthlyBalance(Long userId, int year, int month);
    YearlyBalanceDto getYearlyBalance(Long userId, int year);
}