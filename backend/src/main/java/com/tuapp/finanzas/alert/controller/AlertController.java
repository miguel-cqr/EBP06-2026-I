@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertRepository alertRepository;

    public AlertController(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @GetMapping
    public List<Alert> getAlerts(Authentication auth) {
        Long userId = ((User) auth.getPrincipal()).getId();
        return alertRepository.findByUserId(userId);
    }
}