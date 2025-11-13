package com.thecloudcode.cc.services;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.thecloudcode.cc.models.NewsArticle;
import com.thecloudcode.cc.repository.NewsArticleRepository;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
@Service
@Transactional
public class RSSCollectorService {

    @Autowired
    private NewsArticleRepository articleRepository;

    public List<NewsArticle> fetchArticlesFromRSS(String rssUrl, String sourceName) {
        List<NewsArticle> newArticles = new ArrayList<>();
        
        try {
            System.out.println("Fetching from " + sourceName + ": " + rssUrl);
            
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(new URL(rssUrl)));
            
            for (SyndEntry entry : feed.getEntries()) {
                if (articleRepository.existsByUrl(entry.getLink())) {
                    continue; // Skip duplicates
                }
                
                NewsArticle article = new NewsArticle();
                article.setTitle(entry.getTitle());
                article.setUrl(entry.getLink());
                article.setSource(sourceName);
                
                // Handle description
                if (entry.getDescription() != null) {
                    String desc = entry.getDescription().getValue();
                    if (desc.length() > 2000) {
                        desc = desc.substring(0, 1997) + "...";
                    }
                    article.setDescription(desc);
                } else {
                    article.setDescription("");
                }
                
                // Extract image URL using multiple methods
                String imageUrl = extractImageUrl(entry);
                article.setImageUrl(imageUrl);
                
                // Handle date
                if (entry.getPublishedDate() != null) {
                    article.setPublishedDate(
                        LocalDateTime.ofInstant(entry.getPublishedDate().toInstant(), ZoneId.systemDefault())
                    );
                } else {
                    article.setPublishedDate(LocalDateTime.now());
                }
                
                newArticles.add(article);
            }
            
            if (!newArticles.isEmpty()) {
                articleRepository.saveAll(newArticles);
                System.out.println("Saved " + newArticles.size() + " new articles from " + sourceName);
            }
            
        } catch (Exception e) {
            System.err.println("Error fetching RSS from " + sourceName + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return newArticles;
    }
    
   
    
   private String extractImageUrl(SyndEntry entry) {
    String imageUrl = null;
    
    // Method 1: Check for enclosures (media attachments)
    List<SyndEnclosure> enclosures = entry.getEnclosures();
    if (enclosures != null && !enclosures.isEmpty()) {
        for (SyndEnclosure enclosure : enclosures) {
            if (enclosure.getType() != null && enclosure.getType().startsWith("image/")) {
                imageUrl = enclosure.getUrl();
                System.out.println("✓ Found image in enclosure: " + imageUrl);
                break;
            }
        }
    }
    
    // Method 2: Parse image from description HTML
    if (imageUrl == null && entry.getDescription() != null) {
        imageUrl = extractImageFromDescription(entry.getDescription().getValue());
        if (imageUrl != null) {
            System.out.println("✓ Found image in description: " + imageUrl);
        }
    }
    
    // Method 3: Fetch from article page (og:image)
    if (imageUrl == null) {
        imageUrl = getImageFromArticlePage(entry.getLink());
        if (imageUrl != null) {
            System.out.println("✓ Found og:image from page: " + imageUrl);
        }
    }
    
    if (imageUrl == null) {
        System.out.println("✗ No image found for: " + entry.getTitle());
    }
    
    return imageUrl;
}

private String extractImageFromDescription(String description) {
    try {
        // Parse description HTML and find first image
        Document doc = Jsoup.parse(description);
        Element img = doc.select("img").first();
        if (img != null) {
            String src = img.attr("src");
            // Make sure it's a full URL
            if (src.startsWith("http")) {
                return src;
            }
        }
    } catch (Exception e) {
        System.err.println("Error parsing description for image: " + e.getMessage());
    }
    return null;
}

private String getImageFromArticlePage(String articleUrl) {
    try {
        System.out.println("Fetching og:image from: " + articleUrl);
        Document doc = Jsoup.connect(articleUrl)
            .timeout(5000)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
            .get();
        
        // Try og:image first
        Element ogImage = doc.select("meta[property=og:image]").first();
        if (ogImage != null) {
            String content = ogImage.attr("content");
            if (content.startsWith("http")) {
                return content;
            }
        }
        
        // Try twitter:image
        Element twitterImage = doc.select("meta[name=twitter:image]").first();
        if (twitterImage != null) {
            String content = twitterImage.attr("content");
            if (content.startsWith("http")) {
                return content;
            }
        }
        
    } catch (Exception e) {
        System.err.println("Error fetching image from article page: " + e.getMessage());
    }
    return null;
}
    
    public int collectFromAllSources() {
        int totalCollected = 0;
        
        List<NewsArticle> techcrunchArticles = fetchArticlesFromRSS(
            "https://techcrunch.com/feed/", "TechCrunch"
        );
        totalCollected += techcrunchArticles.size();
        
        List<NewsArticle> vergeArticles = fetchArticlesFromRSS(
            "https://www.theverge.com/rss/index.xml", "The Verge"
        );

        totalCollected += vergeArticles.size();

        List<NewsArticle> wiredArticles=fetchArticlesFromRSS(
            "https://www.wired.com/feed/tag/ai/latest/rss", "Wired");

            totalCollected+=wiredArticles.size();

        List<NewsArticle>microsoftArticles=fetchArticlesFromRSS(
            "https://devblogs.microsoft.com/java/feed/", "Microsoft Dev Blogs");
            totalCollected+=microsoftArticles.size();
        
        return totalCollected;
    }
}