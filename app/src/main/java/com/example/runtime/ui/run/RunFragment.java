package com.example.runtime.ui.run;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.runtime.R;
import com.example.runtime.databinding.FragmentRunBinding;

public class RunFragment extends Fragment {

    private FragmentRunBinding binding;

    private ImageButton play;
    private ImageButton pause;
    private ImageButton stop;
    private boolean pauseManagement = true;
    private float heightcm = 0;
    private float stridem = (float) (heightcm * 1.4);


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        RunViewModel runViewModel =
                new ViewModelProvider(this).get(RunViewModel.class);

        binding = FragmentRunBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        play = (ImageButton) root.findViewById(R.id.play);
        pause = (ImageButton) root.findViewById(R.id.pause);
        stop = (ImageButton) root.findViewById(R.id.stop);

        setButtonListener();

        return root;
    }

    private void setButtonListener() {

        //setOnClickListener of the Play ImageButton
        play.setOnClickListener(v -> {
            play.setVisibility(View.GONE);
            pause.setVisibility(View.VISIBLE);
            stop.setVisibility(View.VISIBLE);
            pause.setImageResource(android.R.drawable.ic_media_pause);
        });

        //setOnClickListener of the Stop ImageButton
        stop.setOnClickListener(v -> {
            play.setVisibility(View.VISIBLE);
            pause.setVisibility(View.GONE);
            stop.setVisibility(View.GONE);
            pause.setImageResource(android.R.drawable.ic_media_pause);
            pauseManagement = true;
        });

        //setOnClickListener of the Pause ImageButton
        pause.setOnClickListener(v -> {
            if(pauseManagement){
                pause.setImageResource(android.R.drawable.ic_media_play);
                pauseManagement = false;
                //create a run part object and push it in firebase
            }else{
                pause.setImageResource(android.R.drawable.ic_media_pause);
                pauseManagement = true;
            }
        });

    }



        @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
