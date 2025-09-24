package com.thecloudcode.cc.controller;

import com.thecloudcode.cc.dto.ProcessedArticle;
import com.thecloudcode.cc.models.NewsArticle;
import com.thecloudcode.cc.repository.NewsArticleRepository;
import com.thecloudcode.cc.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = "http://localhost:3000")

public class NewsController {

    @Autowired
    private RSSCollectorService rssCollectorService;
    
    @Autowired
    private NewsArticleRepository articleRepository;

    @Autowired
    private ContentProcessingService contentProcessingService;

    @Autowired
    private NewsletterSchedulerService schedulerService;
    
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

    @GetMapping("/featured")
public ResponseEntity<Map<String, Object>> getFeaturedArticles() {
    Map<String, Object> response = new HashMap<>();
    
    try {
        // Get top 3 recent articles with images
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        List<NewsArticle> recentArticles = articleRepository.findRecentArticles(oneDayAgo);
        
        // Filter articles that have images and take top 3
        List<NewsArticle> featuredArticles = recentArticles.stream()
            .filter(article -> article.getImageUrl() != null && !article.getImageUrl().isEmpty())
            .limit(3)
            .collect(Collectors.toList());
        
        // If less than 3 with images, fill with articles without images
        if (featuredArticles.size() < 3) {
            List<NewsArticle> additionalArticles = recentArticles.stream()
                .filter(article -> !featuredArticles.contains(article))
                .limit(3 - featuredArticles.size())
                .collect(Collectors.toList());
            featuredArticles.addAll(additionalArticles);
        }
        
        response.put("success", true);
        response.put("articles", featuredArticles);
        
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        response.put("success", false);
        response.put("message", "Error fetching featured articles: " + e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}

      @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processArticles() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("Starting OpenAI content processing...");
            
            List<ProcessedArticle> processedArticles = contentProcessingService.processRecentArticles();
            List<ProcessedArticle> topArticles = contentProcessingService.selectTopArticles(processedArticles);
            
            response.put("success", true);
            response.put("message", "OpenAI processed " + processedArticles.size() + " articles, selected " + topArticles.size() + " for newsletter");
            response.put("processedCount", processedArticles.size());
            response.put("selectedCount", topArticles.size());
            response.put("topArticles", topArticles);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error processing articles with OpenAI: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/generate-newsletter")
    public ResponseEntity<Map<String, Object>> generateNewsletter() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("Generating newsletter with OpenAI...");
            
            // Process recent articles with OpenAI
            List<ProcessedArticle> processedArticles = contentProcessingService.processRecentArticles();
            List<ProcessedArticle> topArticles = contentProcessingService.selectTopArticles(processedArticles);
            
            if (topArticles.isEmpty()) {
                response.put("success", false);
                response.put("message", "No articles available for newsletter generation");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Generate newsletter preview
            String newsletterPreview = contentProcessingService.generateNewsletterPreview(topArticles);
            
            response.put("success", true);
            response.put("message", "Newsletter generated successfully with " + topArticles.size() + " articles");
            response.put("newsletter", newsletterPreview);
            response.put("topArticles", topArticles);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error generating newsletter: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(response);
        }
    }


    @PostMapping("/send-daily")
    public ResponseEntity<Map<String, Object>> sendDailyNewsletterNow() {
        Map<String, Object> response = new HashMap<>();
        try {
            schedulerService.sendDailyNewsletter();
            response.put("success", true);
            response.put("message", "Daily newsletter sent successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to send daily newsletter: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/send-weekly")
    public ResponseEntity<Map<String, Object>> sendWeeklyNewsletterNow() {
        Map<String, Object> response = new HashMap<>();
        try {
            schedulerService.sendWeeklyNewsletter();
            response.put("success", true);
            response.put("message", "Weekly newsletter sent successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to send weekly newsletter: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("News API is working!");
    }


}