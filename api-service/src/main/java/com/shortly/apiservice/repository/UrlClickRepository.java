package com.shortly.apiservice.repository;

import com.shortly.apiservice.entity.UrlClick;
import com.shortly.apiservice.repository.projection.UrlClickAggregateProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface UrlClickRepository extends JpaRepository<UrlClick, UUID> {

    @Query(value = """
            SELECT COUNT(*)
            FROM url_clicks
            WHERE url_id = :urlId
            AND clicked_at BETWEEN :from AND :to
            """, nativeQuery = true)
    long countByUrlIdAndPeriod(
            @Param("urlId") UUID urlId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query(value = """
            SELECT COUNT(*)
            FROM url_clicks c
            JOIN urls u ON u.id = c.url_id
            WHERE u.create_by_user_id = :userId
            AND clicked_at::date = CURRENT_DATE
            """, nativeQuery = true)
    long countTodayByUser(@Param("userId") UUID userId);

    @Query(value = """
            SELECT COUNT(*) FROM url_clicks
            """, nativeQuery = true)
    long countAllTime();

    @Query(value = """
            SELECT COUNT(*) FROM url_clicks
            WHERE clicked_at::date = CURRENT_DATE
            """, nativeQuery = true)
    long countToday();

    @Query(value = """
            SELECT TO_CHAR(clicked_at, 'YYYY-MM-DD') AS label, COUNT(*) AS clicks
            FROM url_clicks
            WHERE url_id = :urlId AND clicked_at BETWEEN :from AND :to
            GROUP BY label
            ORDER BY label
            """, nativeQuery = true)
    List<UrlClickAggregateProjection> aggregateByDay(
            @Param("urlId") UUID urlId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query(value = """
            SELECT COALESCE(country, 'Unknown') AS label, COUNT(*) AS clicks
            FROM url_clicks
            WHERE url_id = :urlId AND clicked_at BETWEEN :from AND :to
            GROUP BY label
            ORDER BY clicks DESC
            """, nativeQuery = true)
    List<UrlClickAggregateProjection> aggregateByCountry(
            @Param("urlId") UUID urlId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query(value = """
            SELECT COALESCE(device, 'Unknown') AS label, COUNT(*) AS clicks
            FROM url_clicks
            WHERE url_id = :urlId AND clicked_at BETWEEN :from AND :to
            GROUP BY label
            ORDER BY clicks DESC
            """, nativeQuery = true)
    List<UrlClickAggregateProjection> aggregateByDevice(
            @Param("urlId") UUID urlId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query(value = """
            SELECT COALESCE(os, 'Unknown') AS label, COUNT(*) AS clicks
            FROM url_clicks
            WHERE url_id = :urlId AND clicked_at BETWEEN :from AND :to
            GROUP BY label
            ORDER BY clicks DESC
            """, nativeQuery = true)
    List<UrlClickAggregateProjection> aggregateByOs(
            @Param("urlId") UUID urlId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query(value = """
            SELECT COALESCE(browser, 'Unknown') AS label, COUNT(*) AS clicks
            FROM url_clicks
            WHERE url_id = :urlId AND clicked_at BETWEEN :from AND :to
            GROUP BY label
            ORDER BY clicks DESC
            """, nativeQuery = true)
    List<UrlClickAggregateProjection> aggregateByBrowser(
            @Param("urlId") UUID urlId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query(value = """
            SELECT COALESCE(referrer_host, 'direct') AS label, COUNT(*) AS clicks
            FROM url_clicks
            WHERE url_id = :urlId AND clicked_at BETWEEN :from AND :to
            GROUP BY label
            ORDER BY clicks DESC
            """, nativeQuery = true)
    List<UrlClickAggregateProjection> aggregateByReferrer(
            @Param("urlId") UUID urlId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
