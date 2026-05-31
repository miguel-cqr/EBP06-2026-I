package com.tuapp.finanzas.user.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {
    private final Map<String, List<Long>> requestCounts = new ConcurrentHashMap<>();

    public boolean isAllowed(String username) {
        long now = System.currentTimeMillis();
        long oneMinuteAgo = now - 60000;

        requestCounts.putIfAbsent(username, new ArrayList<>());
        List<Long> timestamps = requestCounts.get(username);

        synchronized(timestamps) {
            timestamps.removeIf(t -> t < oneMinuteAgo);

            if (timestamps.size() >= 5) {
                return false;
            }

            timestamps.add(now);
            return true;
        }
    }
}