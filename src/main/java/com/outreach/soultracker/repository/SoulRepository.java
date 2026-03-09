package com.outreach.soultracker.repository;

import com.outreach.soultracker.entity.Soul;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoulRepository extends JpaRepository<Soul, Long> {
    java.util.List<Soul> findByCreatedByOrderByTimestampDesc(com.outreach.soultracker.entity.AppUser user);

    long countByIsHealedTrue();

    long countByIsPrayedTrue();

    long countByIsBaptizedTrue();

    long countByIsPlantedTrue();

    long countByCreatedBy(com.outreach.soultracker.entity.AppUser user);

    long countByCreatedByAndIsHealedTrue(com.outreach.soultracker.entity.AppUser user);

    long countByCreatedByAndIsPrayedTrue(com.outreach.soultracker.entity.AppUser user);

    long countByCreatedByAndIsBaptizedTrue(com.outreach.soultracker.entity.AppUser user);

    long countByCreatedByAndIsPlantedTrue(com.outreach.soultracker.entity.AppUser user);
}
