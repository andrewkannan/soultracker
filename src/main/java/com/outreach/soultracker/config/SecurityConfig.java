package com.outreach.soultracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.ApplicationRunner;
import com.outreach.soultracker.entity.AppUser;
import com.outreach.soultracker.repository.UserRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final com.outreach.soultracker.service.CustomUserDetailsService customUserDetailsService;

        public SecurityConfig(com.outreach.soultracker.service.CustomUserDetailsService customUserDetailsService) {
                this.customUserDetailsService = customUserDetailsService;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests((requests) -> requests
                                                .requestMatchers("/", "/css/**", "/js/**", "/favicon.png", "/ws/**",
                                                                "/error")
                                                .permitAll()
                                                .requestMatchers("/users/**", "/adduser").hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .formLogin((form) -> form
                                                .loginPage("/login")
                                                .permitAll()
                                                .defaultSuccessUrl("/", true))
                                .logout((logout) -> logout.permitAll())
                                .userDetailsService(customUserDetailsService);

                return http.build();
        }

        @Bean
        public ApplicationRunner initializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
                return args -> {
                        if (userRepository.count() == 0) {
                                AppUser admin = new AppUser(
                                                "admin",
                                                "System Administrator",
                                                "admin@soultracker.local",
                                                passwordEncoder.encode("password"),
                                                "HQ",
                                                "ROLE_ADMIN");
                                userRepository.save(admin);
                        }
                };
        }
}
