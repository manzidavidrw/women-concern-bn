package com.womenconcern.api.config;

import com.womenconcern.api.auth.entity.User;
import com.womenconcern.api.auth.enums.UserRole;
import com.womenconcern.api.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        String adminEmail = "admin@womenconcern.com";

        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin already exists — skipping seed.");
            return;
        }

        User admin = User.builder()
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode("Admin@1234!"))
                .firstName("System")
                .lastName("Admin")
                .role(UserRole.ADMIN)
                .isActive(true)
                .joinedAt(LocalDate.now())
                .build();

        userRepository.save(admin);
        log.info("✅ Default admin seeded: {}", adminEmail);
    }
}