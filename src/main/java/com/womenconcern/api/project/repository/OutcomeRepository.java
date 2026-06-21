package com.womenconcern.api.project.repository;


import com.womenconcern.api.project.entity.Outcome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutcomeRepository extends JpaRepository<Outcome, UUID> {
    List<Outcome> findByGoalId(UUID goalId);

}
