package com.example.runtime.ui.activityDetails;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.runtime.R;
import com.example.runtime.firestore.FirestoreHelper;
import com.example.runtime.firestore.models.RunSegment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ActivityDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Retrieve the runId from the Intent
        String runId = getIntent().getStringExtra("runId");

        TextView textView = findViewById(R.id.detailTitle);
        textView.setText("Beccati sto TRAPEZIO!! \n\n" + runId + "\n\n" + "Per Favoreh");

        List<RunSegment> runSegments = new ArrayList<>();
        //testBackend
        getItemsFromBackend(runId, runSegments);


        // Use the runId to fetch detailed information or update UI
    }

    private void updateUI(List<RunSegment> itemList) {
        if(itemList.isEmpty()){
            return;
        }
        itemList.sort(Comparator.comparing(RunSegment::getStartDateTime));

        LocalDateTime startRun = FirestoreHelper.getLocalDateTimeFromFirebaseTimestamp(itemList.get(0).getStartDateTime());
        LocalDateTime endRun = FirestoreHelper.getLocalDateTimeFromFirebaseTimestamp(itemList.get(itemList.size() - 1).getEndDateTime());

        Duration totalDuration = Duration.between(startRun, endRun);
        String totalDurationString = null;
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            totalDurationString = String.format("%02d:%02d:%02d", totalDuration.toHours(), totalDuration.toMinutesPart(), totalDuration.toSecondsPart());
        }*/


        int totalSteps = itemList.stream().mapToInt(RunSegment::getSteps).sum();

        Log.w("totalSteps", String.valueOf(totalSteps));
        Log.w("startRun", startRun.toString());
        Log.w("endRun", endRun.toString());
        //Log.w("totalDurationString", totalDurationString);
        Log.w("totalDurationInMinutes", String.valueOf(totalDuration.toMinutes()));
        Log.w("totalDurationInSeconds", String.valueOf(totalDuration.getSeconds()));
    }


    private void getItemsFromBackend(String runId, List<RunSegment> runSegments) {
        CollectionReference runsCollection = FirestoreHelper.getDb().collection("runSegments");

        Query query = runsCollection.whereEqualTo("runId", runId);

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.w("RUN SEGMENT", "try to deserialize segment");
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        RunSegment segment = document.toObject(RunSegment.class);
                        runSegments.add(segment);
                    }
                    //runSegments.forEach(segment -> Log.d(segment.getRunId(), "at time " + FirestoreHelper.getLocalDateTimeFromFirebaseTimestampLocalDateTime(segment.getStartDateTime()) ));

                    updateUI(runSegments);

                })
                .addOnFailureListener(e -> {
                    Log.w("failed to get run", "Error adding document", e);
                });
    }
}
