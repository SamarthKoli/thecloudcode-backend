package com.thecloudcode.cc.services;
import com.thecloudcode.cc.models.NewsArticle;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NewsletterTemplateService {

    /**
     * Generates HTML for daily digest newsletter
     */
    public String generateDailyDigest(List<NewsArticle> articles) {
        StringBuilder html = new StringBuilder();

        html.append("<html><body style=\"font-family: Arial, sans-serif;\">");
        html.append("<h1>TheCloudCode Daily Digest</h1>");
        html.append("<p>Here are today's top tech news articles curated for you:</p>");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        for (NewsArticle article : articles) {
            html.append("<h2><a href='").append(article.getUrl()).append("' target='_blank'>")
                .append(article.getTitle())
                .append("</a></h2>");
            html.append("<p>").append(truncate(article.getDescription(), 150)).append("</p>");
            html.append("<p><em>Source: ").append(article.getSource())
                .append(" | Published: ")
                .append(article.getPublishedDate().format(formatter))
                .append("</em></p><hr>");
        }

        html.append("<p>Thank you for subscribing to TheCloudCode Newsletter!</p>");
        html.append("</body></html>");

        return html.toString();
    }

    /**
     * Generates HTML for weekly roundup newsletter
     */
    public String generateWeeklyRoundup(List<NewsArticle> articles) {
        StringBuilder html = new StringBuilder();

        html.append("<html><body style=\"font-family: Arial, sans-serif;\">");
        html.append("<h1>TheCloudCode Weekly Roundup</h1>");
        html.append("<p>A curated summary of this week's most important tech news:</p>");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        for (NewsArticle article : articles) {
            html.append("<h2><a href='").append(article.getUrl()).append("' target='_blank'>")
                .append(article.getTitle())
                .append("</a></h2>");
            html.append("<p>").append(truncate(article.getDescription(), 200)).append("</p>");
            html.append("<p><em>Source: ").append(article.getSource())
                .append(" | Published: ")
                .append(article.getPublishedDate().format(formatter))
                .append("</em></p><hr>");
        }

        html.append("<p>Enjoy your week ahead! Stay tuned for next Monday’s newsletter.</p>");
        html.append("</body></html>");

        return html.toString();
    }

    /**
     * Helper to truncate text with ellipsis where needed
     */
    private String truncate(String text, int length) {
        if (text == null) {
            return "";
        }
        if (text.length() <= length) {
            return text;
        }
        return text.substring(0, length) + "...";
    }
}