package com.outreach.soultracker.repository;

import com.outreach.soultracker.entity.Authenticator;
import com.outreach.soultracker.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthenticatorRepository extends JpaRepository<Authenticator, Long> {
    List<Authenticator> findAllByUser(AppUser user);

    Optional<Authenticator> findByCredentialId(String credentialId);
}
