package com.shortly.apiservice.repository;

import com.shortly.apiservice.entity.Quota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuotaRepository extends JpaRepository<Quota, UUID> {
}
