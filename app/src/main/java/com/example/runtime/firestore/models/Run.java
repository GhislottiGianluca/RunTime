package com.example.runtime.firestore.models;

import com.google.firebase.Timestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Run {
    private String runId;
    private String userUuid;
    private Timestamp startDateTime;

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

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public void setStartDateTime(Timestamp startDateTime) {
        this.startDateTime = startDateTime;
    }
}
