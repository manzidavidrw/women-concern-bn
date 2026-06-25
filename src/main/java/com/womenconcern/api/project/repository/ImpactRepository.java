package com.womenconcern.api.project.repository;


import com.womenconcern.api.project.entity.Impact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ImpactRepository extends JpaRepository<Impact, UUID> {
    List<Impact> findByObjectiveId(UUID objectiveId);
}
