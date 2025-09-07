package com.thecloudcode.cc.services;
import com.thecloudcode.cc.models.Subscriber;
import com.thecloudcode.cc.models.NewsArticle;
import com.thecloudcode.cc.repository.SubscriberRepository;
import com.thecloudcode.cc.repository.NewsArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NewsletterSchedulerService {

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private NewsArticleRepository newsArticleRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NewsletterTemplateService templateService;

    /**
     * Sends the daily newsletter every weekday at 8 AM IST
     */
    @Scheduled(cron = "0 0 8 * * MON-FRI", zone = "Asia/Kolkata")
    public void sendDailyNewsletter() {
        LocalDateTime from = LocalDateTime.now().minus(24, ChronoUnit.HOURS);
        List<NewsArticle> recentArticles = newsArticleRepository.findByPublishedDateAfterOrderByPublishedDateDesc(from);

        if (recentArticles.isEmpty()) {
            System.out.println("No recent articles found, skipping daily newsletter.");
            return;
        }

        String newsletterHtml = templateService.generateDailyDigest(recentArticles);
        String subject = "TheCloudCode Daily - " + LocalDateTime.now().toLocalDate();

        List<Subscriber> subscribers = subscriberRepository.findByActiveTrue();

        sendNewsletterToSubscribers(subscribers, subject, newsletterHtml);
    }

    /**
     * Sends the weekly newsletter every Monday at 9 AM IST
     */
    @Scheduled(cron = "0 0 9 * * MON", zone = "Asia/Kolkata")
    public void sendWeeklyNewsletter() {
        LocalDateTime from = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
        List<NewsArticle> weeklyArticles = newsArticleRepository.findByPublishedDateAfterOrderByPublishedDateDesc(from);

        if (weeklyArticles.isEmpty()) {
            System.out.println("No weekly articles found, skipping weekly newsletter.");
            return;
        }

        String newsletterHtml = templateService.generateWeeklyRoundup(weeklyArticles);
        String subject = "TheCloudCode Weekly Roundup - Week of " + LocalDateTime.now().toLocalDate();

        List<Subscriber> subscribers = subscriberRepository.findByActiveTrue();

        sendNewsletterToSubscribers(subscribers, subject, newsletterHtml);
    }

    private void sendNewsletterToSubscribers(List<Subscriber> subscribers, String subject, String htmlContent) {
        final int batchSize = 50;
        int sentCount = 0;

        for (int i = 0; i < subscribers.size(); i += batchSize) {
            int end = Math.min(i + batchSize, subscribers.size());
            List<Subscriber> batch = subscribers.subList(i, end);

            for (Subscriber subscriber : batch) {
                try {
                    // Optionally personalize unsubscribe/preferences URLs if template supports placeholders
                    String personalizedHtml = htmlContent
                            .replace("{{unsubscribeUrl}}", "https://thecloudcode.com/unsubscribe?email=" + subscriber.getEmail())
                            .replace("{{preferencesUrl}}", "https://thecloudcode.com/preferences?email=" + subscriber.getEmail());

                    emailService.sendHtmlEmail(subscriber.getEmail(), subject, personalizedHtml);
                    sentCount++;

                    Thread.sleep(100); // Small delay to avoid overwhelming SMTP server

                } catch (Exception e) {
                    System.err.println("Failed to send newsletter to " + subscriber.getEmail() + ": " + e.getMessage());
                }
            }

            try {
                Thread.sleep(2000); // Pause between batches
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.out.println("Batch sent: Subscribers " + i + " to " + (end - 1));
        }

        System.out.println("Total newsletters sent: " + sentCount);
    }
}