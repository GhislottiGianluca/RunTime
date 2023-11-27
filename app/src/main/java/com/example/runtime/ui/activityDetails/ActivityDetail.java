package com.example.runtime.ui.activityDetails;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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
        LinearLayout infoContainer = findViewById(R.id.infoContainer);

        //testBackend
        getItemsFromBackend(runId, runSegments, infoContainer);
        updateUI(runSegments, infoContainer);
    }

    private void getItemsFromBackend(String runId, List<RunSegment> runSegments, LinearLayout infoContainer) {
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

                    updateUI(runSegments, infoContainer);

                })
                .addOnFailureListener(e -> {
                    Log.w("failed to get run", "Error adding document", e);
                });
    }

    private void updateUI(List<RunSegment> itemList, LinearLayout infoContainer) {
        if(itemList.isEmpty()){
            return;
        }
        //property prep
        itemList.sort(Comparator.comparing(RunSegment::getStartDateTime));

        LocalDateTime startRun = FirestoreHelper.getLocalDateTimeFromFirebaseTimestamp(itemList.get(0).getStartDateTime());
        LocalDateTime endRun = FirestoreHelper.getLocalDateTimeFromFirebaseTimestamp(itemList.get(itemList.size() - 1).getEndDateTime());

        Duration totalDuration = Duration.between(startRun, endRun);
        String totalDurationString = null;
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            totalDurationString = String.format("%02d:%02d:%02d", totalDuration.toHours(), totalDuration.toMinutesPart(), totalDuration.toSecondsPart());
        }*/

        int totalSteps = itemList.stream().mapToInt(RunSegment::getSteps).sum();
        //considering a 0.8m step-width
        double totalDistanceKM = totalSteps * 0.8 / 1000;

        createInfoItem("Nr.steps", String.valueOf(totalSteps), infoContainer);
        createInfoItem("totalDistanceKM", String.valueOf(totalDistanceKM), infoContainer);
        createInfoItem("startRun", startRun.toString(), infoContainer);
        createInfoItem("endRun", endRun.toString(), infoContainer);
        createInfoItem("totalDurationInMinutes", String.valueOf(totalDuration.toMinutes()), infoContainer);
        createInfoItem("totalDurationInSeconds", String.valueOf(totalDuration.getSeconds()), infoContainer);


    }

    private void createInfoItem(String titleText, String propertyText, LinearLayout layout){
        LinearLayout infoLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.info_run_detail, layout, false);
        // Customize the card content based on item data
        TextView title = infoLayout.findViewById(R.id.itemTitle);
        TextView property = infoLayout.findViewById(R.id.itemProperty);
        //cardText.setText(item.getRunId());
        title.setText(titleText);
        property.setText(propertyText);

        layout.addView(infoLayout);
    }
}
