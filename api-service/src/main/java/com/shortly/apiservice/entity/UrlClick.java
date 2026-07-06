package com.shortly.apiservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "url_clicks")
public class UrlClick {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "url_id", nullable = false)
    private UUID urlId;

    @CreationTimestamp
    @Column(name = "clicked_at", nullable = false, updatable = false)
    private LocalDateTime clickedAt;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "device", length = 50)
    private String device;

    @Column(name = "os", length = 50)
    private String os;

    @Column(name = "browser", length = 50)
    private String browser;

    @Column(name = "referrer_host")
    private String referrerHost;
}
