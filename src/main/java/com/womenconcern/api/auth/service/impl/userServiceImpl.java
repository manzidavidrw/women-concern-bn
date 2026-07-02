package com.womenconcern.api.auth.service.impl;

import com.womenconcern.api.auth.dto.UserDto;
import com.womenconcern.api.auth.entity.User;
import com.womenconcern.api.auth.repository.UserRepository;
import com.womenconcern.api.auth.service.UserService;
import com.womenconcern.api.exception.BadRequestException;
import com.womenconcern.api.exception.ResourceNotFoundException;
import com.womenconcern.api.utils.PageResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.criteria.Predicate;

@Service
@RequiredArgsConstructor
public class userServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto.FullUser getUserById(UUID id) {
        User user =  userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return mapToFullUser(user);

    }

    @Override
    public PageResponse<UserDto.FullUser> getAllUsers(UserDto.UserFilter filter,Pageable pageable) {
        Page<User> users = userRepository.findAll(
                filter(filter),
                pageable
        );

        Page<UserDto.FullUser> mapped = users.map(this::mapToFullUser);
        return new PageResponse<>(
                mapped.getContent(),
                mapped.getNumber(),
                mapped.getSize(),
                mapped.getTotalElements(),
                mapped.getTotalPages(),
                mapped.isFirst(),
                mapped.isLast()

        );
    }

    @Override
    @Transactional
    public void activateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (user.isActive()) {
            throw new BadRequestException("User is already active.");
        }

        user.setActive(true);
    }

    @Override
    @Transactional
    public void deactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (!user.isActive()) {
            throw new BadRequestException("User is already inactive.");
        }

        user.setActive(false);
    }

    @Override
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        userRepository.delete(user);
    }

    public static Specification<User> filter(UserDto.UserFilter filter) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter.search() != null && !filter.search().isBlank()) {
                String search = "%" + filter.search().toLowerCase() + "%";

                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("email")), search),
                                cb.like(cb.lower(root.get("firstName")), search),
                                cb.like(cb.lower(root.get("lastName")), search)
                        )
                );
            }

            if (filter.active() != null) {
                predicates.add(cb.equal(root.get("isActive"), filter.active()));
            }

            if (filter.gender() != null) {
                predicates.add(cb.equal(root.get("gender"), filter.gender()));
            }

            if (filter.role() != null) {
                predicates.add(cb.equal(root.get("role"), filter.role()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


    private UserDto.FullUser mapToFullUser(User user) {
        return new UserDto.FullUser(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getJobTitle(),
                user.getAddress(),
                user.getProfilePictureUrl(),
                user.getProfilePictureId(),
                user.getNationalId(),
                user.getEmergencyContact(),
                user.getDateOfBirth(),
                user.getGender(),
                user.getCertificates(),
                user.getJoinedAt(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
