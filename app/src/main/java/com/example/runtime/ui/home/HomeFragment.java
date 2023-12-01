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

    private Button logoutButton;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);


        logoutButton = binding.buttonLogout;

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