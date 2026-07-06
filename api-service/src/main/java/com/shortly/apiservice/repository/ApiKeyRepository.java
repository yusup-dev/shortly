package com.shortly.apiservice.repository;

import com.shortly.apiservice.entity.ApiKey;
import com.shortly.apiservice.entity.User;
import com.shortly.apiservice.enumaration.KeyStatusType;
import com.shortly.apiservice.repository.projection.ApiKeyPlanProjection;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    @Query(
            value = """
       SELECT u.* FROM api_keys a LEFT JOIN users u ON u.id = a.user_id
       WHERE u.id = :userId AND a.status = :keyStatus
    """, nativeQuery = true
    )
    Optional<ApiKey> findByStatusActiveAndUserId(@Param("keyStatus") KeyStatusType keyStatus, @Param("userId") UUID userId);

    List<ApiKey> findByUser(User user);

    Optional<ApiKey> findByUserAndStatus(User user, KeyStatusType status);

}
