package com.womenconcern.api.config;

import com.womenconcern.api.auth.entity.User;
import com.womenconcern.api.auth.enums.Gender;
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
        seedAdmin();
        seedFinance();
        seedFieldOfficer();
    }

    private void seedAdmin() {
        String email = "admin@womenconcern.com";

        if (userRepository.existsByEmail(email)) {
            log.info("Admin already exists — skipping seed.");
            return;
        }

        User admin = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("Admin@1234!"))
                .firstName("System")
                .lastName("Admin")
                .gender(Gender.MALE)
                .role(UserRole.ADMIN)
                .isActive(true)
                .mustChangePassword(false)
                .joinedAt(LocalDate.now())
                .build();

        userRepository.save(admin);
        log.info("✅ Default admin seeded: {}", email);
    }

    private void seedFinance() {
        String email = "finance@womenconcern.com";

        if (userRepository.existsByEmail(email)) {
            log.info("Finance user already exists — skipping seed.");
            return;
        }

        User finance = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("Password@123"))
                .firstName("Finance")
                .lastName("User")
                .gender(Gender.FEMALE)
                .role(UserRole.FINANCE)
                .isActive(true)
                .mustChangePassword(false)
                .joinedAt(LocalDate.now())
                .build();

        userRepository.save(finance);
        log.info("✅ Finance user seeded: {}", email);
    }

    private void seedFieldOfficer() {
        String email = "field.office@womenconcern.com";

        if (userRepository.existsByEmail(email)) {
            log.info("Field Officer already exists — skipping seed.");
            return;
        }

        User fieldOfficer = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("Password@123"))
                .firstName("Field")
                .lastName("Officer")
                .gender(Gender.MALE)
                .role(UserRole.FIELD_OFFICER)
                .isActive(true)
                .mustChangePassword(false)
                .joinedAt(LocalDate.now())
                .build();

        userRepository.save(fieldOfficer);
        log.info("✅ Field Officer seeded: {}", email);
    }
}