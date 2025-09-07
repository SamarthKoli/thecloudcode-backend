package com.thecloudcode.cc.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thecloudcode.cc.dto.ProcessedArticle;
import com.thecloudcode.cc.models.NewsArticle;
import com.thecloudcode.cc.repository.NewsArticleRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentProcessingService {

    @Autowired
    private OpenAiServiceClient openAiService;
    
    @Autowired
    private NewsArticleRepository articleRepository;

    public List<ProcessedArticle> processRecentArticles() {
        System.out.println("Starting OpenAI processing of recent articles...");
        
        // Get articles from last 24 hours
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        List<NewsArticle> recentArticles = articleRepository.findRecentArticles(yesterday);
        
        System.out.println("Found " + recentArticles.size() + " articles from last 24 hours");
        
        if (recentArticles.isEmpty()) {
            System.out.println("No recent articles found. Using all articles for processing.");
            recentArticles = articleRepository.findAll();
        }
        
        List<ProcessedArticle> processedArticles = new ArrayList<>();
        
        // Process maximum 10 articles to manage OpenAI API costs
        List<NewsArticle> articlesToProcess = recentArticles.stream()
            .limit(10)
            .collect(Collectors.toList());
        
        for (int i = 0; i < articlesToProcess.size(); i++) {
            NewsArticle article = articlesToProcess.get(i);
            
            try {
                System.out.println("Processing article " + (i+1) + "/" + articlesToProcess.size() + ": " + article.getTitle());
                
                ProcessedArticle processed = new ProcessedArticle(article);
                
                // OpenAI Summarization
                String summary = openAiService.summarizeArticle(
                    article.getTitle(), 
                    article.getDescription() != null ? article.getDescription() : ""
                );
                processed.setSummary(summary);
                System.out.println("✓ Summary: " + summary.substring(0, Math.min(100, summary.length())) + "...");
                
                // OpenAI Relevance Scoring
                int relevanceScore = openAiService.scoreArticleRelevance(
                    article.getTitle(), 
                    article.getDescription() != null ? article.getDescription() : ""
                );
                processed.setRelevanceScore(relevanceScore);
                System.out.println("✓ Relevance Score: " + relevanceScore + "/10");
                
                // OpenAI Categorization
                String category = openAiService.categorizeArticle(
                    article.getTitle(), 
                    article.getDescription() != null ? article.getDescription() : ""
                );
                processed.setCategory(category);
                System.out.println("✓ Category: " + category);
                
                processedArticles.add(processed);
                
                // Add delay to avoid rate limiting
                if (i < articlesToProcess.size() - 1) {
                    System.out.println("Waiting 1 second before next article...");
                    Thread.sleep(1000);
                }
                
            } catch (Exception e) {
                System.err.println("Error processing article: " + article.getTitle() + " - " + e.getMessage());
                
                // Create a fallback processed article
                ProcessedArticle fallback = new ProcessedArticle(article);
                fallback.setSummary("Unable to generate AI summary for this article.");
                fallback.setRelevanceScore(5);
                fallback.setCategory("General Tech");
                processedArticles.add(fallback);
            }
        }
        
        System.out.println("OpenAI processing complete. Processed " + processedArticles.size() + " articles");
        return processedArticles;
    }
    
    public List<ProcessedArticle> selectTopArticles(List<ProcessedArticle> processedArticles) {
        System.out.println("Selecting top articles from " + processedArticles.size() + " processed articles");
        
        // Select articles with relevance score 6 and above, limit to top 5
        List<ProcessedArticle> topArticles = processedArticles.stream()
            .filter(article -> article.getRelevanceScore() >= 6) // Quality threshold
            .sorted((a, b) -> Integer.compare(b.getRelevanceScore(), a.getRelevanceScore())) // Sort by score desc
            .limit(5) // Top 5 articles
            .collect(Collectors.toList());
        
        System.out.println("Selected " + topArticles.size() + " high-scoring articles");
        
        // If we don't have enough high-scoring articles, fill with next best
        if (topArticles.size() < 3) {
            List<ProcessedArticle> additionalArticles = processedArticles.stream()
                .filter(article -> article.getRelevanceScore() < 6)
                .sorted((a, b) -> Integer.compare(b.getRelevanceScore(), a.getRelevanceScore()))
                .limit(3 - topArticles.size())
                .collect(Collectors.toList());
            
            topArticles.addAll(additionalArticles);
            System.out.println("Added " + additionalArticles.size() + " additional articles to reach minimum of 3");
        }
        
        return topArticles;
    }
    
    public String generateNewsletterPreview(List<ProcessedArticle> topArticles) {
        if (topArticles.isEmpty()) {
            return "No articles available for newsletter.";
        }
        
        StringBuilder preview = new StringBuilder();
        
        // Generate subject line using OpenAI
        List<String> titles = topArticles.stream()
            .map(article -> article.getOriginalArticle().getTitle())
            .collect(Collectors.toList());
        
        String subject = openAiService.generateNewsletterSubject(titles);
        preview.append("Subject: ").append(subject).append("\n\n");
        
        preview.append("🚀 TODAY'S TOP TECH STORIES\n\n");
        
        // Group articles by category
        topArticles.stream()
            .collect(Collectors.groupingBy(ProcessedArticle::getCategory))
            .forEach((category, articles) -> {
                preview.append("📂 ").append(category.toUpperCase()).append("\n");
                articles.forEach(article -> {
                    preview.append("• ").append(article.getOriginalArticle().getTitle()).append("\n");
                    preview.append("  ").append(article.getSummary()).append("\n");
                    preview.append("  Read more: ").append(article.getOriginalArticle().getUrl()).append("\n\n");
                });
            });
        
        preview.append("📧 Newsletter powered by AI-curated content");
        
        return preview.toString();
    }
}