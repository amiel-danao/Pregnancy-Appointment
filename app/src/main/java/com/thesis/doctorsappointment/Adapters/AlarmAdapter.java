package com.thesis.doctorsappointment.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.thesis.doctorsappointment.AlarmReceiver;
import com.thesis.doctorsappointment.AlarmService;
import com.thesis.doctorsappointment.R;
import com.thesis.doctorsappointment.models.Alarm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private List<Alarm> alarmList;
    private Context context;

    public AlarmAdapter(Context context, List<Alarm> alarmList) {
        this.alarmList = alarmList;
        this.context = context;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        Alarm alarmTime = alarmList.get(position);
        holder.btnRemoveAlarm.setTag(alarmTime.getTime());
        holder.btnRemoveAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String alarm = (String)v.getTag();
                new AlertDialog.Builder(context).setCancelable(false).setMessage("Are you sure you want to delete this alarm "+ alarm + "?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences preferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                        String json = preferences.getString("Alarms", "");

                        // Step 2: Convert JSON string to JSON object
                        JSONArray jsonArray = null;
                        try {
                            jsonArray = new JSONArray(json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            jsonArray = new JSONArray();
                        }

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonAlarm = null;
                            try {
                                jsonAlarm = jsonArray.getJSONObject(i);
                                if(jsonAlarm.getString("time").equals(alarm)){
                                    jsonArray.remove(i);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        // Step 4: Convert JSON object back to JSON string
                        String updatedJson = jsonArray.toString();

                        // Step 5: Save updated JSON string back to shared preference
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("Alarms", updatedJson);
                        editor.apply();

                        int removedIndex = alarmList.indexOf(alarmTime);
                        alarmList.remove(alarmTime);
                        notifyItemRemoved(removedIndex);

                        Long nextAlarmDelay = AlarmReceiver.getNextAlarmDelay(context);
                        AlarmService.setAlarm(context, nextAlarmDelay);
                    }
                }).setNegativeButton("No",null).show();
            }
        });

        holder.bind(alarmTime.getTime());
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    public class AlarmViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAlarmTime;
        public AppCompatImageView btnRemoveAlarm;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAlarmTime = itemView.findViewById(R.id.tvAlarmTime);
            btnRemoveAlarm = itemView.findViewById(R.id.btnRemoveAlarm);
        }

        public void bind(String alarmTime) {
            tvAlarmTime.setText(alarmTime);
        }
    }
}
