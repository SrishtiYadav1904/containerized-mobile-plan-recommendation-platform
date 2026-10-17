package com.mpr.platform.repository;

import com.mpr.platform.model.RecommendationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationResultRepository extends JpaRepository<RecommendationResult, Long> {

    List<RecommendationResult> findByRequestIdOrderByRankAsc(Long requestId);
}
