package com.example.runtime.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.runtime.R;
import com.example.runtime.databinding.FragmentActivityBinding;
import com.example.runtime.firestore.FirestoreHelper;
import com.example.runtime.firestore.models.Run;
import com.example.runtime.sharedPrefs.SharedPreferencesHelper;
import com.example.runtime.ui.activityDetails.ActivityDetail;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.core.OrderBy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
        LinearLayout cardContainer = root.findViewById(R.id.cardContainer);
        String userUuid = SharedPreferencesHelper.getFieldStringFromSP(requireContext(), "uuid");

        if (userUuid != null && !userUuid.equals("")) {
            getRunsByUserUuidFromBackend(userUuid, runs, cardContainer);
            updateUI(runs, cardContainer);
        }

        return root;
    }

    private void updateUI(List<Run> itemList, LinearLayout cardContainer) {
        Log.w("UI updating", "Launched updateUI req");

        for (Run item : itemList) {
            // Inflate the card item layout
           createCard(item, itemList.indexOf(item), cardContainer);
        }
    }

    private void createCard(Run item, int listIndex, LinearLayout cardContainer){
        CardView cardView = (CardView) LayoutInflater.from(requireContext()).inflate(R.layout.activity_card, cardContainer, false);

        // Customize the card content based on item data
        TextView cardText = cardView.findViewById(R.id.cardText);
        //cardText.setText(item.getRunId());
        cardText.setText("Run Session nr: " + listIndex);


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

    private void getRunsByUserUuidFromBackend(String userUuid, List<Run> runs, LinearLayout cardContainer) {
        //problem with startDateTime descending
        FirestoreHelper.getDb().collection("runs")
                .whereEqualTo("userUuid", userUuid).orderBy("startDateTime", Query.Direction.ASCENDING).get()
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
                    Collections.reverse(runs);
                    updateUI(runs, cardContainer);
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
