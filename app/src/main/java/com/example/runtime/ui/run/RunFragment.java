package com.example.runtime.ui.run;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.runtime.R;
import com.example.runtime.databinding.FragmentRunBinding;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Locale;

public class RunFragment extends Fragment {

    //Button variable
    private FragmentRunBinding binding;
    private ImageButton play;
    private ImageButton pause;
    private ImageButton stop;

    private boolean pauseManagement = true;

    //Users data
    private float height_cm = 170.0f;
    private float weight_kg = 65.5f;
    private final float stride_km = (float) (height_cm * 1.4) / 1_000_000;
    private float km_value = 0;
    private float calories_value = 0;
    private float actualPace_value;
    private float averagePace_value;

    // Data running textView
    private TextView averagePace;
    private TextView actualPace;
    private TextView calories;
    private TextView km;

    //Chronometer variable
    Chronometer chronometer;
    long timeWhenStopped;

    //Sensor variables
    private Sensor accSensor;
    private SensorManager sensorManager;
    private StepCounterListener sensorListener;

    List<LocalDateTime> globalList = new ArrayList<>();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        RunViewModel runViewModel =
                new ViewModelProvider(this).get(RunViewModel.class);

        binding = FragmentRunBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //Button settings
        play = (ImageButton) root.findViewById(R.id.play);
        pause = (ImageButton) root.findViewById(R.id.pause);
        stop = (ImageButton) root.findViewById(R.id.stop);

        //Data textView
        averagePace = (TextView) root.findViewById(R.id.tv1);
        actualPace = (TextView) root.findViewById(R.id.tv3);
        calories = (TextView) root.findViewById(R.id.tv2);
        km = root.findViewById(R.id.tvp);

        //Sensor variable initialization
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        chronometer = root.findViewById(R.id.time);

        setButtonListener();

        return root;
    }

    //Callback interface
    public interface UpdateDataListener {
        void onUpdateData(LocalDateTime past, LocalDateTime now);
    }


    private void setButtonListener() {

        //setOnClickListener of the Play ImageButton
        play.setOnClickListener(v -> {
            play.setVisibility(View.GONE);
            pause.setVisibility(View.VISIBLE);
            stop.setVisibility(View.VISIBLE);
            pause.setImageResource(R.drawable.pause);
            pauseManagement = true;

            //Chronometer handling
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();

            //Sensor handling
            startResumeRun();

        });

        //setOnClickListener of the Stop ImageButton
        stop.setOnClickListener(v -> {
            play.setVisibility(View.VISIBLE);
            pause.setVisibility(View.GONE);
            stop.setVisibility(View.GONE);

            if(sensorListener != null){
                globalList.addAll(sensorListener.getLocalList());
            }

            //Sensor handling
            stopPauseRun();

            //Chronometer handling
            chronometer.stop();
            chronometer.setBase(SystemClock.elapsedRealtime());

            //Reset the textView data
            calories.setText(String.valueOf(0));
            km.setText(String.valueOf(0));
            averagePace.setText("_'__''");
            actualPace.setText("_'__''");

            pause.setImageResource(R.drawable.pause);
            pauseManagement = false;
            terminateRun();

        });

        //setOnClickListener of the Pause ImageButton
        pause.setOnClickListener(v -> {
            if(pauseManagement){
                pause.setImageResource(R.drawable.play);
                pauseManagement = false;

                globalList.addAll(sensorListener.getLocalList());

                //Chronometer handling
                timeWhenStopped = chronometer.getBase() - SystemClock.elapsedRealtime();
                chronometer.stop();

                stopPauseRun();
                //create a run part object and push it in firebase
            }else{
                pause.setImageResource(R.drawable.pause);
                pauseManagement = true;

                //Chronometer handling
                chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                timeWhenStopped = 0;
                chronometer.start();


                startResumeRun();
            }
        });
    }

    //Method used to send the data to Firebase
    public void terminateRun(){

        //TODO 1 : push the data to firebase
        globalList.removeAll(globalList);
    }

    //Method used to activate the sensorListener when the user press start or resume buttons
    public void startResumeRun(){
        if (accSensor == null) {
            Log.e("SensorError", "Accelerometer sensor not available");

        }else if(sensorListener == null){
            sensorListener = new StepCounterListener(this::updateData);
            sensorManager.registerListener(sensorListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    //Method used to stop the stepCount listener when the user press pause or stop buttons
    public void stopPauseRun(){
        if (sensorListener != null) {
            sensorManager.unregisterListener(sensorListener);
            sensorListener = null;
        }
    }

    public void updateData(LocalDateTime past, LocalDateTime now){
        //Km values updating
        km_value += stride_km;
        if(km_value >= 0.01){
            km.setText(String.format(Locale.getDefault(), "%.2f", km_value));
        }

        //Calories value updating
        calories_value =  km_value * weight_kg;
        if(calories_value >= 1){
            calories.setText(String.valueOf((int) calories_value));
        }


        //Add the new step to the global list
        globalList.add(now);


        //Actual Pace
        actualPace_value = stride_km / (ChronoUnit.MILLIS.between(past, now) / 60000.0f);
        Log.d("Update: ", String.valueOf(actualPace_value));
        actualPace.setText(String.valueOf(actualPace_value));

        //Average Pace
        averagePace_value = km_value / (ChronoUnit.MILLIS.between(globalList.get(0), now) / 60000.0f);
        averagePace.setText(String.valueOf(averagePace_value));
    }

        @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}


////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////

class  StepCounterListener implements SensorEventListener {

    List<Integer> accSeries = new ArrayList<>();
    private double accMag = 0;
    private int lastAddedIndex = 1;
    int stepThreshold = 7;
    List<LocalDateTime> localList = new ArrayList<>();

    //RunFragment object
    private RunFragment.UpdateDataListener updateDataListener;

    //Listener constructor
    public StepCounterListener(RunFragment.UpdateDataListener listener) {
        this.updateDataListener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // Check the type of the sensor, this is helpful in case of multiple sensors
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            //Get the raw acc. sensor data
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            // Computing the magnitude for the acceleration
            accMag = Math.sqrt(x*x+y*y+z*z);

            //Storing the magnitude for the acceleration in accSeries
            accSeries.add((int) accMag);

            peakDetection();
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void peakDetection() {

        int windowSize = 10;
        /* Peak detection algorithm derived from: A Step Counter Service for Java-Enabled Devices Using a Built-In Accelerometer Mladenov et al.
         */
        int currentSize = accSeries.size(); // get the length of the series
        if (currentSize - lastAddedIndex < windowSize) { // if the segment is smaller than the processing window size skip it
            return;
        }

        List<Integer> valuesInWindow = accSeries.subList(lastAddedIndex,currentSize);
        lastAddedIndex = currentSize;

        for (int i = 1; i < valuesInWindow.size()-1; i++) {
            int forwardSlope = valuesInWindow.get(i + 1) - valuesInWindow.get(i);
            int downwardSlope = valuesInWindow.get(i) - valuesInWindow.get(i - 1);

            if (forwardSlope < 0 && downwardSlope > 0 && valuesInWindow.get(i) > stepThreshold) {
                Log.d("ACC STEPS: ", String.valueOf(LocalDateTime.now()));
                countSteps();

            }
        }
    }

    private void countSteps() {
        localList.add(LocalDateTime.now());
        if(localList.size() > 2){
            updateDataListener.onUpdateData(localList.get(localList.size()-1), LocalDateTime.now());
        }

    }

    public List<LocalDateTime> getLocalList(){
        return localList;
    }

}
