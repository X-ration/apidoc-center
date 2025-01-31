package com.adam.apidoc_center.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MemoryCacheService {

    private final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> expireMap = new ConcurrentHashMap<>();

    public String getValue(String key) {
        return map.get(key);
    }

    /**
     * setValue
     * @param key
     * @param value
     * @return key先前对应的value
     */
    public String setValue(String key, String value) {
        return map.put(key, value);
    }

    public String setValueExpire(String key, String value, long milliseconds) {
        if(milliseconds > 0) {
            String oldValue = map.put(key, value);
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expireTime = now.plus(milliseconds, ChronoUnit.MILLIS);
            expireMap.put(key, expireTime);
            return oldValue;
        } else {
            return map.get(key);
        }
    }

    @Scheduled(cron = "0 0/2 * * * ? ")
    public void expireKeys() {
        for(Map.Entry<String, LocalDateTime> entry: expireMap.entrySet()) {
            String key = entry.getKey();
            LocalDateTime expireTime = entry.getValue();
            LocalDateTime now = LocalDateTime.now();
            if(!expireTime.isBefore(now)) {
                String value = map.remove(key);
                expireMap.remove(key);
                log.debug("Expired key {} value {} expire time {}", key, value, expireTime);
            }
        }
    }

}
