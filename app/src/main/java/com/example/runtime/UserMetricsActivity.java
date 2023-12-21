package com.example.runtime;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.runtime.firestore.FirestoreHelper;
import com.example.runtime.sharedPrefs.SharedPreferencesHelper;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class UserMetricsActivity extends AppCompatActivity {


    private EditText heightEditText;
    private EditText weightEditText;
    private Button saveMetricsButton;

    private static final String TAG = "UserMetricsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usermetrics);

        String uuid = SharedPreferencesHelper.getFieldStringFromSP(this, "uuid");
        if(uuid == null || uuid.isEmpty()){
            //go to login again
            navigateToLogin();
        }

        heightEditText = findViewById(R.id.height);
        weightEditText = findViewById(R.id.weight);
        saveMetricsButton = findViewById(R.id.saveMetricsButton);

        saveMetricsButton.setOnClickListener(view -> {
            String height = heightEditText.getText().toString();
            String weight = weightEditText.getText().toString();

            if (!height.isEmpty() && !weight.isEmpty() && validateString(height) && validateString(weight)) {
               updateUser(uuid, weight, height);
            } else {
                Toast.makeText(UserMetricsActivity.this, "Not all data are filled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //adding metrics data to user model
    private void updateUser(String uuid,String weight, String height) {
        if (uuid == null) {
            Log.e("Firestore Update", "Invalid uuid");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("weight", weight);
        data.put("height", height);

        Query query = FirestoreHelper.getDb().collection("users").whereEqualTo("uuid", uuid);

        // Execute the query
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Update the first document found with the specified uuid
                            QueryDocumentSnapshot documentSnapshot = (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);
                            DocumentReference runRef = documentSnapshot.getReference();
                            Log.e("snapshot id", runRef.toString());

                            // Update the document with the new data
                            runRef.update(data)
                                    .addOnSuccessListener(aVoid -> {
                                        // Update successful
                                        Log.d("Firestore Update", "User updated successfully!");
                                        SharedPreferencesHelper.insertFieldStringToSP(this, "weight", weight);
                                        SharedPreferencesHelper.insertFieldStringToSP(this, "height", height);
                                        //navigation
                                        navigateToMainActivity();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle the failure
                                        Log.e("Firestore Update", "Error updating document: " + e.getMessage());
                                    });
                        } else {
                            Log.e("Firestore Update", "No document found with uuid: " + uuid);
                        }
                    } else {
                        // Handle the failure to execute the query
                        Log.e("Firestore Update", "Error executing query: " + task.getException().getMessage());
                    }
                });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(UserMetricsActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Avoid navigating back
    }

    private void navigateToLogin() {
        SharedPreferencesHelper.clearSP(this);
        Intent intent = new Intent(UserMetricsActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Avoid navigating back
    }

    //a first implementation to validate the metrics data
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


}
