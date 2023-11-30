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
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText repeatPasswordEditText;
    private EditText heightEditText;
    private EditText weightEditText;
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
        heightEditText = findViewById(R.id.height);
        weightEditText = findViewById(R.id.weight);
        alreadyRegisteredText = findViewById(R.id.alreadyAnAccount);
        createAccountButton = findViewById(R.id.buttonCreateAccount);

        createAccountButton.setOnClickListener(view -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String repeatPassword = repeatPasswordEditText.getText().toString();
            String height = heightEditText.getText().toString();
            String weight = weightEditText.getText().toString();

            if (isPasswordMatchingAndDataInserted(password, repeatPassword, weight, height)) {
                ifUsernameAvailableCreateUser(username, password,  weight, height);
            } else {
                Toast.makeText(RegisterActivity.this, "Password and repeatPassword are different or not all data are filled", Toast.LENGTH_SHORT).show();
            }
        });

        alreadyRegisteredText.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void ifUsernameAvailableCreateUser(String username, String password, String weight, String height) {
        FirestoreHelper.getDb().collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            Toast.makeText(RegisterActivity.this, "Username not available, please change it", Toast.LENGTH_SHORT).show();
                        } else {
                            createUser(username, password, weight, height);
                        }
                    } else {
                        Log.e(TAG, "Error checking username availability", task.getException());
                    }
                });
    }

    private boolean isPasswordMatchingAndDataInserted(String password, String repeatPassword, String weight, String height) {
        return password.equals(repeatPassword) && !weight.isEmpty() && !height.isEmpty();
    }

    private void createUser(String username, String password,  String weight, String height) {
        String uuid = UUID.randomUUID().toString();
        Map<String, Object> data = new HashMap<>();
        data.put("uuid", uuid);
        data.put("username", username);
        data.put("password", password);
        data.put("weight", weight);
        data.put("height", height);

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
