package com.thecloudcode.cc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import  jakarta.servlet.ServletException;
import  jakarta.servlet.http.HttpServletRequest;
import  jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class AdminIpFilter extends OncePerRequestFilter {

    @Value("${admin.allowed-ips:223.185.43.65}")
    private String allowedIpsString;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        
        // Check if this is an admin route
        if (requestPath.startsWith("/admin") || requestPath.contains("/admin/")) {
            String clientIp = getClientIpAddress(request);
            List<String> allowedIps = Arrays.asList(allowedIpsString.split(","));
            
            System.out.println("Admin access attempt from IP: " + clientIp);
            
            if (!isIpAllowed(clientIp, allowedIps)) {
                // Redirect to taunt page
                response.sendRedirect("/api/taunt");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
    

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private boolean isIpAllowed(String clientIp, List<String> allowedIps) {
        // Handle localhost variations
        if (clientIp.equals("0:0:0:0:0:0:0:1")) {
            clientIp = "::1";
        }
        
        for (String allowedIp : allowedIps) {
            if (allowedIp.trim().equals(clientIp)) {
                return true;
            }
        }
        return false;
    }
}