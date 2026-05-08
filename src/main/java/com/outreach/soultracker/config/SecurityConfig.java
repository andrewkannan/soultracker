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
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final com.outreach.soultracker.service.CustomUserDetailsService customUserDetailsService;
        private final TwoFactorAuthenticationFilter twoFactorAuthenticationFilter;

        public SecurityConfig(com.outreach.soultracker.service.CustomUserDetailsService customUserDetailsService,
                              TwoFactorAuthenticationFilter twoFactorAuthenticationFilter) {
                this.customUserDetailsService = customUserDetailsService;
                this.twoFactorAuthenticationFilter = twoFactorAuthenticationFilter;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable()) // Disable CSRF for easier WebAuthn integration (not ideal
                                                              // for production, but simpler for demo/mobile)
                                .authorizeHttpRequests((requests) -> requests
                                                .requestMatchers("/", "/css/**", "/js/**", "/favicon.png", "/og-1.png", "/ws/**",
                                                                "/error", "/webauthn/**", "/signup", "/login")
                                                .permitAll()
                                                .requestMatchers("/users/**", "/adduser", "/admin/**").hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .formLogin((form) -> form
                                                .loginPage("/login")
                                                .permitAll()
                                                .defaultSuccessUrl("/", true))
                                .addFilterAfter(twoFactorAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                                .rememberMe((remember) -> remember
                                                .key("uniqueAndSecret")
                                                .tokenValiditySeconds(86400) // 1 day
                                                .rememberMeParameter("remember-me")
                                                .useSecureCookie(true))
                                .logout((logout) -> logout.permitAll())
                                .userDetailsService(customUserDetailsService);

                return http.build();
        }

        @Bean
        public ApplicationRunner initializer(UserRepository userRepository, PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate) {
                return args -> {
                        try {
                                jdbcTemplate.execute("ALTER TABLE app_user DROP COLUMN IF EXISTS username");
                        } catch (Exception e) {
                                System.out.println("Could not drop username column: " + e.getMessage());
                        }

                        userRepository.findFirstByEmail("admin@soultracker.local").ifPresentOrElse(
                                        admin -> {
                                                if (!admin.isEnabled()) {
                                                        admin.setEnabled(true);
                                                }
                                                // Force reset password to ensure access
                                                admin.setPassword(passwordEncoder.encode("admin123"));
                                                userRepository.save(admin);
                                        },
                                        () -> {
                                                AppUser admin = new AppUser(
                                                                "System Administrator",
                                                                "admin@soultracker.local",
                                                                passwordEncoder.encode("admin123"),
                                                                "HQ",
                                                                "ROLE_ADMIN");
                                                admin.setEnabled(true);
                                                userRepository.save(admin);
                                        });
                };
        }
}
