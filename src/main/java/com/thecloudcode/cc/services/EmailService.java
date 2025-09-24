package com.thecloudcode.cc.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${app.newsletter.sender.name:TheCloudCode Newsletter}")
    private String senderName;

    // Send simple text email
    public boolean sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            javaMailSender.send(message);
            System.out.println("✅ Simple email sent successfully to: " + to);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send simple email to " + to + ": " + e.getMessage());
            return false;
        }
    }

    // Send HTML email
    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(senderEmail, senderName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML content
            
            javaMailSender.send(message);
            System.out.println("✅ HTML email sent successfully to: " + to);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send HTML email to " + to + ": " + e.getMessage());
            return false;
        }
    }

    // Send email with attachment
    public boolean sendEmailWithAttachment(String to, String subject, String text, String attachmentPath) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(senderEmail, senderName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);
            
            // Add attachment
            FileSystemResource file = new FileSystemResource(new File(attachmentPath));
            helper.addAttachment(file.getFilename(), file);
            
            javaMailSender.send(message);
            System.out.println("✅ Email with attachment sent successfully to: " + to);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send email with attachment to " + to + ": " + e.getMessage());
            return false;
        }
    }

    // Send subscription confirmation email
    public boolean sendSubscriptionConfirmation(String to) {
        String subject = "Welcome to TheCloudCode Newsletter! 🚀";
        String htmlContent = createWelcomeEmailHtml(to);
        
        return sendHtmlEmail(to, subject, htmlContent);
    }

    // Send unsubscribe confirmation
    public boolean sendUnsubscribeConfirmation(String to) {
        String subject = "You've been unsubscribed from TheCloudCode Newsletter";
        String text = """
            Hi there,
            
            You have been successfully unsubscribed from TheCloudCode Newsletter.
            
            We're sorry to see you go! If you change your mind, you can always 
            resubscribe at https://thecloudcode.com
            
            Best regards,
            The TheCloudCode Team
            """;
            
        return sendSimpleEmail(to, subject, text);
    }

    private String createWelcomeEmailHtml(String email) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Welcome to TheCloudCode Newsletter</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #FF6B35 0%, #F7931E 100%); color: white; padding: 30px 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: white; padding: 30px; border: 1px solid #eee; }
                    .footer { background: #f8f9fa; padding: 20px; text-align: center; font-size: 12px; color: #666; border-radius: 0 0 10px 10px; }
                    .button { background: #FF6B35; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to TheCloudCode! 🎉</h1>
                        <p>Your daily dose of tech insights starts now</p>
                    </div>
                    <div class="content">
                        <h2>Thanks for subscribing!</h2>
                        <p>Welcome to our community of developers and tech enthusiasts. Here's what you can expect:</p>
                        <ul>
                            <li>📰 <strong>Daily Tech News</strong> - Curated articles from top tech sources</li>
                            <li>🤖 <strong>AI-Powered Summaries</strong> - Quick insights to save your time</li>
                            <li>☁️ <strong>Cloud & DevOps</strong> - Latest trends and best practices</li>
                            <li>📚 <strong>Learning Resources</strong> - Tutorials and guides for skill development</li>
                        </ul>
                        <p>Your first newsletter will arrive tomorrow morning at 8 AM IST.</p>
                        <a href="https://thecloudcode-frontend.vercel.app/" class="button">Visit Our Website</a>
                    </div>
                    <div class="footer">
                        <p>You're receiving this because you subscribed to TheCloudCode Newsletter</p>
                        <p>
                            <a href="https://thecloudcode.com/unsubscribe?email=""" + email + """
                            ">Unsubscribe</a> | 
                            <a href="https://thecloudcode.com/preferences?email=""" + email + """
                            ">Email Preferences</a>
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }
}