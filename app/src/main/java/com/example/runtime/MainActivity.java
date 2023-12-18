package com.example.runtime;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.runtime.sharedPrefs.SharedPreferencesHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.runtime.databinding.ActivityMainBinding;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 46;

    private static final int REQUEST_ACTIVITY_RECOGNITION_PERMISSION = 45;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        getWriteExternalStorage();
        getActivityPermission();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String uuid = SharedPreferencesHelper.getFieldStringFromSP(this, "uuid");
        if(uuid == null || uuid.isEmpty()){
            navigateToLogin();
        }
        String weight = SharedPreferencesHelper.getFieldStringFromSP(this, "weight");
        String height = SharedPreferencesHelper.getFieldStringFromSP(this, "height");

        if((weight == null || weight.isEmpty()) || (height == null || height.isEmpty())){
            navigateToUserMetrics();
        }


        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_activity,
                R.id.navigation_profile, R.id.navigation_run)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        //Creation of the notification channel
        CharSequence name = "reminder_channel";
        String description = "Running Reminder";
        NotificationChannel channel = new NotificationChannel("reminder_channel", name, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

    }

    private void navigateToLogin() {
        SharedPreferencesHelper.clearSP(this);
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Avoid navigating back
    }

    private void navigateToUserMetrics() {
        //case login successfull but no usermetrics available
        Intent intent = new Intent(MainActivity.this, UserMetricsActivity.class);
        startActivity(intent);
        finish();
    }

    private void getWriteExternalStorage() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        else
        {
            Log.i("WRITE_EXTERNAL_STORAGE", "PERMISSION_GRANTED");
        }
    }

    private void getActivityPermission() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACTIVITY_RECOGNITION},
                    REQUEST_ACTIVITY_RECOGNITION_PERMISSION);
        }
        else
        {
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ACTIVITY_RECOGNITION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getActivityPermission();
            }
            else {
                Toast.makeText(this, "step permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}