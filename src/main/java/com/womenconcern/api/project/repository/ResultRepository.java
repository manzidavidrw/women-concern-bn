package com.womenconcern.api.project.repository;


import com.womenconcern.api.project.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResultRepository extends JpaRepository<Result, UUID> {
    List<Result> findByOutcomeId(UUID outcomeId);
}
