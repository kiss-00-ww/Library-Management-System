package com.library.book.config;

import com.library.book.entity.SystemConfig;
import com.library.book.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SysConfigCache {

    @Autowired
    private SystemConfigService systemConfigService;

    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        List<SystemConfig> configs = systemConfigService.list();
        ConcurrentHashMap<String, String> newCache = new ConcurrentHashMap<>();
        for (SystemConfig config : configs) {
            newCache.put(config.getConfigKey(), config.getConfigValue());
        }
        cache.clear();
        cache.putAll(newCache);
        log.info("System config cache refreshed, {} items loaded", newCache.size());
    }

    public String getString(String key, String defaultValue) {
        String value = cache.get(key);
        return value != null ? value : defaultValue;
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public int getInt(String key, int defaultValue) {
        String value = cache.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Config key '{}' value '{}' is not a valid integer, using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    public double getDouble(String key, double defaultValue) {
        String value = cache.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.warn("Config key '{}' value '{}' is not a valid double, using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        String value = cache.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            log.warn("Config key '{}' value '{}' is not a valid BigDecimal, using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }
}
