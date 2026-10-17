package com.mpr.platform.repository;

import com.mpr.platform.model.Plan;
import com.mpr.platform.model.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    List<Plan> findByStatus(PlanStatus status);

    List<Plan> findByOperatorIgnoreCase(String operator);

    List<Plan> findByStatusAndOperatorIgnoreCase(PlanStatus status, String operator);

    List<Plan> findByPriceLessThanEqual(Double maxPrice);

    List<Plan> findByStatusAndPriceLessThanEqual(PlanStatus status, Double maxPrice);

    long countByStatus(PlanStatus status);

    long countByOperatorIgnoreCase(String operator);
}
