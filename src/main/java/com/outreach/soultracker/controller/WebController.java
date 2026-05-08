package com.outreach.soultracker.controller;

import com.outreach.soultracker.service.SoulService;
import com.outreach.soultracker.repository.UserRepository;
import com.outreach.soultracker.entity.AppUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.messaging.simp.SimpMessagingTemplate;

@Controller
public class WebController {

    private final SoulService soulService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public WebController(SoulService soulService, UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.soulService = soulService;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("totalCount", soulService.getTotalCount());
        model.addAttribute("healedCount", soulService.getHealedCount());
        model.addAttribute("prayedCount", soulService.getPrayedCount());
        model.addAttribute("baptizedCount", soulService.getBaptizedCount());
        model.addAttribute("plantedCount", soulService.getPlantedCount());

        return "home";
    }

    @GetMapping("/add")
    public String add(Model model) {
        model.addAttribute("soul", new com.outreach.soultracker.entity.Soul());
        model.addAttribute("allUsers", userRepository.findAll());
        return "add";
    }

    @org.springframework.web.bind.annotation.PostMapping("/add")
    public String submitAdd(
            @org.springframework.web.bind.annotation.ModelAttribute com.outreach.soultracker.entity.Soul soul,
            java.security.Principal principal) {

        if (principal != null) {
            userRepository.findByEmail(principal.getName()).ifPresent(soul::setCreatedBy);
        }
        soulService.saveSoul(soul);

        // Broadcast the updated count to all connected clients
        java.util.Map<String, Long> statPayload = new java.util.HashMap<>();
        statPayload.put("total", soulService.getTotalCount());
        statPayload.put("healed", soulService.getHealedCount());
        statPayload.put("prayed", soulService.getPrayedCount());
        statPayload.put("baptized", soulService.getBaptizedCount());
        statPayload.put("planted", soulService.getPlantedCount());

        messagingTemplate.convertAndSend("/topic/livescore", statPayload);

        return "redirect:/";
    }

    @GetMapping("/api/debug/souls")
    @ResponseBody
    public String debugSouls() {
        AppUser admin = userRepository.findByEmail("admin@soultracker.local").orElse(null);
        if (admin != null) {
            com.outreach.soultracker.entity.Soul testSoul = new com.outreach.soultracker.entity.Soul(
                    "Test User", "Test Loc", "123", "Test testimony", java.time.LocalDateTime.now());
            testSoul.setIsPrayed(true);
            testSoul.setIsBaptized(true);
            testSoul.setCreatedBy(admin);
            soulService.saveSoul(testSoul);

            return "Soul created with isPrayed=true and isBaptized=true. Admin now has Prayed: "
                    + admin.getTotalPrayedFor() + ", Baptized: " + admin.getTotalBaptized();
        }
        return "Admin user not found";
    }

    @GetMapping("/entries")
    public String viewEntries(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            model.addAttribute("souls", soulService.getAllSouls());
        } else {
            String username = auth.getName();
            AppUser user = userRepository.findByEmail(username).orElse(null);
            model.addAttribute("souls", soulService.getSoulsByUser(user));
        }

        java.util.List<AppUser> allUsers = userRepository.findAll();
        java.util.List<String> allBranches = allUsers.stream()
                .map(AppUser::getBranch)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        model.addAttribute("allUsers", allUsers);
        model.addAttribute("allBranches", allBranches);
        return "entries";
    }

    @GetMapping("/edit/{id}")
    public String edit(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        com.outreach.soultracker.entity.Soul soul = soulService.getSoulById(id);
        if (soul == null) {
            return "redirect:/entries";
        }

        if (!isAdmin && (soul.getCreatedBy() == null || !soul.getCreatedBy().getEmail().equals(auth.getName()))) {
            return "redirect:/entries"; // Prevent unauthorized edits
        }

        model.addAttribute("soul", soul);
        model.addAttribute("allUsers", userRepository.findAll());
        model.addAttribute("isAdmin", isAdmin);
        return "edit";
    }

    @org.springframework.web.bind.annotation.PostMapping("/edit/{id}")
    public String submitEdit(@org.springframework.web.bind.annotation.PathVariable Long id,
            @org.springframework.web.bind.annotation.ModelAttribute com.outreach.soultracker.entity.Soul soul) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        com.outreach.soultracker.entity.Soul existing = soulService.getSoulById(id);
        if (existing == null)
            return "redirect:/entries";

        if (!isAdmin
                && (existing.getCreatedBy() == null || !existing.getCreatedBy().getEmail().equals(auth.getName()))) {
            return "redirect:/entries";
        }

        // The ID is kept in the path and bound to the object
        soul.setId(id);

        // Retain original timestamp if the form did not pass it
        if (soul.getTimestamp() == null) {
            soul.setTimestamp(existing.getTimestamp());
        }
        // Retain original createdBy
        soul.setCreatedBy(existing.getCreatedBy());

        // Secure advanced fields: only Admin can change Status, Action, Assignment
        if (!isAdmin) {
            soul.setStatus(existing.getStatus());
            soul.setNextAction(existing.getNextAction());
            soul.setAssignedTo(existing.getAssignedTo());
        }

        // New fields should bind via @ModelAttribute if present in form,
        // but we explicitly mention them here for clarity in logic flow.
        // The Soul object 'soul' already has these fields populated from the form.

        soulService.saveSoul(soul);
        return "redirect:/entries";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
