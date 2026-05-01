@RestController
@RequestMapping("/api/balance")
public class BalanceController {

    private final BalanceService balanceService;
    private final UserLookup userLookup;

    public BalanceController(BalanceService balanceService, UserLookup userLookup) {
        this.balanceService = balanceService;
        this.userLookup = userLookup;
    }

    @GetMapping("/monthly")
    public MonthlyBalanceDto monthly(
            @RequestParam int year,
            @RequestParam int month
    ) {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        User user = userLookup.findByUsername(auth.getName())
                .orElseThrow();

        return balanceService.getMonthlyBalance(user.getId(), year, month);
    }

    @GetMapping("/yearly")
    public YearlyBalanceDto yearly(@RequestParam int year) {

        var auth = SecurityContextHolder.getContext().getAuthentication();

        User user = userLookup.findByUsername(auth.getName())
                .orElseThrow();

        return balanceService.getYearlyBalance(user.getId(), year);
    }
}