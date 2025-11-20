package Green_trade.green_trade_platform.filter;

import Green_trade.green_trade_platform.repository.SystemConfigRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

@Component
@Slf4j
public class BadWordFilter {
    private final SystemConfigRepository systemConfigRepository;
    private final ObjectMapper objectMapper;

    // Default bad words (fallback if config not found)
    private static final List<String> DEFAULT_BAD_WORDS = Arrays.asList(
            "địt", "dit", "cặc", "cac", "lồn", "lon", "buồi", "buoi",
            "đụ", "du", "đm", "dm", "đéo", "deo", "đĩ", "di",
            "mẹ mày", "me may", "khốn", "súc vật", "thằng chó", "thang cho"
    );

    // Default whitelist (fallback if config not found)
    private static final Set<String> DEFAULT_WHITELIST = new HashSet<>(Arrays.asList(
            "điện", "dây điện", "ổ cắm", "điện áp", "sạc", "pin", "đèn", "công tắc",
            "bóng đèn", "quạt", "máy lạnh", "máy giặt", "lò vi sóng", "ổ cắm điện"
    ));

    private static final String BAD_WORDS_CONFIG_KEY = "BAD_WORDS";
    private static final String BAD_WORDS_WHITELIST_CONFIG_KEY = "BAD_WORDS_WHITELIST";

    // Cache for bad words and whitelist
    private volatile List<String> cachedBadWords = new ArrayList<>(DEFAULT_BAD_WORDS);
    private volatile Set<String> cachedWhitelist = new HashSet<>(DEFAULT_WHITELIST);

    public BadWordFilter(SystemConfigRepository systemConfigRepository, ObjectMapper objectMapper) {
        this.systemConfigRepository = systemConfigRepository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        refreshBadWords();
        refreshWhitelist();
    }

    public void refreshBadWords() {
        try {
            Optional<Green_trade.green_trade_platform.model.SystemConfig> configOpt = systemConfigRepository.findByConfigKey(BAD_WORDS_CONFIG_KEY);
            if (configOpt.isPresent()) {
                String configValue = configOpt.get().getConfigValue();
                List<String> badWords = parseJsonList(configValue);
                if (badWords != null && !badWords.isEmpty()) {
                    cachedBadWords = new ArrayList<>(badWords);
                    log.info(">>> [BadWordFilter] Loaded {} bad words from config", cachedBadWords.size());
                    return;
                }
            }
            log.info(">>> [BadWordFilter] Using default bad words ({} words)", DEFAULT_BAD_WORDS.size());
            cachedBadWords = new ArrayList<>(DEFAULT_BAD_WORDS);
        } catch (Exception e) {
            log.error(">>> [BadWordFilter] Error loading bad words from config, using defaults", e);
            cachedBadWords = new ArrayList<>(DEFAULT_BAD_WORDS);
        }
    }

    public void refreshWhitelist() {
        try {
            Optional<Green_trade.green_trade_platform.model.SystemConfig> configOpt = systemConfigRepository.findByConfigKey(BAD_WORDS_WHITELIST_CONFIG_KEY);
            if (configOpt.isPresent()) {
                String configValue = configOpt.get().getConfigValue();
                List<String> whitelist = parseJsonList(configValue);
                if (whitelist != null && !whitelist.isEmpty()) {
                    cachedWhitelist = new HashSet<>(whitelist);
                    log.info(">>> [BadWordFilter] Loaded {} whitelist words from config", cachedWhitelist.size());
                    return;
                }
            }
            log.info(">>> [BadWordFilter] Using default whitelist ({} words)", DEFAULT_WHITELIST.size());
            cachedWhitelist = new HashSet<>(DEFAULT_WHITELIST);
        } catch (Exception e) {
            log.error(">>> [BadWordFilter] Error loading whitelist from config, using defaults", e);
            cachedWhitelist = new HashSet<>(DEFAULT_WHITELIST);
        }
    }

    private List<String> parseJsonList(String json) {
        try {
            if (json == null || json.trim().isEmpty()) {
                return null;
            }
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.error(">>> [BadWordFilter] Error parsing JSON list: {}", json, e);
            return null;
        }
    }

    private String normalize(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        normalized = normalized.replaceAll("đ", "d").replaceAll("Đ", "D");
        return normalized.toLowerCase();
    }

    public boolean containsBadWord(String text) {
        if (text == null || text.isEmpty()) return false;

        String normalized = normalize(text);

        // Check against cached whitelist first (to avoid false positives)
        // Note: Whitelist check is informational, bad word check still runs

        // Check against cached bad words
        List<String> badWords = new ArrayList<>(cachedBadWords);
        for (String bad : badWords) {
            String normalizedBad = normalize(bad);
            // Match full word boundaries or phrases
            String regex = "\\b" + Pattern.quote(normalizedBad) + "\\b";
            if (Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(normalized).find()) {
                // Double-check against whitelist to avoid false positives
                Set<String> whitelist = new HashSet<>(cachedWhitelist);
                boolean isWhitelisted = false;
                for (String safe : whitelist) {
                    String normalizedSafe = normalize(safe);
                    if (normalized.contains(normalizedSafe) && normalizedSafe.contains(normalizedBad)) {
                        isWhitelisted = true;
                        break;
                    }
                }
                if (!isWhitelisted) {
                    return true;
                }
            }
        }

        return false;
    }

    public String censorBadWords(String text) {
        if (text == null || text.isEmpty()) return text;
        String censored = text;
        List<String> badWords = new ArrayList<>(cachedBadWords);
        for (String bad : badWords) {
            String regex = "(?i)\\b" + Pattern.quote(bad) + "\\b";
            censored = censored.replaceAll(regex, "***");
        }
        return censored;
    }

    public List<String> getBadWords() {
        return new ArrayList<>(cachedBadWords);
    }

    public Set<String> getWhitelist() {
        return new HashSet<>(cachedWhitelist);
    }
}
