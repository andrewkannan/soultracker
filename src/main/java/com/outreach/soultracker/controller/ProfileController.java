package com.outreach.soultracker.controller;

import com.outreach.soultracker.entity.AppUser;
import com.outreach.soultracker.repository.UserRepository;
import com.outreach.soultracker.service.SoulService;
import com.outreach.soultracker.service.PosterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SoulService soulService;

    @Autowired
    private PosterService posterService;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @GetMapping("/share/poster/{milestone}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<byte[]> getSharePoster(@PathVariable int milestone,
            @RequestParam String rankName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .build();
        }

        AppUser user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }

        try {
            byte[] posterData = posterService.generateAchievementPoster(user, rankName, milestone);
            return org.springframework.http.ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.IMAGE_PNG)
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"achievement.png\"")
                    .body(posterData);
        } catch (Exception e) {
            e.printStackTrace();
            return org.springframework.http.ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public String showProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }

        String username = auth.getName();
        AppUser user = userRepository.findByEmail(username).orElse(null);

        if (user != null) {
            model.addAttribute("user", user);
            long userSouls = soulService.countSoulsByUser(user);
            model.addAttribute("userTotalSouls", userSouls);
            model.addAttribute("userTotalHealed", soulService.countHealedByUser(user));
            model.addAttribute("baptizedCount", soulService.getBaptizedCount());
            model.addAttribute("plantedCount", soulService.getPlantedCount());
            model.addAttribute("userTotalPrayedFor", soulService.countPrayedByUser(user));
            model.addAttribute("userTotalPlanted", soulService.countPlantedByUser(user));

            int nextMilestone = getNextMilestone(userSouls);
            model.addAttribute("nextMilestone", nextMilestone);
            model.addAttribute("progressPercent", calculateProgress(userSouls, nextMilestone));
        }

        return "profile";
    }

    @PostMapping("/avatar")
    public String uploadAvatar(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Please select a file to upload.");
            return "redirect:/profile";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        AppUser user = userRepository.findByEmail(username).orElse(null);

        if (user != null) {
            try {
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String originalFilename = file.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }

                String newFilename = UUID.randomUUID().toString() + fileExtension;
                Path filePath = uploadPath.resolve(newFilename);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                user.setAvatarUrl("/uploads/" + newFilename);
                userRepository.save(user);

                redirectAttributes.addFlashAttribute("successMsg", "Profile picture updated successfully!");

            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("errorMsg", "Could not upload the file: " + e.getMessage());
            }
        }

        return "redirect:/profile";
    }

    private int getNextMilestone(long current) {
        if (current < 1)
            return 1;
        if (current < 10)
            return 10;
        if (current < 20)
            return 20;
        if (current < 30)
            return 30;
        if (current < 40)
            return 40;
        if (current < 50)
            return 50;
        if (current < 100)
            return 100;
        return 100;
    }

    private int calculateProgress(long current, int nextMilestone) {
        if (current >= 100)
            return 100;
        int previousMilestone = 0;
        if (nextMilestone == 1)
            previousMilestone = 0;
        else if (nextMilestone == 10)
            previousMilestone = 1;
        else if (nextMilestone == 20)
            previousMilestone = 10;
        else if (nextMilestone == 30)
            previousMilestone = 20;
        else if (nextMilestone == 40)
            previousMilestone = 30;
        else if (nextMilestone == 50)
            previousMilestone = 40;
        else if (nextMilestone == 100)
            previousMilestone = 50;

        long range = nextMilestone - previousMilestone;
        long progress = current - previousMilestone;
        return (int) ((progress * 100) / range);
    }
}
