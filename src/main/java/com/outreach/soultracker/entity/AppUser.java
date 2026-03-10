package com.outreach.soultracker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import java.util.List;
import java.util.ArrayList;

@Entity
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String fullName;
    private String email;
    private String password;

    // Branches: JB, IP, SR, MELAKA, TD
    private String branch;

    // Roles: ROLE_ADMIN, ROLE_EVANGELIST
    private String role;

    private String avatarUrl;
    @Column(nullable = true)
    private Boolean enabled = true;

    // Specialist Achievement Trackers
    private Integer totalHealed = 0;
    private Integer totalBaptized = 0;
    private Integer totalPrayedFor = 0;
    private Integer totalPlanted = 0;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Authenticator> authenticators = new ArrayList<>();

    public AppUser() {
    }

    public AppUser(String username, String fullName, String email, String password, String branch, String role) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.branch = branch;
        this.role = role;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Integer getTotalHealed() {
        if (totalHealed == null)
            return 0;
        return totalHealed;
    }

    public void setTotalHealed(Integer totalHealed) {
        this.totalHealed = totalHealed;
    }

    public Integer getTotalBaptized() {
        if (totalBaptized == null)
            return 0;
        return totalBaptized;
    }

    public void setTotalBaptized(Integer totalBaptized) {
        this.totalBaptized = totalBaptized;
    }

    public Integer getTotalPrayedFor() {
        if (totalPrayedFor == null)
            return 0;
        return totalPrayedFor;
    }

    public void setTotalPrayedFor(Integer totalPrayedFor) {
        this.totalPrayedFor = totalPrayedFor;
    }

    public Integer getTotalPlanted() {
        if (totalPlanted == null)
            return 0;
        return totalPlanted;
    }

    public void setTotalPlanted(Integer totalPlanted) {
        this.totalPlanted = totalPlanted;
    }

    public List<Authenticator> getAuthenticators() {
        return authenticators;
    }

    public void setAuthenticators(List<Authenticator> authenticators) {
        this.authenticators = authenticators;
    }

    public boolean isEnabled() {
        return enabled == null || enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
