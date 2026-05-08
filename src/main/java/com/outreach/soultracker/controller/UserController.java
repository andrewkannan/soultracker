package com.outreach.soultracker.controller;

import com.outreach.soultracker.entity.AppUser;
import com.outreach.soultracker.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Controller
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SimpMessagingTemplate messagingTemplate;
    private final com.outreach.soultracker.service.DynamicEmailService dynamicEmailService;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, SimpMessagingTemplate messagingTemplate, com.outreach.soultracker.service.DynamicEmailService dynamicEmailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.messagingTemplate = messagingTemplate;
        this.dynamicEmailService = dynamicEmailService;
    }


    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new AppUser());
        return "signup";
    }

    @PostMapping("/signup")
    @org.springframework.transaction.annotation.Transactional
    public String signupUser(@ModelAttribute AppUser user, Model model) {
        if (userRepository.findFirstByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("error", "Email already exists");
            return "signup";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_EVANGELIST");
        user.setBranch("JB");
        user.setEnabled(true);

        userRepository.saveAndFlush(user);

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            dynamicEmailService.sendSignupNotification(user.getEmail(), user.getFullName());
        }

        // Broadcast new daily count
        LocalDateTime startOfDay = LocalDateTime.now(ZoneId.systemDefault()).with(java.time.LocalTime.MIN);
        long newSignupsToday = userRepository.countByCreatedAtAfter(startOfDay);
        System.out.println("USER_SIGNUP_FLOW: startOfDay=" + startOfDay + ", count=" + newSignupsToday);
        messagingTemplate.convertAndSend("/topic/userscore", String.valueOf(newSignupsToday));

        return "redirect:/login?registered=true";
    }

    @GetMapping("/admin/signup-dashboard")
    public String showAdminSignupDashboard(Model model) {
        LocalDateTime startOfDay = LocalDateTime.now(ZoneId.systemDefault()).with(java.time.LocalTime.MIN);
        long signupsToday = userRepository.countByCreatedAtAfter(startOfDay);
        model.addAttribute("signupsToday", signupsToday);
        return "admin-signup";
    }
}
