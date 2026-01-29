package com.revature.model;

import java.time.LocalDateTime;


//   Designation Entity - Job titles/positions

public class Designation {
    private int designationId;
    private String designationName;
    private int level;
    private boolean isActive;
    private LocalDateTime createdAt;


    public Designation() {
        this.isActive = true;
        this.level = 1;
    }


    public Designation(String designationName, int level) {
        this();
        this.designationName = designationName;
        this.level = level;
    }


    public int getDesignationId() { return designationId; }
    public void setDesignationId(int designationId) { this.designationId = designationId; }

    public String getDesignationName() { return designationName; }
    public void setDesignationName(String designationName) { this.designationName = designationName; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getLevelName() {
        return switch (level) {
            case 1 -> "Junior";
            case 2 -> "Mid-Level";
            case 3 -> "Senior";
            case 4 -> "Lead";
            case 5 -> "Manager";
            default -> "Unknown";
        };
    }

    @Override
    public String toString() {
        return String.format("%d. %s (Level : %s)", designationId, designationName, getLevelName());
    }
}


