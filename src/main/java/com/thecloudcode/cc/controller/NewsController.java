package com.thecloudcode.cc.controller;

import org.springframework.web.bind.annotation.RestController;

import com.thecloudcode.cc.models.NewsArticle;
import com.thecloudcode.cc.repository.NewsArticleRepository;
import com.thecloudcode.cc.services.RssCollectorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = "http://localhost:3000")
public class NewsController {

    @Autowired
    private RssCollectorService rssCollectorService;
    
    @Autowired
    private NewsArticleRepository articleRepository;

    @PostMapping("/collect")
    public ResponseEntity<Map<String, Object>> collectNews() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int totalCollected = rssCollectorService.collectFromAllSources();
            
            response.put("success", true);
            response.put("message", "Successfully collected " + totalCollected + " new articles");
            response.put("articlesCollected", totalCollected);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error collecting articles: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentArticles() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get articles from last 24 hours
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            List<NewsArticle> recentArticles = articleRepository.findRecentArticles(yesterday);
            
            response.put("success", true);
            response.put("articles", recentArticles);
            response.put("count", recentArticles.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching articles: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllArticles() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<NewsArticle> allArticles = articleRepository.findAll();
            
            response.put("success", true);
            response.put("articles", allArticles);
            response.put("count", allArticles.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching articles: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("News API is working!");
    }
}