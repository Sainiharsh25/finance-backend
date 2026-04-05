package com.finance.backend.config;

import com.finance.backend.entity.User;
import com.finance.backend.enums.Role;
import com.finance.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdminUser();
    }

    private void seedAdminUser() {
        if (!userRepository.existsByEmail("admin@finance.com")) {
            User admin = User.builder()
                    .email("admin@finance.com")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("System Admin")
                    .role(Role.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
            log.info("Default admin created: admin@finance.com / admin123");
        }

        if (!userRepository.existsByEmail("analyst@finance.com")) {
            User analyst = User.builder()
                    .email("analyst@finance.com")
                    .password(passwordEncoder.encode("analyst123"))
                    .fullName("Default Analyst")
                    .role(Role.ANALYST)
                    .active(true)
                    .build();
            userRepository.save(analyst);
            log.info("Default analyst created: analyst@finance.com / analyst123");
        }

        if (!userRepository.existsByEmail("viewer@finance.com")) {
            User viewer = User.builder()
                    .email("viewer@finance.com")
                    .password(passwordEncoder.encode("viewer123"))
                    .fullName("Default Viewer")
                    .role(Role.VIEWER)
                    .active(true)
                    .build();
            userRepository.save(viewer);
            log.info("Default viewer created: viewer@finance.com / viewer123");
        }
    }
}
