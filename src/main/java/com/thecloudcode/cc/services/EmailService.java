package com.thecloudcode.cc.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender javaMailSender;

    @Autowired(required = false)
    private MailMicroserviceClient mailMicroserviceClient;

    @Value("${spring.mail.username:}")
    private String senderEmail;

    @Value("${app.newsletter.sender.name:TheCloudCode Newsletter}")
    private String senderName;

    @Value("${email.provider:smtp}")
    private String emailProvider;

    // Enhanced email validation
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    // Send simple text email
    public boolean sendSimpleEmail(String to, String subject, String text) {
        if (!isValidEmail(to)) {
            System.err.println("❌ Invalid email format: " + to);
            return false;
        }

        // Use mail microservice if configured
        if ("mail-microservice".equalsIgnoreCase(emailProvider) && mailMicroserviceClient != null) {
            String htmlContent = "<html><body><pre style='font-family: Arial, sans-serif; white-space: pre-wrap;'>" + 
                                  escapeHtml(text) + "</pre></body></html>";
            return mailMicroserviceClient.sendEmail(to, subject, htmlContent);
        }

        // Fallback to SMTP
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            javaMailSender.send(message);
            System.out.println("✅ SMTP: Email sent successfully to: " + to);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Failed to send SMTP email to " + to + ": " + e.getMessage());
            return false;
        }
    }

    // Send HTML email
    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        if (!isValidEmail(to)) {
            System.err.println("❌ Invalid email format: " + to);
            return false;
        }

        // Use mail microservice if configured
        if ("mail-microservice".equalsIgnoreCase(emailProvider) && mailMicroserviceClient != null) {
            System.out.println("Using mail microservice to send email to: " + to);
            return mailMicroserviceClient.sendEmail(to, subject, htmlContent);
        }

        // Fallback to SMTP
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, senderName);
            helper.setTo(to);
            helper.setSubject(subject);

            // Set both plain text and HTML for better deliverability
            String plainText = htmlContent.replaceAll("\\<[^>]*>", "");
            helper.setText(plainText, htmlContent);

            javaMailSender.send(message);
            System.out.println("✅ SMTP: HTML email sent successfully to: " + to);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Failed to send SMTP HTML email to " + to + ": " + e.getMessage());
            return false;
        }
    }

    public boolean sendSubscriptionConfirmation(String to) {
        String subject = "🎉 Welcome to TheCloudCode Newsletter!";
        String htmlContent = createWelcomeEmailHtml(to);

        System.out.println("📧 Sending welcome email to: " + to + " via " + emailProvider);
        return sendHtmlEmail(to, subject, htmlContent);
    }

    public boolean sendUnsubscribeConfirmation(String to) {
        String subject = "You've been unsubscribed from TheCloudCode Newsletter";
        String htmlContent = createUnsubscribeEmailHtml(to);

        System.out.println("📧 Sending unsubscribe confirmation to: " + to + " via " + emailProvider);
        return sendHtmlEmail(to, subject, htmlContent);
    }

    public boolean sendDailyNewsletter(String to, String subject, String htmlContent) {
        System.out.println("📧 Sending newsletter to: " + to + " via " + emailProvider);
        return sendHtmlEmail(to, subject, htmlContent);
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    private String createWelcomeEmailHtml(String email) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Welcome to TheCloudCode Newsletter</title>
            </head>
            <body style="font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f8f9fa;">
                
                <!-- Header -->
                <div style="background: linear-gradient(135deg, #ff6b35, #f7931e); color: white; padding: 30px; border-radius: 12px; text-align: center; margin-bottom: 30px; box-shadow: 0 4px 15px rgba(255, 107, 53, 0.3);">
                    <h1 style="margin: 0; font-size: 32px; font-weight: 700;">🚀 Welcome to TheCloudCode!</h1>
                    <p style="margin: 15px 0 0 0; font-size: 18px; opacity: 0.95;">Your daily dose of tech insights</p>
                </div>
                
                <!-- Welcome Message -->
                <div style="background: white; padding: 30px; border-radius: 12px; margin-bottom: 25px; box-shadow: 0 2px 10px rgba(0,0,0,0.05);">
                    <h2 style="color: #ff6b35; margin-top: 0; font-size: 24px;">🎊 Thank you for subscribing!</h2>
                    <p style="font-size: 16px; line-height: 1.7; margin-bottom: 20px;">You've successfully joined <strong>thousands of developers</strong> who stay ahead of the curve with our AI-curated tech newsletter.</p>
                    <p style="font-size: 16px; line-height: 1.7; color: #666;">Get ready for amazing tech content delivered straight to your inbox!</p>
                </div>
                
                <!-- What You'll Receive -->
                <div style="background: white; padding: 30px; border-radius: 12px; margin-bottom: 25px; box-shadow: 0 2px 10px rgba(0,0,0,0.05);">
                    <h3 style="color: #333; margin-top: 0; font-size: 20px;">📬 What you'll receive:</h3>
                    <div style="margin-top: 20px;">
                        <div style="padding: 12px 0; border-bottom: 1px solid #f0f0f0;">
                            <span style="font-size: 16px;">⚡ Daily tech insights and breaking news</span>
                        </div>
                        <div style="padding: 12px 0; border-bottom: 1px solid #f0f0f0;">
                            <span style="font-size: 16px;">🤖 AI-curated articles from top sources</span>
                        </div>
                        <div style="padding: 12px 0; border-bottom: 1px solid #f0f0f0;">
                            <span style="font-size: 16px;">🔥 Industry trends and analysis</span>
                        </div>
                        <div style="padding: 12px 0;">
                            <span style="font-size: 16px;">💡 Developer tools and career tips</span>
                        </div>
                    </div>
                </div>
                
                <!-- CTA Button -->
                <div style="text-align: center; margin: 35px 0;">
                    <a href="https://thecloudcode.com" 
                       style="background: linear-gradient(135deg, #ff6b35, #f7931e); 
                              color: white; 
                              padding: 15px 35px; 
                              text-decoration: none; 
                              border-radius: 30px; 
                              font-weight: 600; 
                              font-size: 16px; 
                              display: inline-block; 
                              box-shadow: 0 4px 15px rgba(255, 107, 53, 0.3);">
                        🌐 Visit TheCloudCode
                    </a>
                </div>
                
                <!-- Footer -->
                <div style="text-align: center; padding: 25px 20px; border-top: 2px solid #f0f0f0; color: #666; font-size: 14px; margin-top: 30px;">
                    <p style="margin-bottom: 10px; font-size: 16px;">
                        <strong style="color: #ff6b35;">The TheCloudCode Team</strong>
                    </p>
                    <p style="margin-bottom: 15px;">Making tech accessible, one newsletter at a time 🚀</p>
                    <p style="font-size: 12px; color: #999;">
                        If you don't want to receive these emails, you can 
                        <a href="https://thecloudcode.com/unsubscribe" style="color: #ff6b35; text-decoration: underline;">unsubscribe anytime</a>.
                    </p>
                </div>
            </body>
            </html>
            """;
    }

    private String createUnsubscribeEmailHtml(String email) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Unsubscribed from TheCloudCode</title>
            </head>
            <body style="font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f8f9fa;">
                <div style="background: white; padding: 40px; border-radius: 12px; text-align: center; box-shadow: 0 2px 10px rgba(0,0,0,0.05);">
                    <h1 style="color: #ff6b35; margin-top: 0; font-size: 28px;">👋 You've been unsubscribed</h1>
                    <p style="font-size: 16px; line-height: 1.7; margin-bottom: 20px;">You have been successfully unsubscribed from TheCloudCode Newsletter.</p>
                    <p style="font-size: 16px; line-height: 1.7; color: #666;">We're sorry to see you go! If you change your mind, you can always resubscribe at our website.</p>
                    <div style="margin: 30px 0;">
                        <a href="https://thecloudcode.com" 
                           style="background: linear-gradient(135deg, #ff6b35, #f7931e); 
                                  color: white; 
                                  padding: 12px 30px; 
                                  text-decoration: none; 
                                  border-radius: 25px; 
                                  font-weight: 600; 
                                  display: inline-block;">
                            🌐 Visit TheCloudCode
                        </a>
                    </div>
                    <p style="font-size: 12px; color: #666; margin-top: 40px;">
                        Thanks for being part of our community!<br/>
                        <strong style="color: #ff6b35;">The TheCloudCode Team</strong>
                    </p>
                </div>
            </body>
            </html>
            """;
    }
}
