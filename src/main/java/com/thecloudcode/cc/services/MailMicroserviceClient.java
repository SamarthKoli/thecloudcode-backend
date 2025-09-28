package com.thecloudcode.cc.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
public class MailMicroserviceClient {

    @Value("${mail.microservice.url}")
    private String mailServiceUrl;

    private final WebClient webClient = WebClient.builder().build();

    public boolean sendEmail(String toEmail, String subject, String htmlContent) {
        EmailRequest emailRequest = new EmailRequest(toEmail, subject, htmlContent);

        try {
            Mono<Void> responseMono = webClient.post()
                    .uri(mailServiceUrl + "/api/send-email")
                    .bodyValue(emailRequest)
                    .retrieve()
                    .onStatus(
                        status -> status.value() >= 400,
                        clientResponse -> Mono.error(
                            new IllegalStateException("Failed to send email: " + clientResponse.statusCode().toString()))
                    )
                    .bodyToMono(Void.class);

            responseMono.block();

            return true;
        } catch (WebClientResponseException e) {
            System.err.println("Error response from mail microservice: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            System.err.println("Exception calling mail microservice: " + e.getMessage());
            return false;
        }
    }

    private static class EmailRequest {
        private final String to;
        private final String subject;

        @JsonProperty("html")
        private final String htmlContent;

        public EmailRequest(String to, String subject, String htmlContent) {
            this.to = to;
            this.subject = subject;
            this.htmlContent = htmlContent;
        }

        public String getTo() {
            return to;
        }

        public String getSubject() {
            return subject;
        }

        public String getHtmlContent() {
            return htmlContent;
        }
    }
}

