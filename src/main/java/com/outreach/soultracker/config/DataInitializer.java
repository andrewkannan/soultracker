package com.outreach.soultracker.config;

import com.outreach.soultracker.entity.AppUser;
import com.outreach.soultracker.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            String encodedPassword = passwordEncoder.encode("password");

            AppUser admin = new AppUser("admin", "Admin System", "admin@outreach.com", encodedPassword, "JB",
                    "ROLE_ADMIN");
            AppUser moses = new AppUser("moses", "Moses Lim", "moses@outreach.com", encodedPassword, "IP",
                    "ROLE_EVANGELIST");
            AppUser joshua = new AppUser("joshua", "Joshua Lee", "joshua@outreach.com", encodedPassword, "JB",
                    "ROLE_EVANGELIST");

            userRepository.save(admin);
            userRepository.save(moses);
            userRepository.save(joshua);

            System.out.println("Default users initialized successfully.");
        }
    }
}
