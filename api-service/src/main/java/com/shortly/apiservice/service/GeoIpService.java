package com.shortly.apiservice.service;

public interface GeoIpService {
    /**
     * Resolve the ISO country code for the given IP address.
     * Returns null when the GeoIP database is unavailable or the lookup fails.
     */
    String lookupCountry(String ipAddress);
}
