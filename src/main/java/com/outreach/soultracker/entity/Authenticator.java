package com.outreach.soultracker.entity;

import jakarta.persistence.*;
import java.util.Arrays;

@Entity
@Table(name = "authenticators")
public class Authenticator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String credentialId;

    @Column(nullable = false, columnDefinition = "BYTEA")
    private byte[] publicKey;

    @Column(nullable = false)
    private long signCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column
    private String name; // e.g., "iPhone 15", "Android Pixel"

    public Authenticator() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public long getSignCount() {
        return signCount;
    }

    public void setSignCount(long signCount) {
        this.signCount = signCount;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
