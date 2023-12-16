package com.example.runtime.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.runtime.LoginActivity;
import com.example.runtime.databinding.FragmentHomeBinding;
import com.example.runtime.firestore.FirestoreHelper;
import com.example.runtime.firestore.models.Run;
import com.example.runtime.firestore.models.RunSegment;
import com.example.runtime.sharedPrefs.SharedPreferencesHelper;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private Button logoutButton;

    private TextView stepsText;
    private TextView caloriesText;
    private TextView kmText;
    private TextView timeText;




    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        stepsText = binding.textSteps;
        kmText = binding.textKm;
        timeText = binding.textTime;
        caloriesText = binding.textCalories;



        logoutButton = binding.buttonLogout;
        logoutButton.setOnClickListener(e -> {
            SharedPreferencesHelper.clearSP(requireContext());
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            startActivity(intent);

            // Optionally, you can finish the current activity to prevent going back to it
            requireActivity().finish();
        });

        List<Run> runs = new ArrayList<>();

        String uuid = SharedPreferencesHelper.getFieldStringFromSP(requireContext(), "uuid");
        if (!uuid.isEmpty()) {
            //testBackend
            getRunsFromBackend(uuid, runs);
        } else {
            updateUI(runs);
        }


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void getRunsFromBackend(String userUuid, List<Run> runs) {
        CollectionReference runsCollection = FirestoreHelper.getDb().collection("runs");

        Query query = runsCollection.whereEqualTo("userUuid", userUuid);

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.w("RUN", "try to deserialize run");
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Run singleRun = document.toObject(Run.class);
                        runs.add(singleRun);
                    }

                    updateUI(runs);

                })
                .addOnFailureListener(e -> {
                    Log.w("failed to get run", "Error adding document", e);
                });
    }

    private void updateUI(List<Run> runs) {
        if (runs.isEmpty()) {
            stepsText.setText("N/A");
            kmText.setText("N/A");
            caloriesText.setText("N/A");
            timeText.setText("N/A");
            return;
        }
        DecimalFormat df = new DecimalFormat("#.###");
        //property prep
        int totalSteps = runs.stream().mapToInt(Run::getTotSteps).sum();
        float totalDistanceKM = (float) runs.stream().mapToDouble(Run::getTotalKm).sum();
        float totalCalories = (float) runs.stream().mapToDouble(Run::getTotalCalories).sum();
        long totalTime = runs.stream().mapToLong(Run::getTotalTimeMs).sum();

        stepsText.setText(String.valueOf(totalSteps) + " steps");
        kmText.setText(df.format(totalDistanceKM) + " km");
        caloriesText.setText(df.format(totalCalories));
        timeText.setText(String.valueOf(totalTime / 1000) + "s" );
    }
}