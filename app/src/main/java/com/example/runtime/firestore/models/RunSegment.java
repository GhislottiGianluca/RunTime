package com.example.runtime.firestore.models;

import com.google.firebase.Timestamp;

import java.time.LocalDateTime;

public class RunSegment {
    private String runId;
    private int steps;
    private Timestamp startDateTime;
    private Timestamp endDateTime;

    public RunSegment(String runId, int steps, Timestamp startDateTime, Timestamp endDateTime) {
        this.runId = runId;
        this.steps = steps;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public RunSegment() {
    }

    @Override
    public String toString() {
        return "RunSegment{" +
                "runId='" + runId + '\'' +
                ", steps=" + steps +
                ", startDateTime=" + startDateTime +
                ", endDateTime=" + endDateTime +
                '}';
    }

    public String getRunId() {
        return runId;
    }

    public int getSteps() {
        return steps;
    }

    public Timestamp getStartDateTime() {
        return startDateTime;
    }

    public Timestamp getEndDateTime() {
        return endDateTime;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public void setStartDateTime(Timestamp startDateTime) {
        this.startDateTime = startDateTime;
    }

    public void setEndDateTime(Timestamp endDateTime) {
        this.endDateTime = endDateTime;
    }
}
