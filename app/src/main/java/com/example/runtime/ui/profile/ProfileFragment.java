package com.example.runtime.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.runtime.R;
import com.example.runtime.databinding.FragmentProfileBinding;
import com.example.runtime.ui.profile.ProfileViewModel;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    private Button edit;
    private Button save;
    private Button cancel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        edit = root.findViewById(R.id.EditButton);
        save = root.findViewById(R.id.SaveButton);
        cancel = root.findViewById(R.id.CancelButton);

        setButtonListener();

        return root;
    }

    public void setButtonListener(){

        //setOnClickListener of the Edit Button
        edit.setOnClickListener(v -> {

            save.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.VISIBLE);
            edit.setVisibility(View.GONE);

        });

        //setOnClickListener of the Save Button
        save.setOnClickListener(v -> {

            save.setVisibility(View.GONE);
            cancel.setVisibility(View.GONE);
            edit.setVisibility(View.VISIBLE);

        });

        //setOnClickListener of the Cancel Button
        cancel.setOnClickListener(v -> {

            save.setVisibility(View.GONE);
            cancel.setVisibility(View.GONE);
            edit.setVisibility(View.VISIBLE);


        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
