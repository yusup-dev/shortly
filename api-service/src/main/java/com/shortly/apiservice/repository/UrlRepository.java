package com.shortly.apiservice.repository;

import com.shortly.apiservice.entity.Url;
import com.shortly.apiservice.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UrlRepository extends JpaRepository<Url, UUID>, JpaSpecificationExecutor<Url> {

    Optional<Url> findByOriginalUrlAndUser(String url, User user);

    boolean existsByShortKey(String shortKey);

    Optional<Url> findByShortKey(String shortKey);

    @Query(value = """
            SELECT COUNT(*)
            FROM urls u
            JOIN api_keys ak ON u.api_key_id = ak.id
            WHERE ak.key_hash = :apiKey
            """, nativeQuery = true)
    Long countByApiKeyHash(@Param("apiKey") String apiKey);

    Page<Url> findByUserAndOriginalUrlContainingIgnoreCase(
            User user,
            String search,
            Pageable pageable
    );

    Page<Url> findByOriginalUrlContainingIgnoreCase(
            String search,
            Pageable pageable
    );

    Page<Url> findAll(Pageable pageable);

    Page<Url> findByUser(
            User user,
            Pageable pageable
    );

    Optional<Url> findByIdAndUser(UUID id,  User user);

    @Query(value = """
            SELECT COUNT(*) FROM urls WHERE deleted_at IS NULL AND status = 'ACTIVE'
            AND (expires_at IS NULL OR expires_at > :now)
            """, nativeQuery = true)
    long countActive(@Param("now") LocalDateTime now);

    @Query(value = """
            SELECT COUNT(*) FROM urls WHERE deleted_at IS NULL AND status = 'ACTIVE'
            AND expires_at IS NOT NULL AND expires_at <= :now
            """, nativeQuery = true)
    long countExpired(@Param("now") LocalDateTime now);
}
