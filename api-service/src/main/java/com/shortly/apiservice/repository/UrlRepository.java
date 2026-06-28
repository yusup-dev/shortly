package com.shortly.apiservice.repository;

import com.shortly.apiservice.entity.Url;
import com.shortly.apiservice.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public interface UrlRepository extends JpaRepository<Url, UUID> {

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
}
