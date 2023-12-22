package com.example.runtime.ui.profile;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.runtime.R;
import com.example.runtime.databinding.FragmentProfileBinding;
import com.example.runtime.firestore.FirestoreHelper;
import com.example.runtime.firestore.models.User;
import com.example.runtime.sharedPrefs.SharedPreferencesHelper;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    //Button variable to handle the user's intent
    private Button edit;
    private Button save;
    private Button cancel;

    //Edit text used to manipulate the user's data
    private EditText usernameEditText;
    private EditText heightEditText;
    private EditText weightEditText;

    //Strings referred to the old value of height and weight
    private String oldWeight;
    private String oldHeight;

    private static final String TAG = "ProfileFragment";

    //Switch variable used to activate reminder notifications
    Switch reminderSwitch;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        edit = root.findViewById(R.id.EditButton);
        save = root.findViewById(R.id.SaveButton);
        cancel = root.findViewById(R.id.CancelButton);

        usernameEditText = root.findViewById(R.id.usernameEdit);
        weightEditText = root.findViewById(R.id.weightEdit);
        heightEditText = root.findViewById(R.id.heightEdit);

        reminderSwitch = root.findViewById(R.id.reminder_notifications);

        // I used the SharedPreferences to save the choice of the user to have the notifications activate or not
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        boolean isSwitchOn = sharedPref.getBoolean("EncouragementSwitchState", false);
        reminderSwitch.setChecked(isSwitchOn);

        //Set onClick listener for the notification switch
        reminderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("EncouragementSwitchState", isChecked);
            editor.apply();

            if (isChecked) {
                scheduleReminderNotifications();
            } else {
                cancelReminderNotifications();
            }
        });

        //Shared preferences to remember the selection of the user
        String uuid = SharedPreferencesHelper.getFieldStringFromSP(requireContext(), "uuid");
        if (!uuid.isEmpty()) {
            getUserInfo(uuid);
            setButtonListener(uuid);
        }


        return root;
    }

    private void allowEditable(boolean bool){
        weightEditText.setEnabled(bool);
        heightEditText.setEnabled(bool);
    }

    public void setButtonListener(String uuid){
        //setOnClickListener of the Edit Button
        edit.setOnClickListener(v -> {
            save.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.VISIBLE);
            edit.setVisibility(View.GONE);
            //set editTextenabled
            allowEditable(true);
        });

        //setOnClickListener of the Save Button
        save.setOnClickListener(v -> {
            String height = heightEditText.getText().toString();
            String weight = weightEditText.getText().toString();

            if (!height.isEmpty() && !weight.isEmpty() && validateString(weight) && validateString(height)) {
                updateUser(uuid, weight, height);
                save.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                edit.setVisibility(View.VISIBLE);

                //set editText not enabled
                allowEditable(false);
            } else {
                Toast.makeText(requireContext(), "Not all data are filled or invalid format", Toast.LENGTH_SHORT).show();
            }
        });

        //setOnClickListener of the Cancel Button
        cancel.setOnClickListener(v -> {

            save.setVisibility(View.GONE);
            cancel.setVisibility(View.GONE);
            edit.setVisibility(View.VISIBLE);

            //set editText not enabled
            allowEditable(false);

        });

    }

    private void updateUser(String uuid,String weight, String height) {
        if (uuid == null) {
            Log.e("Firestore Update", "Invalid uuid");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("weight", weight);
        data.put("height", height);

        // Create a query to find the document with the specified runId property
        Query query = FirestoreHelper.getDb().collection("users").whereEqualTo("uuid", uuid);

        // Execute the query
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            QueryDocumentSnapshot documentSnapshot = (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);
                            DocumentReference runRef = documentSnapshot.getReference();
                            Log.e("snapshot id", runRef.toString());

                            // Update the document with the new data
                            runRef.update(data)
                                    .addOnSuccessListener(aVoid -> {
                                        // Update successful
                                        Log.d("Firestore Update", "User updated successfully!");
                                        SharedPreferencesHelper.insertFieldStringToSP(requireContext(), "weight", weight);
                                        SharedPreferencesHelper.insertFieldStringToSP(requireContext(), "height", height);
                                        updateOldValue(weight, height);
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle the failure
                                        Log.e("Firestore Update", "Error updating document: " + e.getMessage());
                                        resetOldValue();
                                    });
                        } else {
                            // Handle the case where no document with the specified runId is found
                            Log.e("Firestore Update", "No document found with uuid: " + uuid);
                            resetOldValue();
                        }
                    } else {
                        // Handle the failure to execute the query
                        Log.e("Firestore Update", "Error executing query: " + task.getException().getMessage());
                        resetOldValue();
                    }
                });
    }

    private boolean validateString(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        if (!input.matches("\\d*\\.?\\d+")) {
            return false;
        }

        try {
            double number = Double.parseDouble(input);
            return number > 10;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void resetOldValue() {
        heightEditText.setText(oldHeight);
        weightEditText.setText(oldWeight);
    }

    private void updateOldValue(String newWeight, String newHeight) {
       oldWeight = newWeight;
       oldHeight = newHeight;
    }

    // Method used to schedule notifications
    private void scheduleReminderNotifications() {
        AlarmManager alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Reminder interval set to 3 days
        long interval = 3 * 24 * 60 * 60 * 1000;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
    }

    // Method used to cancel notifications
    private void cancelReminderNotifications() {
        AlarmManager alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    //check if user with uuid exist in the collection
    private void getUserInfo(String uuid){
        FirestoreHelper.getDb().collection("users")
                .whereEqualTo("uuid", uuid)
                .get()
                .addOnCompleteListener(this::handleRenderUserData);
    }

    private void handleRenderUserData(Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            QuerySnapshot querySnapshot = task.getResult();

            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                User user = querySnapshot.getDocuments().get(0).toObject(User.class);

                if (user != null) {
                    //updatefields
                    usernameEditText.setText(user.getUsername());
                    weightEditText.setText(user.getWeight());
                    heightEditText.setText(user.getHeight());
                    oldWeight = user.getWeight();
                    oldHeight = user.getHeight();
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