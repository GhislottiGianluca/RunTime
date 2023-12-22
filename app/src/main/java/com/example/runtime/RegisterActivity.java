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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    //Edit Text used to insert username and password during the registration process
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText repeatPasswordEditText;

    private TextView alreadyRegisteredText;
    private Button createAccountButton;

    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameEditText = findViewById(R.id.editTextUsername);
        passwordEditText = findViewById(R.id.editTextPassword);
        repeatPasswordEditText = findViewById(R.id.editTextRepeatPassword);
        alreadyRegisteredText = findViewById(R.id.alreadyAnAccount);
        createAccountButton = findViewById(R.id.buttonCreateAccount);

        createAccountButton.setOnClickListener(view -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String repeatPassword = repeatPasswordEditText.getText().toString();

            if (isPasswordMatching(password, repeatPassword/*, weight, height*/)) {
                ifUsernameAvailableCreateUser(username, password/*,  weight, height*/);
            } else {
                Toast.makeText(RegisterActivity.this, "Password and repeatPassword are different or not all data are filled", Toast.LENGTH_SHORT).show();
            }
        });

        alreadyRegisteredText.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    //Methods used to check if the username inserted already exists
    private void ifUsernameAvailableCreateUser(String username, String password) {
        FirestoreHelper.getDb().collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            Toast.makeText(RegisterActivity.this, "Username not available, please change it", Toast.LENGTH_SHORT).show();
                        } else {
                            createUser(username, password);
                        }
                    } else {
                        Log.e(TAG, "Error checking username availability", task.getException());
                    }
                });
    }


    //Method used to check if the password and the repetition match
    private boolean isPasswordMatching(String password, String repeatPassword) {
        return password.equals(repeatPassword);
    }

    //Mehthod used to create an user inside the database
    private void createUser(String username, String password) {
        String uuid = UUID.randomUUID().toString();
        Map<String, Object> data = new HashMap<>();
        data.put("uuid", uuid);
        data.put("username", username);
        data.put("password", password);

        FirestoreHelper.getDb().collection("users")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(RegisterActivity.this, "Registration successful, please log in", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    Log.d(TAG, "User created. DocumentSnapshot added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding document", e);
                });
    }
}
