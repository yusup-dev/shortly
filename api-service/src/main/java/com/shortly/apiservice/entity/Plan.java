package com.shortly.apiservice.entity;

import com.shortly.apiservice.enumaration.PlanType;
import com.shortly.apiservice.enumaration.RoleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "plans")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", length = 20, nullable = false)
    private PlanType name;

    @Column(name = "max_requests_per_day", nullable = false)
    private Integer maxRequestsPerDay;

    @Column(name = "max_urls_per_key", nullable = false)
    private Integer maxUrlsPerKey;

    @Column(name = "max_bulk", nullable = false)
    private Integer maxBulk;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
