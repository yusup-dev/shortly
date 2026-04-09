package com.shortly.apiservice.configuration;

import com.shortly.apiservice.client.KgsClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class TestRunner {

    @Bean
    CommandLineRunner testKgs(KgsClient kgsClient) {
        return args -> {
            String key = kgsClient.getKey();
            log.info("Key from KGS: {}", key);
        };
    }
}

