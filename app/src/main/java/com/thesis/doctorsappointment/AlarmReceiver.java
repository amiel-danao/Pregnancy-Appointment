package com.thesis.doctorsappointment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "DoctorChannelId";
    private static final String TAG = "myLogTag";
    private static final Long oneDayMillis = 24 * 60 * 60 * 1000L;;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"RECEIVED");
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        long fireTime = sharedPreferences.getLong(AlarmService.UPDATE_TIME_KEY, (new Date()).getTime());

        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"PERFORMED");
                Long nextAlarmDelay =  getNextAlarmDelay(context);
//                long nextDelay = (new Date()).getTime() + nextAlarmDelay;
                AlarmService.setAlarm(context, nextAlarmDelay);

                showNotification(context);
//                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "hcannel id")
//                        .setContentTitle("Patient Alarm")
//                        .setContentText("It's time to take your medicine!")
//                        .setSmallIcon(R.drawable.app_icon)
//                        .setContentIntent(pendingIntent)
//                        .setAutoCancel(true);

                playSoundLoop(context);

//                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
//                notificationManager.notify(1001, builder.build());
            }
        }, fireTime);
    }

    private void playSoundLoop(Context context) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }


    public static Long getNextAlarmDelay(Context context){
        // Get the current time in milliseconds
        long millisFromToday = AlarmService.getTodayMillis();

        long nextAlarmTimeMillis = Long.MAX_VALUE;
        long nextDayAlarmTimeMillis = 0L;
        List<Long> alarmsMillisList = getAlarmsMillis(context);

        for (Long alarmTimeMillis: alarmsMillisList){
            if (millisFromToday - alarmTimeMillis > 0 && alarmTimeMillis < nextAlarmTimeMillis && alarmTimeMillis > 0) {
                nextAlarmTimeMillis = alarmTimeMillis;
            }
            if(alarmTimeMillis < nextDayAlarmTimeMillis){
                nextDayAlarmTimeMillis = alarmTimeMillis;
            }
        }

        if(nextAlarmTimeMillis == Long.MAX_VALUE){
            nextAlarmTimeMillis = nextDayAlarmTimeMillis + oneDayMillis;
        }

        return nextAlarmTimeMillis;
    }

    public static List<Long> getAlarmsMillis(Context context){
        List<Long> list = new ArrayList<>();

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
                Long alarmMillis = AlarmService.convertTimeToMillis(alarmTime);
                list.add(alarmMillis);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return list;
    }

    public static void showNotification(Context context) {
        Intent intent = new Intent(context, AlarmActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, flags);

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Channel Name",
                NotificationManager.IMPORTANCE_HIGH
        );

// Set additional channel properties
        channel.setDescription("Pregnancy Appointment");
        // Create and display the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.createNotificationChannel(channel);
        // Build the notification content and settings
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle("Patient Medicine Alarm")
                .setContentText("It's time to take your medicine")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Display the notification
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
