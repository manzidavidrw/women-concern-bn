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
public class Seeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {

        seedUser("admin@womenconcern.com", "Admin@1234!", "System", "Admin", UserRole.ADMIN);
        seedUser("pm@womenconcern.com", "Pm@1234!", "Default", "Manager", UserRole.PROJECT_MANAGER);
        seedUser("director@womenconcern.com", "Director@1234!", "Executive", "Director", UserRole.EXECUTIVE_DIRECTOR);
        seedUser("finance@womenconcern.com", "Finance@1234!", "Default", "Finance", UserRole.FINANCE);
        seedUser("manzi2020d@gmail.com", "Field@1234!", "Default", "FieldOfficer", UserRole.FIELD_OFFICER);
    }

    private void seedUser(String email, String password, String firstName, String lastName, UserRole role) {
        if (!userRepository.existsByEmail(email)) {
            User user = User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(password))
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(role)
                    .gender(Gender.MALE)
                    .isActive(true)
                    .joinedAt(LocalDate.now())
                    .build();
            userRepository.save(user);
            log.info("✅ Seeded [{}]: {}", role, email);
        } else {
            log.info("⏭️ Already exists — skipping [{}]: {}", role, email);
        }
    }
}