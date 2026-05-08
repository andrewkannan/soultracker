package com.outreach.soultracker.config;

import com.outreach.soultracker.entity.AppUser;
import com.outreach.soultracker.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TwoFactorAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public TwoFactorAuthenticationFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // Skip static resources, public endpoints, and the verification endpoint itself
        if (uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/favicon")
                || uri.startsWith("/login") || uri.startsWith("/signup") || uri.startsWith("/webauthn")
                || uri.startsWith("/2fa/verify")) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            AppUser user = userRepository.findByEmail(auth.getName()).orElse(null);
            if (user != null && user.isTwoFactorEnabled()) {
                Boolean passed2fa = (Boolean) request.getSession().getAttribute("passed_2fa");
                if (passed2fa == null || !passed2fa) {
                    response.sendRedirect("/2fa/verify");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
