package com.outreach.soultracker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import java.time.LocalDateTime;

@Entity
public class Soul {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private String phoneNumber;
    private String testimony;
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private AppUser createdBy;

    // Phase 3 Advanced Fields
    private SoulStatus status = SoulStatus.NEW;
    private String nextAction;

    @ManyToOne
    @JoinColumn(name = "assigned_to_id")
    private AppUser assignedTo;

    private Boolean isPriority = false;
    private Boolean isHealed = false;
    private Boolean isPrayed = false;
    private Boolean isBaptized = false;
    private Boolean isPlanted = false;

    // Discipleship & Form Enhancements
    private String homeChurch;
    private String ministry;
    private String visitationStatus = "Not Visited";
    private String visitedBy;

    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String mentorCaseNotes;

    private Boolean isServing = false;

    public Soul() {
    }

    public Soul(String name, String location, String phoneNumber, String testimony, LocalDateTime timestamp) {
        this.name = name;
        this.location = location;
        this.phoneNumber = phoneNumber;
        this.testimony = testimony;
        this.timestamp = timestamp;
        this.status = SoulStatus.NEW;
        this.isPriority = false;
        this.isHealed = false;
        this.isPrayed = false;
        this.isBaptized = false;
        this.isPlanted = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTestimony() {
        return testimony;
    }

    public void setTestimony(String testimony) {
        this.testimony = testimony;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public AppUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(AppUser createdBy) {
        this.createdBy = createdBy;
    }

    public SoulStatus getStatus() {
        return status;
    }

    public void setStatus(SoulStatus status) {
        this.status = status;
    }

    public String getNextAction() {
        return nextAction;
    }

    public void setNextAction(String nextAction) {
        this.nextAction = nextAction;
    }

    public AppUser getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(AppUser assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Boolean getIsPriority() {
        return isPriority;
    }

    public void setIsPriority(Boolean isPriority) {
        this.isPriority = isPriority;
    }

    public Boolean getIsHealed() {
        return isHealed;
    }

    public void setIsHealed(Boolean isHealed) {
        this.isHealed = isHealed;
    }

    public Boolean getIsPrayed() {
        return isPrayed;
    }

    public void setIsPrayed(Boolean isPrayed) {
        this.isPrayed = isPrayed;
    }

    public Boolean getIsBaptized() {
        return isBaptized;
    }

    public void setIsBaptized(Boolean isBaptized) {
        this.isBaptized = isBaptized;
    }

    public Boolean getIsPlanted() {
        return isPlanted;
    }

    public void setIsPlanted(Boolean isPlanted) {
        this.isPlanted = isPlanted;
    }

    public String getHomeChurch() {
        return homeChurch;
    }

    public void setHomeChurch(String homeChurch) {
        this.homeChurch = homeChurch;
    }

    public String getMinistry() {
        return ministry;
    }

    public void setMinistry(String ministry) {
        this.ministry = ministry;
    }

    public String getVisitationStatus() {
        return visitationStatus;
    }

    public void setVisitationStatus(String visitationStatus) {
        this.visitationStatus = visitationStatus;
    }

    public String getVisitedBy() {
        return visitedBy;
    }

    public void setVisitedBy(String visitedBy) {
        this.visitedBy = visitedBy;
    }

    public String getMentorCaseNotes() {
        return mentorCaseNotes;
    }

    public void setMentorCaseNotes(String mentorCaseNotes) {
        this.mentorCaseNotes = mentorCaseNotes;
    }

    public Boolean getIsServing() {
        return isServing;
    }

    public void setIsServing(Boolean isServing) {
        this.isServing = isServing;
    }
}
