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
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;
import java.util.UUID;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private TextView registerText;
    private Button loginButton;

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.editTextUsername);
        passwordEditText = findViewById(R.id.editTextPassword);
        registerText = findViewById(R.id.register);
        loginButton = findViewById(R.id.buttonLogin);

        checkForExistingUser();

        loginButton.setOnClickListener(view -> attemptLogin());

        registerText.setOnClickListener(view -> navigateToRegisterScreen());
    }

    private void checkForExistingUser() {
        String uuid = SharedPreferencesHelper.getFieldStringFromSP(this, "uuid");
        if (!uuid.isEmpty()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Avoid navigating back to LoginActivity
        }
    }

    private void attemptLogin() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (isValidCredentials(username, password)) {
            FirestoreHelper.getDb().collection("users")
                    .whereEqualTo("username", username)
                    .whereEqualTo("password", password)
                    .get()
                    .addOnCompleteListener(task -> handleLoginResult(task, username));
        } else {
            // Failed login
            Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLoginResult(Task<QuerySnapshot> task, String username) {
        if (task.isSuccessful()) {
            QuerySnapshot querySnapshot = task.getResult();

            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                User user = querySnapshot.getDocuments().get(0).toObject(User.class);

                if (user != null) {
                    handleSuccessfulLogin(username, user);
                } else {
                    // There was an issue with conversion
                    Toast.makeText(LoginActivity.this, "There was an issue with conversion", Toast.LENGTH_SHORT).show();
                }
            } else {
                // No user found
                Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle the error
            Exception e = task.getException();
            if (e != null) {
                Log.e(TAG, "Failed to fetch user: " + e.getMessage());
            }
        }
    }

    private void handleSuccessfulLogin(String username, User user) {
        SharedPreferencesHelper.insertFieldStringToSP(this, "username", username);
        SharedPreferencesHelper.insertFieldStringToSP(this, "uuid", user.getUuid());
        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Avoid navigating back to LoginActivity
    }

    private void navigateToRegisterScreen() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private boolean isValidCredentials(String username, String password) {
        return !username.isEmpty() && !password.isEmpty();
    }
}