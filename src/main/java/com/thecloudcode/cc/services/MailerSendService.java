package com.thecloudcode.cc.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

@Service
public class MailerSendService {

    @Value("${mailersend.api.token}")
    private String apiToken;

    @Value("${mailersend.from.email:newsletter@thecloudcode.com}")
    private String fromEmail;

    @Value("${mailersend.from.name:TheCloudCode}")
    private String fromName;

    @Value("${mailersend.enabled:false}")
    private boolean mailerSendEnabled;

    public boolean sendEmail(String toEmail, String subject, String htmlContent) {
        if (!mailerSendEnabled) {
            System.out.println("📧 MailerSend disabled - would send to: " + toEmail);
            return true; // Return true to simulate success
        }

        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost("https://api.mailersend.com/v1/email");

            // Set headers
            httpPost.setHeader("Authorization", "Bearer " + apiToken);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("X-Requested-With", "XMLHttpRequest");

            // Build email payload
            Map<String, Object> payload = new HashMap<>();
            
            // From
            Map<String, String> from = new HashMap<>();
            from.put("email", fromEmail);
            from.put("name", fromName);
            payload.put("from", from);

            // To
            Map<String, String> to = new HashMap<>();
            to.put("email", toEmail);
            payload.put("to", Arrays.asList(to));

            // Subject and content
            payload.put("subject", subject);
            payload.put("html", htmlContent);

            // Convert to JSON
            ObjectMapper mapper = new ObjectMapper();
            String jsonPayload = mapper.writeValueAsString(payload);

            System.out.println("📧 Sending email via MailerSend to: " + toEmail);

            // Set request body
            StringEntity entity = new StringEntity(jsonPayload, "UTF-8");
            httpPost.setEntity(entity);

            // Send request
            CloseableHttpResponse response = client.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();

            response.close();
            client.close();

            if (statusCode >= 200 && statusCode < 300) {
                System.out.println("✅ MailerSend email sent successfully to: " + toEmail);
                return true;
            } else {
                System.err.println("❌ MailerSend error: HTTP " + statusCode);
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ MailerSend exception: " + e.getMessage());
            return false;
        }
    }
}
