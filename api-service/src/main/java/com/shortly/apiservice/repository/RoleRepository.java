package com.shortly.apiservice.repository;

import com.shortly.apiservice.entity.Role;
import com.shortly.apiservice.enumaration.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleType name);
}
