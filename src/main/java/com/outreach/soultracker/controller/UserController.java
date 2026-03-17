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

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "users";
    }

    @GetMapping("/adduser")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new AppUser());
        return "addUser";
    }

    @PostMapping("/adduser")
    public String addUser(@ModelAttribute AppUser user) {
        // Hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Ensure role is prefixed correctly if not from a dropdown
        if (!user.getRole().startsWith("ROLE_")) {
            user.setRole("ROLE_" + user.getRole());
        }

        userRepository.save(user);
        return "redirect:/users";
    }

    @PostMapping("/edituser")
    public String editUser(@ModelAttribute AppUser updatedUser) {
        AppUser existingUser = userRepository.findById(updatedUser.getId()).orElse(null);
        if (existingUser != null) {
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setFullName(updatedUser.getFullName());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setBranch(updatedUser.getBranch());

            // Check Role Prefix
            if (updatedUser.getRole() != null) {
                if (!updatedUser.getRole().startsWith("ROLE_")) {
                    existingUser.setRole("ROLE_" + updatedUser.getRole());
                } else {
                    existingUser.setRole(updatedUser.getRole());
                }
            }

            // Handle Password Change
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().trim().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            } // Otherwise leave the existing password alone

            // Account Status
            existingUser.setEnabled(updatedUser.isEnabled());

            userRepository.save(existingUser);
        }
        return "redirect:/users";
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new AppUser());
        return "signup";
    }

    @PostMapping("/signup")
    public String signupUser(@ModelAttribute AppUser user, Model model) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Username already exists");
            return "signup";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_EVANGELIST");
        user.setBranch("JB");
        user.setEnabled(true);

        userRepository.save(user);

        // Broadcast new daily count
        LocalDateTime startOfDay = LocalDateTime.now(ZoneId.systemDefault()).with(java.time.LocalTime.MIN);
        long newSignupsToday = userRepository.countByCreatedAtAfter(startOfDay);
        messagingTemplate.convertAndSend("/topic/userscore", newSignupsToday);

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
