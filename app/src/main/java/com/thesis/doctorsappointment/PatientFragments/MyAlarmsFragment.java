package com.thesis.doctorsappointment.PatientFragments;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.thesis.doctorsappointment.Adapters.AlarmAdapter;
import com.thesis.doctorsappointment.AlarmReceiver;
import com.thesis.doctorsappointment.AlarmService;
import com.thesis.doctorsappointment.R;
import com.thesis.doctorsappointment.models.Alarm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyAlarmsFragment extends Fragment {

    private Button btnTimePicker;
    private int hour, minute;
    private SharedPreferences sharedPreferences;
    private Button btnSubmit;
    List<Alarm> alarmList = new ArrayList<>();
    AlarmAdapter adapter;

    public MyAlarmsFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_alarms, container, false);
        sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        btnTimePicker = view.findViewById(R.id.btnTimePicker);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        // Step 4: Set the adapter to the RecyclerView
        adapter = new AlarmAdapter(getContext(), alarmList);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewAlarms);

        String alarmsJson = sharedPreferences.getString("Alarms", null);
        if (alarmsJson != null) {
            // Deserialize the JSON string into a list of Alarm objects
            try {
                JSONArray jsonArray = new JSONArray(alarmsJson);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonAlarm = jsonArray.getJSONObject(i);
                    // Convert the JSON object to an Alarm object and add it to the list
                    Alarm alarm = new Alarm(jsonAlarm.getString("time"));
                    alarmList.add(alarm);
                }

                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Use the alarmList as needed
        }


        btnTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        // ...

        // Set OnClickListener for btnSubmit
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAlarm();
            }
        });
        return view;
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfDay) {
                hour = hourOfDay;
                minute = minuteOfDay;

                // Convert the hour to 12-hour format and determine AM/PM
                String timeText;
                if (hourOfDay >= 12) {
                    if (hourOfDay > 12) {
                        hourOfDay -= 12;
                    }
                    timeText = String.format(Locale.getDefault(), "%02d:%02d PM", hourOfDay, minuteOfDay);
                } else {
                    if (hourOfDay == 0) {
                        hourOfDay = 12;
                    }
                    timeText = String.format(Locale.getDefault(), "%02d:%02d AM", hourOfDay, minuteOfDay);
                }

                // Handle the selected time as needed
                // For example, update the UI or save the selected time

                // Update the button text with the selected time
                btnTimePicker.setText(timeText);
            }
        }, hour, minute, false);

        timePickerDialog.show();
    }


    private void saveAlarm() {
        // Get the user ID (replace "user123" with the actual user ID logic)

        // Get the selected time from the button
        String time = btnTimePicker.getText().toString();
        // Check if no time has been picked
        if (time.isEmpty()) {
            // Show an error message or perform any other required actions
            Toast.makeText(requireContext(), "Please pick a time for the alarm", Toast.LENGTH_SHORT).show();
            return;
        }


        String alarmsJson = sharedPreferences.getString("Alarms", null);
        JSONArray jsonArray;
        if (alarmsJson != null) {
            try {
                jsonArray = new JSONArray(alarmsJson);
            } catch (JSONException e) {
                e.printStackTrace();
                jsonArray = new JSONArray();
            }
        } else {
            jsonArray = new JSONArray();
        }

        // Check if the alarm already exists
        if (isAlarmAlreadyExists(jsonArray, time)) {
            Toast.makeText(getActivity(), "Alarm already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new JSON object for the alarm
        JSONObject jsonAlarm = new JSONObject();
        try {
            jsonAlarm.put("time", time);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // Add the new alarm to the JSON array
        jsonArray.put(jsonAlarm);

        // Save the updated alarms back to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Alarms", jsonArray.toString());
        editor.apply();

        // Clear the picked time and btnTimePicker
        btnTimePicker.setText("");

        Alarm alarm = new Alarm(time);
        alarmList.add(alarm);
        adapter.notifyItemInserted(alarmList.size());

        Long nextAlarmDelay = AlarmReceiver.getNextAlarmDelay(getContext());
        AlarmService.setAlarm(getContext(), nextAlarmDelay);

        Toast.makeText(getActivity(), "Alarm saved successfully", Toast.LENGTH_SHORT).show();
    }

    private boolean isAlarmAlreadyExists(JSONArray jsonArray, String alarmTime) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonAlarm = jsonArray.getJSONObject(i);
                String savedTime = jsonAlarm.getString("time");
                if (savedTime.equals(alarmTime)) {
                    return true; // Alarm already exists
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false; // Alarm does not exist
    }

}