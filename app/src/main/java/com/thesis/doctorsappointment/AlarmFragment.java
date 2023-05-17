package com.thesis.doctorsappointment;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class AlarmFragment extends Fragment {

    private MediaPlayer mediaPlayer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);

        // Initialize and start playing the alarm sound
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm_sound);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Release the MediaPlayer when the fragment is destroyed
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
