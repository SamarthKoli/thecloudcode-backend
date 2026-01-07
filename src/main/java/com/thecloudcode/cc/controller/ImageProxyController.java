package com.thecloudcode.cc.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/v1/assets")
public class ImageProxyController {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @GetMapping("/badge-image")
    public ResponseEntity<byte[]> proxyBadgeImage(@RequestParam String url) {
        try {
            // Fetch image from LeetCode
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            headers.set("Referer", "https://leetcode.com/");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                byte[].class
            );
            
           if (response.getStatusCode() == HttpStatus.OK) {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(MediaType.IMAGE_PNG);
    // Add this line to allow the browser to read the image for html2canvas
    responseHeaders.setAccessControlAllowOrigin("https://www.thecloudcode.fun");
    return new ResponseEntity<>(response.getBody(), responseHeaders, HttpStatus.OK);
}

            
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            System.err.println("Error proxying image: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}