package com.shortly.apiservice.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.shortly.apiservice.entity.ApiKey;
import com.shortly.apiservice.repository.projection.ApiKeyListProjection;
import com.shortly.apiservice.repository.projection.ApiKeyPlanProjection;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

    ApiKey findByKeyHash(String keyHash);

    @Query(value = """
        SELECT
            ak.key_hash AS keyHash,
            q.max_requests_per_day AS maxRequestsPerDay,
            q.max_urls_per_key AS maxUrlsPerKey,
            q.max_bulk AS maxBulk
        FROM api_keys ak
        JOIN quotas q ON q.api_key_id = ak.id
        WHERE ak.key_hash = :apiKey
        AND ak.status = 'ACTIVE'
        AND ak.expires_at > NOW()
        """, nativeQuery = true)
    Optional<ApiKeyPlanProjection> findLimitByApiKey(@Param("apiKey") String apiKey);

    Optional<ApiKey> findByIdAndUser_Id(UUID id, UUID userId);

    @Query(value = """
        SELECT
            ak.id AS id,
            ak.status AS status,
            ak.expires_at AS expiresAt,
            ak.created_at AS createdAt,
            q.max_requests_per_day AS maxRequestsPerDay,
            q.max_urls_per_key AS maxUrlsPerKey,
            q.max_bulk AS maxBulk
        FROM api_keys ak
        LEFT JOIN quotas q ON q.api_key_id = ak.id
        WHERE ak.user_id = :userId
        ORDER BY ak.created_at DESC
        """, nativeQuery = true)
    List<ApiKeyListProjection> findListByUserId(@Param("userId") UUID userId);
}
