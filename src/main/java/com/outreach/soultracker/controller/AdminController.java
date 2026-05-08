package com.outreach.soultracker.controller;

import com.outreach.soultracker.entity.AppUser;
import com.outreach.soultracker.entity.EmailConfig;
import com.outreach.soultracker.entity.EmailTemplate;
import com.outreach.soultracker.entity.Event;
import com.outreach.soultracker.repository.EmailConfigRepository;
import com.outreach.soultracker.repository.EmailTemplateRepository;
import com.outreach.soultracker.repository.UserRepository;
import com.outreach.soultracker.service.DynamicEmailService;
import com.outreach.soultracker.service.EventService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailConfigRepository emailConfigRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final DynamicEmailService dynamicEmailService;
    private final EventService eventService;

    public AdminController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           EmailConfigRepository emailConfigRepository, EmailTemplateRepository emailTemplateRepository,
                           DynamicEmailService dynamicEmailService, EventService eventService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailConfigRepository = emailConfigRepository;
        this.emailTemplateRepository = emailTemplateRepository;
        this.dynamicEmailService = dynamicEmailService;
        this.eventService = eventService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.appendAttribute("totalUsers", userRepository.count());
        model.appendAttribute("totalEvents", eventService.getAllEvents().size());
        return "admin-dashboard";
    }

    // --- Users Management ---
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin-users";
    }

    @PostMapping("/users/add")
    public String addUser(@ModelAttribute AppUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (!user.getRole().startsWith("ROLE_")) {
            user.setRole("ROLE_" + user.getRole());
        }
        userRepository.save(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/edit")
    public String editUser(@ModelAttribute AppUser updatedUser) {
        AppUser existingUser = userRepository.findById(updatedUser.getId()).orElse(null);
        if (existingUser != null) {
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setFullName(updatedUser.getFullName());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
            existingUser.setBranch(updatedUser.getBranch());

            if (updatedUser.getRole() != null) {
                if (!updatedUser.getRole().startsWith("ROLE_")) {
                    existingUser.setRole("ROLE_" + updatedUser.getRole());
                } else {
                    existingUser.setRole(updatedUser.getRole());
                }
            }

            if (updatedUser.getPassword() != null && !updatedUser.getPassword().trim().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }

            existingUser.setEnabled(updatedUser.isEnabled());
            userRepository.save(existingUser);
        }
        return "redirect:/admin/users";
    }

    // --- Email Configuration ---
    @GetMapping("/email-config")
    public String emailConfig(Model model) {
        EmailConfig config = emailConfigRepository.findAll().stream().findFirst().orElse(new EmailConfig());
        model.addAttribute("config", config);
        return "admin-email-config";
    }

    @PostMapping("/email-config")
    public String saveEmailConfig(@ModelAttribute EmailConfig config, RedirectAttributes redirectAttributes) {
        emailConfigRepository.deleteAll();
        emailConfigRepository.save(config);
        redirectAttributes.addFlashAttribute("successMsg", "Email Configuration Saved Successfully!");
        return "redirect:/admin/email-config";
    }

    // --- Email Templates ---
    @GetMapping("/email-templates")
    public String emailTemplates(Model model) {
        model.addAttribute("templates", emailTemplateRepository.findAll());
        model.addAttribute("newTemplate", new EmailTemplate());
        return "admin-email-templates";
    }

    @PostMapping("/email-templates")
    public String saveEmailTemplate(@ModelAttribute EmailTemplate template) {
        emailTemplateRepository.save(template);
        return "redirect:/admin/email-templates";
    }

    // --- Bulk Email ---
    @GetMapping("/bulk-email")
    public String bulkEmailForm() {
        return "admin-bulk-email";
    }

    @PostMapping("/bulk-email/send")
    public String sendBulkEmail(@RequestParam String subject, @RequestParam String body, RedirectAttributes redirectAttributes) {
        List<AppUser> users = userRepository.findAll();
        int count = 0;
        for (AppUser user : users) {
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                dynamicEmailService.sendEmail(user.getEmail(), subject, body);
                count++;
            }
        }
        redirectAttributes.addFlashAttribute("successMsg", "Bulk email sent to " + count + " users.");
        return "redirect:/admin/bulk-email";
    }

    // --- Events Management ---
    @GetMapping("/events")
    public String listEvents(Model model) {
        model.addAttribute("events", eventService.getAllEvents());
        model.addAttribute("newEvent", new Event());
        return "admin-events";
    }

    @PostMapping("/events/add")
    public String addEvent(@ModelAttribute Event event) {
        eventService.saveEvent(event);
        return "redirect:/admin/events";
    }

    @PostMapping("/events/delete/{id}")
    public String deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return "redirect:/admin/events";
    }
}
