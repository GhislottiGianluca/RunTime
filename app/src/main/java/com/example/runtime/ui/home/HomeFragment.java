package com.example.runtime.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.runtime.LoginActivity;
import com.example.runtime.databinding.FragmentHomeBinding;
import com.example.runtime.firestore.FirestoreHelper;
import com.example.runtime.firestore.models.Run;
import com.example.runtime.sharedPrefs.SharedPreferencesHelper;

import java.time.LocalDateTime;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private Button testButton;
    private Button createRunButton;
    private Button createSegmentButton;
    private Button getRun;
    private Button getSegments;

    private Button logoutButton;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        //testButton = binding.buttonTest;
        createRunButton = binding.buttonRunCreate;
        createSegmentButton = binding.buttonRunSegCreate;
        getRun = binding.buttonGetRuns;
        getSegments = binding.buttonGetSegments;
        logoutButton = binding.buttonLogout;


      /*  testButton.setOnClickListener(e -> {
            FirestoreHelper.createUser("Cristo", "Risorto");
        });*/

        createRunButton.setOnClickListener(e -> {
            FirestoreHelper.createRun("1538fefe-256b-464e-b9cb-5ed8848043e8", LocalDateTime.now());
        });

        createSegmentButton.setOnClickListener(e -> {
            FirestoreHelper.createRunSegment("12914a82-3482-407d-b8f8-594899243ddc", 15000, LocalDateTime.now(), LocalDateTime.now().plusHours(3));
        });

        getRun.setOnClickListener(e -> {
           FirestoreHelper.getRunsByUuidAndStartDateTimeInRange("1538fefe-256b-464e-b9cb-5ed8848043e8", LocalDateTime.now().minusHours(4), LocalDateTime.now().plusHours(5));
        });

        getSegments.setOnClickListener(e -> {
            FirestoreHelper.getRunSegmentsByRunId("12914a82-3482-407d-b8f8-594899243ddc");
        });

        logoutButton.setOnClickListener(e -> {
            SharedPreferencesHelper.clearSP(requireContext());
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            startActivity(intent);

            // Optionally, you can finish the current activity to prevent going back to it
            requireActivity().finish();
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}