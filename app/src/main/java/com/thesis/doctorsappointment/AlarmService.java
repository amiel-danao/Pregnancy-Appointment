package com.thesis.doctorsappointment;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thesis.doctorsappointment.models.Alarm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class AlarmService {
    public static String UPDATE_TIME_KEY = "update_time_key";

    public static void scheduleAlarms(Context context) {
        // Retrieve the saved alarms from SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
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

        // Schedule the alarms
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonAlarm = jsonArray.getJSONObject(i);
                String alarmTime = jsonAlarm.getString("time");
                scheduleAlarm(context, alarmTime);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void scheduleAlarm(Context context, String alarmTime) {
        // Convert the alarm time to milliseconds
        long alarmMillis = convertTimeToMillis(alarmTime);

//        Calendar calendar = Calendar.getInstance();

        long currentTimeMillis = getTodayMillis();

        // Calculate the delay until the alarm time
        long delayMillis = alarmMillis - currentTimeMillis;

        if (delayMillis > 0) {
            setAlarm(context, delayMillis);
        }
    }

    public static Long getTodayMillis(){
        LocalDateTime currentDateTime = LocalDateTime.now();
        ZoneId philippinesZone = ZoneId.of("Asia/Manila");
        ZoneOffset philippinesOffset = philippinesZone.getRules().getOffset(currentDateTime);
        LocalDateTime startOfDay = currentDateTime.toLocalDate().atStartOfDay();
        Instant startOfDayInstant = startOfDay.toInstant(philippinesOffset);
        long startOfDayMillis = (Instant.now().toEpochMilli() - startOfDayInstant.toEpochMilli());

// Calculate the current time in milliseconds from today
        return startOfDayMillis;
    }

    public static long convertTimeToMillis(String timeString) {
// Get the current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();

// Parse the time string
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US);
        LocalTime parsedTime = LocalTime.parse(timeString, timeFormatter);

// Combine the current date and parsed time
        LocalDateTime targetDateTime = currentDateTime.with(parsedTime);

// Get the start of the day
        LocalDateTime startOfDay = currentDateTime.toLocalDate().atStartOfDay();

// Calculate the time difference in milliseconds
        long timeDifferenceMillis = Duration.between(startOfDay, targetDateTime).toMillis();

        ZoneId philippinesZone = ZoneId.of("Asia/Manila");
        ZoneOffset philippinesOffset = philippinesZone.getRules().getOffset(currentDateTime);
        Instant startOfDayInstant = startOfDay.toInstant(philippinesOffset);

        return timeDifferenceMillis - (Instant.now().toEpochMilli() - startOfDayInstant.toEpochMilli());
    }

    public static void setAlarm(Context context, long triggerTimeInMillis) {
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(context, AlarmService.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
//
//        // Set the alarm to trigger at the specified time
//        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeInMillis, pendingIntent);

        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().putLong(UPDATE_TIME_KEY, triggerTimeInMillis).apply();

        Intent startIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(triggerTimeInMillis, pendingIntent), pendingIntent);
//        AlarmManager alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC, triggerTimeInMillis, pendingIntent);
    }


}
