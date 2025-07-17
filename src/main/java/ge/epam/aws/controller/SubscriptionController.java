package ge.epam.aws.controller;

import ge.epam.aws.service.MessagingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final MessagingService awsMessagingService;

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(@RequestParam String email) {
        String subscriptionArn = awsMessagingService.subscribeToNewsletter(email);
        if (subscriptionArn != null) {
            log.info("Successfully initiated subscription for {}. " +
                    "Please check your email inbox and spam folder to confirm your subscription.", email);
            return ResponseEntity.ok("Successfully initiated subscription for " + email +
                    ". Please check your email inbox and spam folder to confirm your subscription.");
        } else {
            return ResponseEntity.status(500).body("Failed to subscribe to newsletter for " + email);
        }
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribe(@RequestParam String subscriptionArn) {
        awsMessagingService.unsubscribeFromNewsletter(subscriptionArn);
        return ResponseEntity.ok("Unsubscribe request sent for subscription: " + subscriptionArn +
                ". You may receive a final confirmation email.");
    }
}
