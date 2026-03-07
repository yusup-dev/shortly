package com.shortly.apiservice.repository;

import com.shortly.apiservice.entity.Plan;
import com.shortly.apiservice.enumaration.PlanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    Optional<Plan> findByName(PlanType name);
}
