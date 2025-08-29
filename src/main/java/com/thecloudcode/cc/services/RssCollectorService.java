package com.thecloudcode.cc.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.thecloudcode.cc.models.NewsArticle;
import com.thecloudcode.cc.repository.NewsArticleRepository;

import jakarta.transaction.Transactional;
import java.net.URL;



@Service
@Transactional
public class RssCollectorService {


    @Autowired
    private NewsArticleRepository articleRepository;

    public List<NewsArticle> fetchArticlesFromRSS(String rssUrl, String sourceName) {
        List<NewsArticle> newArticles = new ArrayList<>();
        
        try {
            System.out.println("Fetching from " + sourceName + ": " + rssUrl);
            
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(new URL(rssUrl)));
            
            System.out.println("Found " + feed.getEntries().size() + " entries in RSS feed");
            
            for (SyndEntry entry : feed.getEntries()) {
                // Skip if URL already exists (avoid duplicates)
                if (articleRepository.existsByUrl(entry.getLink())) {
                    continue;
                }
                
                NewsArticle article = new NewsArticle();
                article.setTitle(entry.getTitle());
                
                // Handle description safely
                if (entry.getDescription() != null) {
                    String desc = entry.getDescription().getValue();
                    // Truncate if too long
                    if (desc.length() > 2000) {
                        desc = desc.substring(0, 1997) + "...";
                    }
                    article.setDescription(desc);
                } else {
                    article.setDescription("");
                }
                
                article.setUrl(entry.getLink());
                article.setSource(sourceName);
                
                // Handle date conversion
                if (entry.getPublishedDate() != null) {
                    article.setPublishedDate(
                        LocalDateTime.ofInstant(entry.getPublishedDate().toInstant(), ZoneId.systemDefault())
                    );
                } else {
                    article.setPublishedDate(LocalDateTime.now());
                }
                
                newArticles.add(article);
            }
            
            // Save all new articles
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

      public int collectFromAllSources() {
        int totalCollected = 0;
        
        // TechCrunch
        List<NewsArticle> techcrunchArticles = fetchArticlesFromRSS(
            "https://techcrunch.com/feed/", "TechCrunch"
        );
        totalCollected += techcrunchArticles.size();
        
        // The Verge
        List<NewsArticle> vergeArticles = fetchArticlesFromRSS(
            "https://www.theverge.com/rss/index.xml", "The Verge"
        );
        totalCollected += vergeArticles.size();
        
        return totalCollected;
    }
}
