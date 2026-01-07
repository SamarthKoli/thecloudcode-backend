package com.thecloudcode.cc.controller;

import com.thecloudcode.cc.services.LeetcodeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/leetcase")
public class LeetCaseController {
    
    @Autowired
    private LeetcodeService leetCodeService;
    
    @GetMapping("/{username}")
    public ResponseEntity<Map<String, Object>> getLeetCaseData(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> leetCodeData = leetCodeService.fetchUserProfile(username);
            
            response.put("success", true);
            response.put("data", leetCodeData);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

