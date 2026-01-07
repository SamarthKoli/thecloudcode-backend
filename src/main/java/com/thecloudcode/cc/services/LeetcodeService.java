// src/main/java/com/thecloudcode/cc/services/LeetCodeService.java
package com.thecloudcode.cc.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class LeetcodeService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String LEETCODE_GRAPHQL_URL = "https://leetcode.com/graphql";
    
    public Map<String, Object> fetchUserProfile(String username) {
        try {
            // This query DEFINITELY fetches all badges including monthly challenges
            String query = String.format("""
                query getUserProfile($username: String!) {
                  matchedUser(username: $username) {
                    username
                    profile {
                      ranking
                      userAvatar
                    }
                    submitStats {
                      acSubmissionNum {
                        difficulty
                        count
                      }
                    }
                    badges {
                      id
                      name
                      displayName
                      icon
                      creationDate
                    }
                  }
                }
                """);
            
            Map<String, Object> variables = new HashMap<>();
            variables.put("username", username);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", query);
            requestBody.put("variables", variables);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Content-Type", "application/json");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                LEETCODE_GRAPHQL_URL,
                HttpMethod.POST,
                request,
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode userData = root.path("data").path("matchedUser");
                
                if (userData.isMissingNode() || userData.isNull()) {
                    throw new RuntimeException("User not found");
                }
                
                return parseUserData(userData);
            }
            
            throw new RuntimeException("Failed to fetch LeetCode data");
            
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
    
 private Map<String, Object> parseUserData(JsonNode userData) {
    Map<String, Object> result = new HashMap<>();
    
    // Basic info
    result.put("username", userData.path("username").asText());
    result.put("avatar", userData.path("profile").path("userAvatar").asText());
    result.put("ranking", userData.path("profile").path("ranking").asInt());
    
    // Calculate total solved
    JsonNode submissions = userData.path("submitStats").path("acSubmissionNum");
    int totalSolved = 0;
    for (JsonNode sub : submissions) {
        if ("All".equals(sub.path("difficulty").asText())) {
            totalSolved = sub.path("count").asInt();
            break;
        }
    }
    result.put("totalSolved", totalSolved);
    
    // Parse badges and proxy URLs
    List<Map<String, String>> badges = new ArrayList<>();
    JsonNode badgesNode = userData.path("badges");
    
    if (badgesNode.isArray()) {
        for (JsonNode badge : badgesNode) {
            String displayName = badge.path("displayName").asText("");
            String icon = badge.path("icon").asText("");
            
            if (displayName.isEmpty()) {
                continue;
            }
            
            // Fix relative URLs
            if (!icon.isEmpty()) {
                if (icon.startsWith("/")) {
                    icon = "https://leetcode.com" + icon;
                } else if (!icon.startsWith("http")) {
                    icon = "https://assets.leetcode.com/static_assets/public/webpack_bundles/images/logo-dark.e99485d9b.svg";
                }
                // REPLACE WITH THIS:
if (icon.startsWith("/")) {
    icon = "https://leetcode.com" + icon;
}
// Just return the raw LeetCode URL. The frontend will handle the proxying.
result.put("icon", icon); 
                // // Proxy through our backend to avoid CORS
                // icon = "http://localhost:8080/api/proxy/badge-image?url=" + 
                //        java.net.URLEncoder.encode(icon, java.nio.charset.StandardCharsets.UTF_8);
            } else {
                icon = "http://localhost:8080/api/proxy/badge-image?url=" + 
                       java.net.URLEncoder.encode("https://assets.leetcode.com/static_assets/public/webpack_bundles/images/logo-dark.e99485d9b.svg", 
                       java.nio.charset.StandardCharsets.UTF_8);
            }
            
            System.out.println("Badge: " + displayName + " | Proxied Icon: " + icon);
            
            Map<String, String> badgeInfo = new HashMap<>();
            badgeInfo.put("name", displayName);
            badgeInfo.put("icon", icon);
            badges.add(badgeInfo);
        }
    }
    
    result.put("badges", badges);
    result.put("badgeCount", badges.size());
    
    return result;
}

}
