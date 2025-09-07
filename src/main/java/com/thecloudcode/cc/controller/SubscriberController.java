package com.thecloudcode.cc.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thecloudcode.cc.models.Subscriber;
import com.thecloudcode.cc.repository.SubscriberRepository;
import com.thecloudcode.cc.services.EmailService;
import com.thecloudcode.cc.services.SubscriberService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/subscribers")
@CrossOrigin(origins = "http://localhost:3000")
public class SubscriberController {

    @Autowired
    private SubscriberService subscriberService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, Object>> subscribe(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = payload.get("email");
            if (email == null || email.trim().isEmpty() || !isValidEmail(email)) {
                response.put("success", false);
                response.put("message", "Please provide a valid email address.");
                return ResponseEntity.badRequest().body(response);
            }

            String normalizedEmail = email.trim().toLowerCase();

            // Check if already subscribed
            if (subscriberRepository.existsByEmail(normalizedEmail)) {
                response.put("success", false);
                response.put("message", "This email is already subscribed to our newsletter.");
                return ResponseEntity.badRequest().body(response);
            }

            Subscriber subscriber = subscriberService.subscribe(normalizedEmail);

            // Send welcome/confirmation email
            boolean emailSent = emailService.sendSubscriptionConfirmation(normalizedEmail);

            response.put("success", true);
            response.put("subscriber", subscriber);
            response.put("message", emailSent
                ? "Successfully subscribed! Please check your email for confirmation. You'll receive daily tech updates."
                : "Successfully subscribed! (Confirmation email could not be sent.)");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Subscription failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

      @PostMapping("/unsubscribe")
    public ResponseEntity<Map<String, Object>> unsubscribe(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = payload.get("email");
            if (email == null || email.trim().isEmpty() || !isValidEmail(email)) {
                response.put("success", false);
                response.put("message", "Please provide a valid email address.");
                return ResponseEntity.badRequest().body(response);
            }

            String normalizedEmail = email.trim().toLowerCase();

            Subscriber subscriber = subscriberService.findByEmail(normalizedEmail);
            if (subscriber == null) {
                response.put("success", false);
                response.put("message", "Email is not subscribed or already unsubscribed.");
                return ResponseEntity.badRequest().body(response);
            }

            subscriberService.unSubscribe(normalizedEmail);

            boolean emailSent = emailService.sendUnsubscribeConfirmation(normalizedEmail);

            response.put("success", true);
            response.put("message", emailSent 
                ? "You have been unsubscribed! Confirmation sent to your email."
                : "You have been unsubscribed! (Confirmation email could not be sent.)");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Unsubscribe failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getSubscriberCount() {
        Map<String, Object> response = new HashMap<>();
        long count = subscriberService.getActiveSubscriberCount();
        response.put("success", true);
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("cloudcode API is working with MySQL!");
    }

    // Email validation utility
    private boolean isValidEmail(String email) {
        // Basic regex validation, you can switch to Apache Commons EmailValidator for production
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}
