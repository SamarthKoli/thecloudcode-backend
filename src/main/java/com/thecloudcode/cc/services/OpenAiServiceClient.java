package com.thecloudcode.cc.services;


import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OpenAiServiceClient {

    @Autowired
    private OpenAiService openAiService;

    public String summarizeArticle(String title, String content) {
        try {
            String prompt = String.format(
                "Summarize this tech article in exactly 2-3 clear sentences for a daily tech newsletter. " +
                "Focus on the key innovation, business impact, and why tech professionals should care. " +
                "Keep it concise and engaging.\n\nTitle: %s\n\nContent: %s",
                title,
                content.length() > 2000 ? content.substring(0, 2000) + "..." : content
            );

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(new ChatMessage("user", prompt)))
                .maxTokens(120)
                .temperature(0.3)
                .build();

            List<ChatCompletionChoice> choices = openAiService.createChatCompletion(request).getChoices();
            return choices.stream()
                .map(ChatCompletionChoice::getMessage)
                .map(ChatMessage::getContent)
                .collect(Collectors.joining())
                .trim();
                
        } catch (Exception e) {
            System.err.println("Error summarizing article: " + e.getMessage());
            return "Summary unavailable - please check the original article.";
        }
    }

    public int scoreArticleRelevance(String title, String content) {
        try {
            String prompt = String.format(
                "Rate this tech article's importance for a daily tech newsletter audience on a scale of 1-10. " +
                "Consider: innovation level, business impact, audience interest, and timeliness. " +
                "Respond with ONLY a single number from 1-10, nothing else.\n\nTitle: %s\n\nContent: %s",
                title,
                content.length() > 1000 ? content.substring(0, 1000) + "..." : content
            );

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(new ChatMessage("user", prompt)))
                .maxTokens(10)
                .temperature(0.1)
                .build();

            List<ChatCompletionChoice> choices = openAiService.createChatCompletion(request).getChoices();
            String response = choices.stream()
                .map(ChatCompletionChoice::getMessage)
                .map(ChatMessage::getContent)
                .collect(Collectors.joining())
                .trim();

            String digit = response.replaceAll("[^0-9]", "");
            if (!digit.isEmpty()) {
                int score = Integer.parseInt(digit.substring(0, 1));
                return Math.max(1, Math.min(10, score));
            }
        } catch (Exception e) {
            System.err.println("Error scoring article relevance: " + e.getMessage());
        }
        return 5;
    }

    public String categorizeArticle(String title, String content) {
        try {
            String prompt = String.format(
                "Categorize this tech article into ONE of these exact categories: " +
                "AI/ML, Startups/Funding, Consumer Tech, Enterprise/Business, Security/Privacy, Developer Tools, Hardware. " +
                "Respond with ONLY the category name, nothing else.\n\nTitle: %s\n\nContent: %s",
                title,
                content.length() > 800 ? content.substring(0, 800) + "..." : content
            );

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(new ChatMessage("user", prompt)))
                .maxTokens(20)
                .temperature(0.1)
                .build();

            List<ChatCompletionChoice> choices = openAiService.createChatCompletion(request).getChoices();
            return choices.stream()
                .map(ChatCompletionChoice::getMessage)
                .map(ChatMessage::getContent)
                .collect(Collectors.joining())
                .trim();
                
        } catch (Exception e) {
            System.err.println("Error categorizing article: " + e.getMessage());
            return "General Tech";
        }
    }

    public String generateNewsletterSubject(List<String> topArticleTitles) {
        try {
            String prompt = String.format(
                "Create an engaging newsletter subject line (maximum 50 characters) based on these top tech stories: %s. " +
                "Make it compelling for busy tech professionals. Use format like 'Daily Tech: [Key Topics]' or 'Tech Brief: [Main Theme]'",
                String.join(", ", topArticleTitles.subList(0, Math.min(3, topArticleTitles.size())))
            );

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(new ChatMessage("user", prompt)))
                .maxTokens(30)
                .temperature(0.5)
                .build();

            List<ChatCompletionChoice> choices = openAiService.createChatCompletion(request).getChoices();
            return choices.stream()
                .map(ChatCompletionChoice::getMessage)
                .map(ChatMessage::getContent)
                .collect(Collectors.joining())
                .trim();
                
        } catch (Exception e) {
            System.err.println("Error generating subject line: " + e.getMessage());
            return "Daily Tech Updates";
        }
    }
}
