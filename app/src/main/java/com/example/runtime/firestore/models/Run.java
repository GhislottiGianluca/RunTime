package com.example.runtime.firestore.models;

import com.google.firebase.Timestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Run {
    private String runId;
    private String userUuid;
    private Timestamp startDateTime;

    private float totalKm;

    private float totalCalories;

    private int totSteps;

    private long totalTimeMs;

    public Run(String runId, String userUuid, Timestamp startDateTime) {
        this.runId = runId;
        this.userUuid = userUuid;
        this.startDateTime = startDateTime;
    }

    public Run() {
    }

    @Override
    public String toString() {
        return "Run{" +
                "runId='" + runId + '\'' +
                ", userUuid='" + userUuid + '\'' +
                ", startDateTime=" + startDateTime +
                '}';
    }

    public String getRunId() {
        return runId;
    }

    public String getUserUuid() {
        return userUuid;
    }

    public Timestamp getStartDateTime() {
        return startDateTime;
    }

    public float getTotalKm() {
        return totalKm;
    }

    public float getTotalCalories() {
        return totalCalories;
    }

    public int getTotSteps() {
        return totSteps;
    }

    public long getTotalTimeMs() {
        return totalTimeMs;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public void setStartDateTime(Timestamp startDateTime) {
        this.startDateTime = startDateTime;
    }

    public void setTotalKm(float totalKm) {
        this.totalKm = totalKm;
    }

    public void setTotalCalories(float totalCalories) {
        this.totalCalories = totalCalories;
    }

    public void setTotSteps(int totSteps) {
        this.totSteps = totSteps;
    }

    public void setTotalTimeMs(long totalTimeMs) {
        this.totalTimeMs = totalTimeMs;
    }
}
