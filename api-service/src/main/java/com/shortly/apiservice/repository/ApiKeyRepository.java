package com.shortly.apiservice.repository;

import com.shortly.apiservice.entity.ApiKey;
import com.shortly.apiservice.enumaration.KeyStatusType;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

    @Query(
            value = """
       SELECT u.* FROM api_keys a LEFT JOIN users u ON u.id = a.user_id 
       WHERE u.id = :userId AND a.status = :keyStatus
    """, nativeQuery = true
    )
    Optional<ApiKey> findByStatusActiveAndUserId(@Param("keyStatus") KeyStatusType keyStatus, @Param("userId") UUID userId);


}
