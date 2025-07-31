package com.healthfirst.middleware;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    private final Map<String, RateLimitInfo> clientLimits = new ConcurrentHashMap<>();
    private final int MAX_ATTEMPTS = 5;
    private final int WINDOW_HOURS = 1;

    /**
     * Check if request is allowed for registration
     */
    public boolean isRegistrationAllowed(String clientIp) {
        RateLimitInfo limitInfo = clientLimits.computeIfAbsent(clientIp, k -> new RateLimitInfo());
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minus(WINDOW_HOURS, ChronoUnit.HOURS);
        
        // Clean old attempts outside the window
        limitInfo.getAttempts().removeIf(attempt -> attempt.isBefore(windowStart));
        
        // Check if under limit
        if (limitInfo.getAttempts().size() < MAX_ATTEMPTS) {
            limitInfo.getAttempts().add(now);
            return true;
        }
        
        return false;
    }

    /**
     * Get remaining attempts for client IP
     */
    public int getRemainingAttempts(String clientIp) {
        RateLimitInfo limitInfo = clientLimits.get(clientIp);
        if (limitInfo == null) {
            return MAX_ATTEMPTS;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minus(WINDOW_HOURS, ChronoUnit.HOURS);
        
        // Clean old attempts outside the window
        limitInfo.getAttempts().removeIf(attempt -> attempt.isBefore(windowStart));
        
        return Math.max(0, MAX_ATTEMPTS - limitInfo.getAttempts().size());
    }

    /**
     * Reset limits for client IP (for testing)
     */
    public void resetLimits(String clientIp) {
        clientLimits.remove(clientIp);
    }

    /**
     * Clear all limits (for testing or maintenance)
     */
    public void clearAllLimits() {
        clientLimits.clear();
    }

    // Inner class to hold rate limiting information
    private static class RateLimitInfo {
        private final java.util.List<LocalDateTime> attempts = new java.util.concurrent.CopyOnWriteArrayList<>();

        public java.util.List<LocalDateTime> getAttempts() {
            return attempts;
        }
    }
} 