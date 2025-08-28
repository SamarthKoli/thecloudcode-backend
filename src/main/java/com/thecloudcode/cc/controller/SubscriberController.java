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
import com.thecloudcode.cc.services.SubscriberService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/subscribers")
@CrossOrigin(origins = "http://localhost:3000")
public class SubscriberController {

    @Autowired
    private SubscriberService subscriberService;

    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, Object>> subscribe(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = payload.get("email");
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            Subscriber subscriber = subscriberService.subscribe(email.trim().toLowerCase());
            response.put("success", true);
            response.put("message", "Successfully subscribed! You'll receive daily tech updates.");
            response.put("subscriber", subscriber);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
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

}
