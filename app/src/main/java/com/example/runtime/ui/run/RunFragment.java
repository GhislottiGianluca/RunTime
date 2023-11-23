package com.example.runtime.ui.run;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.runtime.R;
import com.example.runtime.databinding.FragmentRunBinding;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class RunFragment extends Fragment {

    private FragmentRunBinding binding;

    private ImageButton play;
    private ImageButton pause;
    private ImageButton stop;
    private boolean pauseManagement = true;
    private float heightcm = 0;
    private float stridem = (float) (heightcm * 1.4);

    //Sensor variable managing
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

        play = (ImageButton) root.findViewById(R.id.play);
        pause = (ImageButton) root.findViewById(R.id.pause);
        stop = (ImageButton) root.findViewById(R.id.stop);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        setButtonListener();

        return root;
    }

    private void setButtonListener() {

        //setOnClickListener of the Play ImageButton
        play.setOnClickListener(v -> {
            play.setVisibility(View.GONE);
            pause.setVisibility(View.VISIBLE);
            stop.setVisibility(View.VISIBLE);
            pause.setImageResource(R.drawable.pause);

            startResumeRun();

        });

        //setOnClickListener of the Stop ImageButton
        stop.setOnClickListener(v -> {
            play.setVisibility(View.VISIBLE);
            pause.setVisibility(View.GONE);
            stop.setVisibility(View.GONE);
            globalList.addAll(sensorListener.getLocalList());
            stopPauseRun();
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
                stopPauseRun();
                //create a run part object and push it in firebase
            }else{
                pause.setImageResource(R.drawable.pause);
                pauseManagement = true;
                startResumeRun();
            }
        });
    }

    //Method used to send the data to Firebase
    public void terminateRun(){
        globalList.removeAll(globalList);
    }

    //Method used to activate the sensorListener when the user press start or resume buttons
    public void startResumeRun(){
        if (accSensor == null) {
            Log.e("SensorError", "Accelerometer sensor not available");

        }else if(sensorListener == null){
            sensorListener = new StepCounterListener();
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

        @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}


class  StepCounterListener implements SensorEventListener {

    List<Integer> accSeries = new ArrayList<>();
    private double accMag = 0;
    private int lastAddedIndex = 1;
    int stepThreshold = 7;
    List<LocalDateTime> localList = new ArrayList<>();


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
                countSteps();
                Log.d("ACC STEPS: ", String.valueOf(LocalDateTime.now()));

            }
        }
    }

    private void countSteps() {
        localList.add(LocalDateTime.now());

    }

    public List<LocalDateTime> getLocalList(){
        return localList;
    }

}
