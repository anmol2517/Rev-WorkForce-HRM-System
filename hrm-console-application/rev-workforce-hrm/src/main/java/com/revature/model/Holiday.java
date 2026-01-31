package com.revature.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


//  Holiday Entity - Company holidays calendar

public class Holiday {
    private int holidayId;
    private String holidayName;
    private LocalDate holidayDate;
    private String description;
    private int year;
    private boolean isOptional;
    private LocalDateTime createdAt;

    public Holiday() {
        this.isOptional = false;
        this.year = java.time.Year.now().getValue();
    }

    public Holiday(String holidayName, LocalDate holidayDate, String description) {
        this();
        this.holidayName = holidayName;
        this.holidayDate = holidayDate;
        this.description = description;
        this.year = holidayDate.getYear();
    }

    public int getHolidayId() { return holidayId; }
    public void setHolidayId(int holidayId) { this.holidayId = holidayId; }

    public String getHolidayName() { return holidayName; }
    public void setHolidayName(String holidayName) { this.holidayName = holidayName; }

    public LocalDate getHolidayDate() { return holidayDate; }
    public void setHolidayDate(LocalDate holidayDate) { 
        this.holidayDate = holidayDate;
        if (holidayDate != null) this.year = holidayDate.getYear();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public boolean isOptional() { return isOptional; }
    public void setOptional(boolean optional) { isOptional = optional; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getDayOfWeek() {
        return holidayDate != null ? 
            holidayDate.getDayOfWeek().toString().substring(0, 3) : "";
    }

    public String getFormattedDate() {
        return holidayDate != null ? 
            holidayDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) : "";
    }

    public boolean isUpcoming() {
        return holidayDate != null && holidayDate.isAfter(LocalDate.now());
    }

    @Override
    public String toString() {
        String optional = isOptional ? " (Optional)" : "";
        return String.format("%s - %s (%s)%s", 
            getFormattedDate(), holidayName, getDayOfWeek(), optional);
    }
}

