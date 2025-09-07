package com.thecloudcode.cc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class TauntController {

    private final List<String> taunts = Arrays.asList(
        "Nice try, hacker wannabe! 🕵️‍♂️",
        "Oops! Wrong door, cyber detective! 🚪",
        "Admin panel? More like admin NOPE! 😂",
        "Error 403: Curiosity killed the cat! 🐱",
        "You're not the admin we're looking for! ✋",
        "Hacking level: Beginner 🤪",
        "Plot twist: You're not Neo! 🕶️",
        "Access denied faster than your WiFi! 📡",
        "Better luck next time, script kiddie! 👶",
        "FBI is watching... just kidding! 👀"
    );

    private final List<String> suggestions = Arrays.asList(
        "Try learning ethical hacking instead! 📚",
        "Maybe start with HTML tutorials? 💻",
        "Subscribe to our newsletter for tech tips! 📧",
        "Consider a career in cybersecurity! 🔒",
        "Practice on HackTheBox, not here! 🎯",
        "Read some security blogs instead! 📖",
        "Try bug bounty programs legally! 🐛",
        "Learn Python first, then hack! 🐍"
    );

    @GetMapping("/taunt")
    public ResponseEntity<Map<String, Object>> getTaunt(HttpServletRequest request) {
        Random random = new Random();
        
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        // Log the unauthorized attempt
        System.out.println("🚨 Unauthorized admin access attempt:");
        System.out.println("IP: " + clientIp);
        System.out.println("User-Agent: " + userAgent);
        System.out.println("Time: " + timestamp);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", taunts.get(random.nextInt(taunts.size())));
        response.put("suggestion", suggestions.get(random.nextInt(suggestions.size())));
        response.put("timestamp", timestamp);
        response.put("yourIp", clientIp);
        response.put("statusCode", 403);
        response.put("incident", "UNAUTHORIZED_ADMIN_ACCESS");
        
        return ResponseEntity.status(403).body(response);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}