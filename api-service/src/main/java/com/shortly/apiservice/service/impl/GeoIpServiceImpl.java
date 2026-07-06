package com.shortly.apiservice.service.impl;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CountryResponse;
import com.shortly.apiservice.service.GeoIpService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.net.InetAddress;

/**
 * Wraps the MaxMind GeoLite2 offline database. If no database file is configured
 * (property {@code geoip.database.path}) the service degrades gracefully and every
 * lookup simply returns {@code null}, instead of failing analytics recording.
 */
@Slf4j
@Service
public class GeoIpServiceImpl implements GeoIpService {

    @Value("${geoip.database.path:}")
    private String databasePath;

    private DatabaseReader reader;

    @PostConstruct
    public void init() {
        if (!StringUtils.hasText(databasePath)) {
            log.warn("geoip.database.path is not configured, GeoIP country lookup is disabled");
            return;
        }

        File file = new File(databasePath);
        if (!file.exists()) {
            log.warn("GeoIP database file not found at {}, country lookup is disabled", databasePath);
            return;
        }

        try {
            reader = new DatabaseReader.Builder(file).build();
            log.info("GeoIP database loaded from {}", databasePath);
        } catch (Exception e) {
            log.warn("Failed to load GeoIP database from {}: {}", databasePath, e.getMessage());
        }
    }

    @Override
    public String lookupCountry(String ipAddress) {
        if (reader == null || !StringUtils.hasText(ipAddress)) {
            return null;
        }

        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            CountryResponse response = reader.country(address);
            return response.getCountry() != null ? response.getCountry().getIsoCode() : null;
        } catch (Exception e) {
            log.debug("GeoIP lookup failed for ip={}: {}", ipAddress, e.getMessage());
            return null;
        }
    }
}
