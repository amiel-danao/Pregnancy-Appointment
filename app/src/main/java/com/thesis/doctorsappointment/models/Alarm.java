package com.thesis.doctorsappointment.models;

import androidx.annotation.Nullable;

public class Alarm {
    private String time;

    public Alarm(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        Alarm other = (Alarm)obj;
        assert other != null;
        return other.time.equals(this.time);
    }
}
