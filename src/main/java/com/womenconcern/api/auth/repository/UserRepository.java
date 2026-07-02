package com.womenconcern.api.auth.repository;

import com.womenconcern.api.auth.dto.UserDto;
import com.womenconcern.api.auth.entity.User;
import com.womenconcern.api.utils.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findAll(Specification<User> filter, Pageable pageable);
}
