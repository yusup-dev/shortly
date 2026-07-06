package com.shortly.apiservice.service.impl;

import com.shortly.apiservice.constant.CacheConstants;
import com.shortly.apiservice.dto.response.AdvancedAnalyticsResponse;
import com.shortly.apiservice.dto.response.ClickAnalyticsResponse;
import com.shortly.apiservice.dto.response.PeriodResponse;
import com.shortly.apiservice.entity.UrlClick;
import com.shortly.apiservice.repository.UrlClickRepository;
import com.shortly.apiservice.repository.projection.UrlClickAggregateProjection;
import com.shortly.apiservice.service.AnalyticsService;
import com.shortly.apiservice.service.GeoIpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final UrlClickRepository urlClickRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserAgentAnalyzer userAgentAnalyzer;
    private final GeoIpService geoIpService;

    @Async("analyticsExecutor")
    @Override
    public void recordClickAsync(UUID urlId, String ipAddress, String userAgent, String referer) {
        try {
            String device = null;
            String os = null;
            String browser = null;

            if (StringUtils.hasText(userAgent)) {
                UserAgent parsed = userAgentAnalyzer.parse(userAgent);
                device = parsed.getValue(UserAgent.DEVICE_CLASS);
                os = parsed.getValue(UserAgent.OPERATING_SYSTEM_NAME);
                browser = parsed.getValue(UserAgent.AGENT_NAME);
            }

            String country = geoIpService.lookupCountry(ipAddress);
            String referrerHost = extractHost(referer);

            UrlClick click = UrlClick.builder()
                    .urlId(urlId)
                    .ipAddress(ipAddress)
                    .country(country)
                    .device(device)
                    .os(os)
                    .browser(browser)
                    .referrerHost(referrerHost)
                    .build();

            urlClickRepository.save(click);

            String today = LocalDate.now().toString();
            String visitor = StringUtils.hasText(ipAddress) ? ipAddress : "unknown";

            stringRedisTemplate.opsForHyperLogLog()
                    .add(CacheConstants.CACHE_ANALYTICS_HLL + urlId + ":" + today, visitor);
            stringRedisTemplate.opsForHyperLogLog()
                    .add(CacheConstants.CACHE_ANALYTICS_HLL_GLOBAL + today, visitor);
            stringRedisTemplate.opsForValue()
                    .increment(CacheConstants.CACHE_ANALYTICS_TOTAL + urlId);
        } catch (Exception e) {
            log.error("Failed to record click analytics for urlId={}", urlId, e);
        }
    }

    @Override
    public long getTotalClicks(UUID urlId) {
        String key = CacheConstants.CACHE_ANALYTICS_TOTAL + urlId;
        String cached = stringRedisTemplate.opsForValue().get(key);

        if (cached != null) {
            try {
                return Long.parseLong(cached);
            } catch (NumberFormatException ignored) {
                // fall through to DB
            }
        }

        long count = urlClickRepository.countByUrlIdAndPeriod(
                urlId, LocalDateTime.of(1970, 1, 1, 0, 0), LocalDateTime.now());
        stringRedisTemplate.opsForValue().set(key, String.valueOf(count));
        return count;
    }

    @Override
    public ClickAnalyticsResponse getAnalytics(UUID urlId, String shortUrl, LocalDate from, LocalDate to) {
        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.atTime(LocalTime.MAX);

        long totalClicks = urlClickRepository.countByUrlIdAndPeriod(urlId, fromDt, toDt);
        long uniqueVisitors = countUniqueVisitors(urlId, from, to);

        return ClickAnalyticsResponse.builder()
                .shortUrl(shortUrl)
                .period(PeriodResponse.builder().from(from).to(to).build())
                .totalClicks(totalClicks)
                .uniqueVisitors(uniqueVisitors)
                .build();
    }

    @Override
    public AdvancedAnalyticsResponse getAdvancedAnalytics(UUID urlId, LocalDate from, LocalDate to) {
        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.atTime(LocalTime.MAX);

        long totalClicks = urlClickRepository.countByUrlIdAndPeriod(urlId, fromDt, toDt);
        long uniqueVisitors = countUniqueVisitors(urlId, from, to);

        return AdvancedAnalyticsResponse.builder()
                .totalClicks(totalClicks)
                .uniqueVisitors(uniqueVisitors)
                .byDay(toMapList("date", urlClickRepository.aggregateByDay(urlId, fromDt, toDt)))
                .byCountry(toMapList("country", urlClickRepository.aggregateByCountry(urlId, fromDt, toDt)))
                .byDevice(toMapList("device", urlClickRepository.aggregateByDevice(urlId, fromDt, toDt)))
                .byOs(toMapList("os", urlClickRepository.aggregateByOs(urlId, fromDt, toDt)))
                .byBrowser(toMapList("browser", urlClickRepository.aggregateByBrowser(urlId, fromDt, toDt)))
                .byReferrer(toMapList("referrer", urlClickRepository.aggregateByReferrer(urlId, fromDt, toDt)))
                .build();
    }

    private long countUniqueVisitors(UUID urlId, LocalDate from, LocalDate to) {
        List<String> keys = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            keys.add(CacheConstants.CACHE_ANALYTICS_HLL + urlId + ":" + d);
        }

        if (keys.isEmpty()) {
            return 0;
        }

        Long count = stringRedisTemplate.opsForHyperLogLog().size(keys.toArray(new String[0]));
        return count == null ? 0 : count;
    }

    private List<Map<String, Object>> toMapList(String labelKey, List<UrlClickAggregateProjection> rows) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (UrlClickAggregateProjection row : rows) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put(labelKey, row.getLabel());
            entry.put("clicks", row.getClicks());
            result.add(entry);
        }
        return result;
    }

    private String extractHost(String referer) {
        if (!StringUtils.hasText(referer)) {
            return null;
        }
        try {
            return URI.create(referer).getHost();
        } catch (Exception e) {
            return null;
        }
    }
}
