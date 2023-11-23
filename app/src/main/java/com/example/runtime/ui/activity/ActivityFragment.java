package com.example.runtime.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.runtime.R;
import com.example.runtime.databinding.FragmentActivityBinding;
import com.example.runtime.firestore.FirestoreHelper;
import com.example.runtime.firestore.models.Run;
import com.example.runtime.ui.activityDetails.ActivityDetail;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ActivityFragment extends Fragment {

    private FragmentActivityBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentActivityBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Assuming you have a list of items from your backend
        List<Run> runs = new ArrayList<>();

        getItemsFromBackend("1538fefe-256b-464e-b9cb-5ed8848043e8", runs);

        // Get the LinearLayout container from the root view
        LinearLayout cardContainer = root.findViewById(R.id.cardContainer);

        // Loop through the items and create CardViews
        for (Run item : runs) {
            // Inflate the card item layout
            View cardView = LayoutInflater.from(requireContext()).inflate(R.layout.activity_card, cardContainer, false);

            // Customize the card content based on item data
            // For example, if you have a TextView in your card layout
            TextView textView = cardView.findViewById(R.id.cardText);
            textView.setText(item.getRunId());

            // Add the card to the container
            cardContainer.addView(cardView);
        }
        return root;
    }

    private void updateUI(List<Run> itemList) {
        Log.w("UI updating", "Launched updateUI req");
        LinearLayout cardContainer = binding.getRoot().findViewById(R.id.cardContainer);

        for (Run item : itemList) {
            // Inflate the card item layout
            CardView cardView = (CardView) LayoutInflater.from(requireContext()).inflate(R.layout.activity_card, cardContainer, false);

            // Customize the card content based on item data
            TextView cardText = cardView.findViewById(R.id.cardText);
            //cardText.setText(item.getRunId());
            cardText.setText("Run Session nr: " + itemList.indexOf(item));


            TextView startSession = cardView.findViewById(R.id.startSession);
            LocalDateTime start = FirestoreHelper.getLocalDateTimeFromFirebaseTimestamp(item.getStartDateTime());
            startSession.setText(FirestoreHelper.formatDateTime(start));

            cardView.setOnClickListener(view -> {
                // Handle card click, e.g., navigate to detail screen
                navigateToDetailScreen(item.getRunId());
            });

            int cardBackgroundColor = getResources().getColor(R.color.activityCardBackground);
            cardView.setCardBackgroundColor(cardBackgroundColor);
            cardView.setRadius(25);

            //cardView.setPadding(0, 20, 0, 20);
            // Add the card to the container
            cardContainer.addView(cardView);
        }
    }

    private void getItemsFromBackend(String userUuid, List<Run> runs) {
        CollectionReference runsCollection = FirestoreHelper.getDb().collection("runs");

        Query query = runsCollection
                .whereEqualTo("userUuid", userUuid);

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.w("RUN", "try to deserialize run");
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Log.w("RUN shap", "try  run");
                        Run run = document.toObject(Run.class);
                        runs.add(run);
                    }
                    runs.forEach(run -> Log.d(run.getRunId(), "at time " + run.getStartDateTime()));
                    Log.w("Update UI", "try  to updating ui");
                    //to reflect the changes
                    updateUI(runs);
                })
                .addOnFailureListener(e -> {
                    Log.w("failed to get run", "Error adding document", e);
                });
    }

    private void navigateToDetailScreen(String runId) {
        Intent intent = new Intent(requireContext(), ActivityDetail.class);
        intent.putExtra("runId", runId);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}
