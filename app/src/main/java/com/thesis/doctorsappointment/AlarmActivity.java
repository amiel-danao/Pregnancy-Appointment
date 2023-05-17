package com.thesis.doctorsappointment;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class AlarmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        // Replace the placeholder fragment container with the AlarmFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AlarmFragment())
                .commit();
    }
}
