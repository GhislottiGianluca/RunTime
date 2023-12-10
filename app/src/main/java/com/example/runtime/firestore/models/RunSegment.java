package com.example.runtime.firestore.models;

import com.google.firebase.Timestamp;

import org.osmdroid.util.GeoPoint;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

public class RunSegment {
    private String runId;
    private int steps;
    private Timestamp startDateTime;

    private Timestamp endDateTime;

    private Double km;
    private double averagePace;

    private double calories;

    private ArrayList<GeoPoint> geoPoints = new ArrayList<>();

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

    public Double getKm() {
        return km;
    }

    public double getAveragePace() {
        return averagePace;
    }

    public double getCalories() {
        return calories;
    }

    public ArrayList<GeoPoint> getGeoPoints() {
        return geoPoints;
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

    public void setGeoPoints(ArrayList<Map<String, Double>> geoPoints) {
        if (geoPoints == null || geoPoints.isEmpty()) {
            return;
        }

        ArrayList<GeoPoint> geoPointArrayList = new ArrayList<>();
        for (Map<String, Double> geopoint : geoPoints) {
            if (geopoint != null && geopoint.containsKey("latitude") && geopoint.containsKey("longitude")) {
                Double latitude = geopoint.get("latitude");
                Double longitude = geopoint.get("longitude");

                if (latitude != null && longitude != null) {
                    geoPointArrayList.add(new GeoPoint(latitude, longitude));
                }
            }
        }
        this.geoPoints = geoPointArrayList;
    }
}
