package com.thecloudcode.cc.dto;

import java.time.LocalDateTime;

import com.thecloudcode.cc.models.NewsArticle;

public class ProcessedArticle {
    private NewsArticle originalArticle;
    private String summary;
    private int relevanceScore;
    private String category;
    private LocalDateTime processedAt;
    
    // Constructors
    public ProcessedArticle() {}
    
    public ProcessedArticle(NewsArticle originalArticle) {
        this.originalArticle = originalArticle;
        this.processedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public NewsArticle getOriginalArticle() { 
        return originalArticle; 
    }
    public void setOriginalArticle(NewsArticle originalArticle) { 
        this.originalArticle = originalArticle; 
    }
    
    public String getSummary() { 
        return summary; 
    }
    public void setSummary(String summary) { 
        this.summary = summary; 
    }
    
    public int getRelevanceScore() { 
        return relevanceScore; 
    }
    public void setRelevanceScore(int relevanceScore) { 
        this.relevanceScore = relevanceScore; 
    }
    
    public String getCategory() { 
        return category; 
    }
    public void setCategory(String category) { 
        this.category = category; 
    }
    
    public LocalDateTime getProcessedAt() { 
        return processedAt; 
    }
    public void setProcessedAt(LocalDateTime processedAt) { 
        this.processedAt = processedAt; 
    }
}
