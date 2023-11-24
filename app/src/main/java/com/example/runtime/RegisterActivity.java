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

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText repeatPasswordEditText;
    private TextView alreadyRegisteredText;
    private Button createAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameEditText = findViewById(R.id.editTextUsername);
        passwordEditText = findViewById(R.id.editTextPassword);
        repeatPasswordEditText = findViewById(R.id.editTextRepeatPassword);
        alreadyRegisteredText = findViewById(R.id.alreadyAnAccount);
        createAccountButton = findViewById(R.id.buttonCreateAccount);

        // Add any additional logic for the register screen if needed
        createAccountButton.setOnClickListener(view -> {
            // Get username and password from EditText fields
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String repeatPassword = repeatPasswordEditText.getText().toString();

            // Check if username and password are valid (you can replace this with your own logic)
            if (isPasswordMatching(password, repeatPassword)) {
                if (isUsernameAvailable(username)) {
                    //can be removed
                    String uuid = UUID.randomUUID().toString();
                    Map<String, Object> data = new HashMap<>();
                    data.put("uuid", uuid);
                    data.put("username", username);
                    data.put("password", password);

                    //        Log.d(String tag, String message): Debug log message.
                    //        Log.i(String tag, String message): Info log message.
                    //        Log.w(String tag, String message): Warning log message.
                    //        Log.e(String tag, String message): Error log message.
                    FirestoreHelper.getDb().collection("users")
                            .add(data)
                            .addOnSuccessListener(documentReference -> {
                                // Document added successfully
                                Toast.makeText(RegisterActivity.this, "Registration successful, please log in", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                                Log.d("User created", "DocumentSnapshot added with ID: " + documentReference.getId());
                            })
                            .addOnFailureListener(e -> {
                                // Handle errors
                                Log.w("User creation failed", "Error adding document", e);
                            });

                } else {
                    Toast.makeText(RegisterActivity.this, "Username not available, please change it", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Failed login
                Toast.makeText(RegisterActivity.this, "Password and repeatPassword are different", Toast.LENGTH_SHORT).show();
            }
        });

        alreadyRegisteredText.setOnClickListener(view -> {
            // navigate to registerScreen
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    //todo
    private boolean isUsernameAvailable(String username) {
        //firebase call to check availability
        return true;
    }

    private boolean isPasswordMatching(String password, String repeatPassword) {
        // Example: Check if username and password are not empty
        return password.equals(repeatPassword);
    }
}