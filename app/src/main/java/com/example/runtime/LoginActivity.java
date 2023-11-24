package com.example.runtime;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.runtime.firestore.FirestoreHelper;
import com.example.runtime.firestore.models.User;
import com.example.runtime.sharedPrefs.SharedPreferencesHelper;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;

    private TextView registerText;

    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.editTextUsername);
        passwordEditText = findViewById(R.id.editTextPassword);
        registerText = findViewById(R.id.register);
        loginButton = findViewById(R.id.buttonLogin);

        String uuid = SharedPreferencesHelper.getFieldStringFromSP(this, "uuid");

        if(!uuid.isEmpty()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);

            //finish this activity
            finish();
        }


        loginButton.setOnClickListener(view -> {
            // Get username and password from EditText fields
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            // Check if username and password are valid (you can replace this with your own logic)
            if (isValidCredentials(username, password)) {
                Log.e("parameter", "username: " + username + " password: " + password);
                FirestoreHelper.getDb().collection("users")
                        .whereEqualTo("username", username)
                        .whereEqualTo("password", password)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                QuerySnapshot querySnapshot = task.getResult();

                                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                    // Assuming your Firestore document maps to the User class
                                    User user = querySnapshot.getDocuments().get(0).toObject(User.class);
                                    // Now you can use the 'user' object
                                    if (user != null) {
                                        // User exists, perform the appropriate action
                                        SharedPreferencesHelper.insertFieldStringToSP(this, "username", username);
                                        SharedPreferencesHelper.insertFieldStringToSP(this, "uuid", user.getUuid());
                                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish(); //avoid to navigate back
                                    } else {
                                        // There was an issue with conversion
                                        Toast.makeText(LoginActivity.this, "There was an issue with conversion", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // No user found
                                    Toast.makeText(LoginActivity.this, "user not found", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Handle the error
                                Exception e = task.getException();
                                if (e != null) {
                                    Log.e("failed to fetch user", Objects.requireNonNull(e.getMessage()));
                                }
                            }

                        })
                        .addOnFailureListener(e -> {
                            Log.w("failed to get run", "Error adding document", e);
                            Toast.makeText(LoginActivity.this, "Invalid username or password, no user found", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Failed login
                Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });

        registerText.setOnClickListener(view -> {
            // navigate to registerScreen
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    // Replace this method with your own logic to validate the credentials
    private boolean isValidCredentials(String username, String password) {
        // Example: Check if username and password are not empty
        return !username.isEmpty() && !password.isEmpty();
    }
}