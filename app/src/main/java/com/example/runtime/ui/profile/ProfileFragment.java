package com.example.runtime.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.runtime.LoginActivity;
import com.example.runtime.MainActivity;
import com.example.runtime.R;
import com.example.runtime.databinding.FragmentProfileBinding;
import com.example.runtime.firestore.FirestoreHelper;
import com.example.runtime.firestore.models.Run;
import com.example.runtime.firestore.models.User;
import com.example.runtime.sharedPrefs.SharedPreferencesHelper;
import com.example.runtime.ui.profile.ProfileViewModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collections;
import java.util.List;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    private Button edit;
    private Button save;
    private Button cancel;

    private TextView usernameEditText;
    private TextView heightEditText;
    private TextView weightEditText;

    private static final String TAG = "ProfileFragment";


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        edit = root.findViewById(R.id.EditButton);
        save = root.findViewById(R.id.SaveButton);
        cancel = root.findViewById(R.id.CancelButton);
        usernameEditText = root.findViewById(R.id.textView10);
        weightEditText = root.findViewById(R.id.textView16);
        heightEditText = root.findViewById(R.id.textView11);

        String uuid = SharedPreferencesHelper.getFieldStringFromSP(requireContext(), "uuid");
        if (!uuid.isEmpty()) {
            getUserInfo(uuid);
        }

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

    private void getUserInfo(String uuid){
        FirestoreHelper.getDb().collection("users")
                .whereEqualTo("uuid", uuid)
                .get()
                .addOnCompleteListener(this::handleLoginResult);
    }

    private void handleLoginResult(Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            QuerySnapshot querySnapshot = task.getResult();

            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                User user = querySnapshot.getDocuments().get(0).toObject(User.class);

                if (user != null) {
                    //updatefields
                    usernameEditText.setText(user.getUsername());
                    weightEditText.setText(user.getWeight());
                    heightEditText.setText(user.getHeight());
                } else {
                    // There was an issue with conversion
                    Toast.makeText(requireContext(), "There was an issue with conversion", Toast.LENGTH_SHORT).show();
                }
            } else {
                // No user found
                Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle the error
            Exception e = task.getException();
            if (e != null) {
                Log.e(TAG, "Failed to fetch user: " + e.getMessage());
            }
        }
    }
}
