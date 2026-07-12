package com.shortly.apiservice.repository;

import com.shortly.apiservice.entity.User;
import com.shortly.apiservice.enumaration.StatusType;
import com.shortly.apiservice.repository.projection.UserAuthProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    long countByDeletedAtIsNull();

    long countByStatusAndDeletedAtIsNull(StatusType status);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query(value = """
        SELECT 
            u.id,
            u.email,
            u.password,
            u.status,
            r.name AS roleName,
            p.name AS planName
        FROM users u
        JOIN roles r ON u.role_id = r.id
        JOIN plans p ON u.plan_id = p.id
        WHERE u.email = :email
    """, nativeQuery = true)
    Optional<UserAuthProjection> findAuthByEmail(@Param("email") String email);
}
