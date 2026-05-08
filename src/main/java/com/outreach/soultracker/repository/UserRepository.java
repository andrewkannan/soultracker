package com.outreach.soultracker.repository;

import com.outreach.soultracker.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findFirstByEmail(String email);
    long countByCreatedAtAfter(java.time.LocalDateTime startOfDay);
}
