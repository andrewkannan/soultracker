package com.outreach.soultracker.service;

import com.outreach.soultracker.repository.SoulRepository;
import org.springframework.stereotype.Service;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;
import java.util.HashMap;

@Service
public class SoulService {

    private final SoulRepository soulRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public SoulService(SoulRepository soulRepository, SimpMessagingTemplate messagingTemplate) {
        this.soulRepository = soulRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public long getTotalCount() {
        return soulRepository.count();
    }

    public long getHealedCount() {
        return soulRepository.countByIsHealedTrue();
    }

    public long getPrayedCount() {
        return soulRepository.countByIsPrayedTrue();
    }

    public long getBaptizedCount() {
        return soulRepository.countByIsBaptizedTrue();
    }

    public long getPlantedCount() {
        return soulRepository.countByIsPlantedTrue();
    }

    public java.util.List<com.outreach.soultracker.entity.Soul> getAllSouls() {
        return soulRepository.findAll(org.springframework.data.domain.Sort
                .by(org.springframework.data.domain.Sort.Direction.DESC, "timestamp"));
    }

    public java.util.List<com.outreach.soultracker.entity.Soul> getSoulsByUser(
            com.outreach.soultracker.entity.AppUser user) {
        return soulRepository.findByCreatedByOrderByTimestampDesc(user);
    }

    public com.outreach.soultracker.entity.Soul getSoulById(Long id) {
        return soulRepository.findById(id).orElse(null);
    }

    public long countSoulsByUser(com.outreach.soultracker.entity.AppUser user) {
        return soulRepository.countByCreatedBy(user);
    }

    public long countHealedByUser(com.outreach.soultracker.entity.AppUser user) {
        return soulRepository.countByCreatedByAndIsHealedTrue(user);
    }

    public long countPrayedByUser(com.outreach.soultracker.entity.AppUser user) {
        return soulRepository.countByCreatedByAndIsPrayedTrue(user);
    }

    public long countBaptizedByUser(com.outreach.soultracker.entity.AppUser user) {
        return soulRepository.countByCreatedByAndIsBaptizedTrue(user);
    }

    public long countPlantedByUser(com.outreach.soultracker.entity.AppUser user) {
        return soulRepository.countByCreatedByAndIsPlantedTrue(user);
    }

    public void saveSoul(com.outreach.soultracker.entity.Soul soul) {
        boolean isNewSoul = (soul.getId() == null);
        if (soul.getTimestamp() == null) {
            soul.setTimestamp(java.time.LocalDateTime.now());
        }

        // Check user counts before save
        long previousCount = 0;
        int previousHealed = 0;
        int previousBaptized = 0;
        int previousPrayedFor = 0;
        int previousPlanted = 0;

        if (soul.getCreatedBy() != null) {
            previousCount = countSoulsByUser(soul.getCreatedBy());
            previousHealed = (int) countHealedByUser(soul.getCreatedBy());
            previousBaptized = (int) countBaptizedByUser(soul.getCreatedBy());
            previousPrayedFor = (int) countPrayedByUser(soul.getCreatedBy());
            previousPlanted = (int) countPlantedByUser(soul.getCreatedBy());
        }

        soulRepository.save(soul);

        // Check user counts after save for milestones
        if (soul.getCreatedBy() != null) {
            long newCount = countSoulsByUser(soul.getCreatedBy());
            checkMilestones(soul.getCreatedBy().getUsername(), previousCount, newCount);

            if (isNewSoul) {
                int newHealed = (int) countHealedByUser(soul.getCreatedBy());
                int newBaptized = (int) countBaptizedByUser(soul.getCreatedBy());
                int newPrayedFor = (int) countPrayedByUser(soul.getCreatedBy());
                int newPlanted = (int) countPlantedByUser(soul.getCreatedBy());

                checkSpecialistMilestones(soul.getCreatedBy().getUsername(),
                        previousHealed, newHealed,
                        previousBaptized, newBaptized,
                        previousPrayedFor, newPrayedFor,
                        previousPlanted, newPlanted);
            }
        }
    }

    private void checkMilestones(String username, long previousCount, long newCount) {
        int[] milestones = { 1, 10, 50, 100 };
        String[] ranks = { "Bronze Initiate", "Gold Disciple", "Platinum Ambassador", "Legendary Reaper" };

        for (int i = 0; i < milestones.length; i++) {
            if (previousCount < milestones[i] && newCount >= milestones[i]) {
                // Milestone unlocked!
                Map<String, String> payload = new HashMap<>();
                payload.put("type", "RANK");
                payload.put("rank", ranks[i]);
                payload.put("milestone", String.valueOf(milestones[i]));
                messagingTemplate.convertAndSend("/topic/achievements/" + username, payload);
            }
        }
    }

    private void checkSpecialistMilestones(String username,
            int prevHealed, int newHealed,
            int prevBaptized, int newBaptized,
            int prevPrayedFor, int newPrayedFor,
            int prevPlanted, int newPlanted) {

        // The Healer
        checkSingleSpecialist(username, prevHealed, newHealed, 5, "Gift of Mercy");
        checkSingleSpecialist(username, prevHealed, newHealed, 20, "Healing Hand");
        checkSingleSpecialist(username, prevHealed, newHealed, 50, "Divine Catalyst");

        // The Baptist
        checkSingleSpecialist(username, prevBaptized, newBaptized, 5, "River Guide");
        checkSingleSpecialist(username, prevBaptized, newBaptized, 20, "Kingdom Gatekeeper");
        checkSingleSpecialist(username, prevBaptized, newBaptized, 50, "Living Water Envoy");

        // The Intercessor
        checkSingleSpecialist(username, prevPrayedFor, newPrayedFor, 10, "Prayer Warrior");
        checkSingleSpecialist(username, prevPrayedFor, newPrayedFor, 50, "Spiritual Pillar");
        checkSingleSpecialist(username, prevPrayedFor, newPrayedFor, 100, "Shield of Faith");

        // The Harvester
        checkSingleSpecialist(username, prevPlanted, newPlanted, 5, "Seed Sower");
        checkSingleSpecialist(username, prevPlanted, newPlanted, 10, "Fruit Bearer");
        checkSingleSpecialist(username, prevPlanted, newPlanted, 20, "Master Harvester");
    }

    private void checkSingleSpecialist(String username, int previous, int current, int required, String badgeName) {
        if (previous < required && current >= required) {
            Map<String, String> payload = new HashMap<>();
            payload.put("type", "SPECIALIST");
            payload.put("badge", badgeName);
            payload.put("requirement", String.valueOf(required));
            messagingTemplate.convertAndSend("/topic/achievements/" + username, payload);
        }
    }
}
