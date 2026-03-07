package com.shortly.apiservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "quotas")
public class Quota {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_key_id")
    private ApiKey apiKey;

    @Column(name = "max_requests_per_day", nullable = false)
    private Integer maxRequestsPerDay;

    @Column(name = "max_urls_per_key", nullable = false)
    private Integer maxUrlsPerKey;

    @Column(name = "max_bulk", nullable = false)
    private Integer maxBulk;
}
