package com.shortly.apiservice.repository;

import com.shortly.apiservice.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UrlRepository extends JpaRepository<Url, UUID>, JpaSpecificationExecutor<Url> {

    boolean existsByShortKey(String shortKey);

    Optional<Url> findByShortKey(String shortKey);

    @Query(value = """
            SELECT COUNT(*)
            FROM urls u
            JOIN api_keys ak ON u.api_key_id = ak.id
            WHERE ak.key_hash = :apiKey
            AND u.deleted_at IS NULL
            """, nativeQuery = true)
    Long countByApiKeyHash(@Param("apiKey") String apiKey);

    Optional<Url> findByIdAndUser_Id(UUID id, UUID userId);

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

    @Query(value = """
            SELECT COUNT(*) FROM urls WHERE deleted_at IS NULL
            """, nativeQuery = true)
    long countAll();

    @Query(value = """
            SELECT COUNT(*) FROM urls WHERE deleted_at IS NULL AND status = 'SUSPENDED'
            """, nativeQuery = true)
    long countSuspended();

    @Query(value = """
            SELECT COUNT(*) FROM urls
            WHERE deleted_at IS NULL AND create_by_user_id = :userId
            """, nativeQuery = true)
    long countByUserId(@Param("userId") UUID userId);

    @Query(value = """
            SELECT create_by_user_id, COUNT(*)
            FROM urls
            WHERE deleted_at IS NULL AND create_by_user_id IN :userIds
            GROUP BY create_by_user_id
            """, nativeQuery = true)
    List<Object[]> countUrlsByUserIds(@Param("userIds") List<UUID> userIds);
}
