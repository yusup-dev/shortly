package com.shortly.apiservice.repository;

import com.shortly.apiservice.entity.Role;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query(value = """
       SELECT r.* FROM roles r LEFT JOIN users u ON r.id = u.role_id WHERE u.id = :userId
    """, nativeQuery = true)
    Optional<Role> findByUserId(@Param("userId") UUID userId);
}
