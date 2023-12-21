package com.example.runtime.ui.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
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
        for (Run item : itemList) {
            // Inflate the card item layout
            createActivityCard(item, cardContainer);
        }
    }

    private void createActivityCard(Run item, LinearLayout cardContainer) {
        CardView cardView = (CardView) LayoutInflater.from(requireContext()).inflate(R.layout.activity_card, cardContainer, false);

        // Customize the card content based on item data
        TextView cardText = cardView.findViewById(R.id.cardText);
        DecimalFormat df = new DecimalFormat("#.##");
        cardText.setText("Total KM: " + df.format(item.getTotalKm()));

        ImageView cardImg = cardView.findViewById(R.id.cardImg);

        Drawable drawable;

        //set one of 3 possible icons based on user activity results
        if (item.getTotalKm() < 2) {
            drawable = getResources().getDrawable(R.drawable.normal_run);
        } else if (item.getTotalKm() < 5) {
            drawable = getResources().getDrawable(R.drawable.good_run);
        } else {
            drawable = getResources().getDrawable(R.drawable.great_run);
        }

        // Set the drawable to the ImageView
        cardImg.setImageDrawable(drawable);

        TextView startSession = cardView.findViewById(R.id.startSession);
        LocalDateTime start = FirestoreHelper.getLocalDateTimeFromFirebaseTimestamp(item.getStartDateTime());
        startSession.setText(FirestoreHelper.formatDateTime(start));

        cardView.setOnClickListener(view -> {
            navigateToDetailScreen(item.getRunId());
        });

        // Add the card to the container
        cardContainer.addView(cardView);
    }

    private void getRunsByUserUuidFromBackend(String userUuid, List<Run> runs, LinearLayout cardContainer) {
        //problem with startDateTime descending
        FirestoreHelper.getDb().collection("runs")
                .whereEqualTo("userUuid", userUuid).orderBy("startDateTime", Query.Direction.ASCENDING).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Run run = document.toObject(Run.class);
                        runs.add(run);
                    }
                    //show data from to more recent to older
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
